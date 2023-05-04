package de.otto.edison.aws.s3;

import de.otto.edison.aws.configuration.AwsConfiguration;
import de.otto.edison.aws.s3.configuration.S3Configuration;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;

import java.io.File;
import java.io.FileWriter;
import java.net.URI;
import java.util.List;

import static java.nio.file.Files.createTempFile;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.startsWith;
import static software.amazon.awssdk.services.s3.S3Configuration.builder;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {AwsConfiguration.class, S3Configuration.class})
@TestPropertySource("classpath:application-test.properties")
public class S3ServiceIntegrationTest {

    private static final String TESTBUCKET = "testbucket";

    private S3Service s3Service;

    @BeforeEach
    public void setUp() throws Exception {

        final S3Client s3Client = S3Client.builder()
                .credentialsProvider(StaticCredentialsProvider.create(AwsBasicCredentials.create("test", "test")))
                .region(Region.US_EAST_1)
                .endpointOverride(new URI("http://localhost:4572"))
                .serviceConfiguration(builder().pathStyleAccessEnabled(true).build())
                .build();

        s3Service = new S3Service(s3Client);

        s3Service.createBucket(TESTBUCKET);
        s3Service.deleteAllObjectsInBucket(TESTBUCKET);
    }

    @AfterEach
    public void tearDown() {
        s3Service.deleteAllObjectsInBucket(TESTBUCKET);
    }

    @Test
    public void shouldOnlyDeleteFilesWithPrefix() throws Exception {
        //given
        final File tempFile = createTempFile("test", ".txt").toFile();
        try (final FileWriter writer = new FileWriter(tempFile)) {
            writer.append("Hello World!");
            writer.flush();
        }
        s3Service.upload(TESTBUCKET, tempFile);
        final File prefixedTempFile = createTempFile("prefix", ".txt").toFile();
        try (final FileWriter writer = new FileWriter(prefixedTempFile)) {
            writer.append("Hello World!");
            writer.flush();
        }
        s3Service.upload(TESTBUCKET, prefixedTempFile);

        System.out.println(prefixedTempFile.getName());

        //when
        s3Service.deleteAllObjectsWithPrefixInBucket(TESTBUCKET, "prefix");

        //then
        final List<String> allFiles = s3Service.listAllFiles(TESTBUCKET);
        System.out.println(allFiles);
        assertThat(allFiles, contains(startsWith("test")));
        assertThat(allFiles, not(contains(startsWith("prefixed_test"))));
    }

    //@Test
    public void shouldDeleteAllFilesInBucket() throws Exception {
        //given
        s3Service.upload(TESTBUCKET, createTempFile("test", ".json.zip").toFile());
        s3Service.upload(TESTBUCKET, createTempFile("prefixed_test", ".json.zip").toFile());

        //when
        s3Service.deleteAllObjectsInBucket(TESTBUCKET);

        //then
        final List<String> allFiles = s3Service.listAllFiles(TESTBUCKET);
        assertThat(allFiles, hasSize(0));
    }

}
