package de.otto.edison.aws.s3;

import de.otto.edison.aws.configuration.AwsConfiguration;
import de.otto.edison.aws.s3.configuration.S3Configuration;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.File;
import java.io.FileWriter;
import java.util.List;

import static java.nio.file.Files.createTempFile;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.startsWith;
import static org.junit.Assert.assertThat;

@RunWith(SpringRunner.class)
@ContextConfiguration(classes = {AwsConfiguration.class, S3Configuration.class})
@TestPropertySource("classpath:application-test.properties")
public class S3ServiceIntegrationTest {

    // TODO: Migrate s3 tests to LocalStack!

    private static final String TESTBUCKET = "de-otto-s3-service-integration-test-bucket";

    @Autowired
    private S3Service s3Service;

    @Before
    public void setUp() throws Exception {
        s3Service.createBucket(TESTBUCKET);
        s3Service.deleteAllObjectsInBucket(TESTBUCKET);
    }

    @After
    public void tearDown() throws Exception {
        s3Service.deleteAllObjectsInBucket(TESTBUCKET);
    }

    @Test
    public void shouldOnlyDeleteFilesWithPrefix() throws Exception {
        //given
        final File tempFile = createTempFile("test", ".txt").toFile();
        try (FileWriter writer = new FileWriter(tempFile)) {
            writer.append("Hello World!");
            writer.flush();
        }
        s3Service.upload(TESTBUCKET, tempFile);
        final File prefixedTempFile = createTempFile("prefixed", ".txt").toFile();
        try (FileWriter writer = new FileWriter(prefixedTempFile)) {
            writer.append("Hello World!");
            writer.flush();
        }
        s3Service.upload(TESTBUCKET, prefixedTempFile);

        //when
        s3Service.deleteAllObjectsWithPrefixInBucket(TESTBUCKET, "prefix");

        //then
        List<String> allFiles = s3Service.listAllFiles(TESTBUCKET);
        System.out.println(allFiles);
        assertThat(allFiles, contains(startsWith("test")));
        assertThat(allFiles, not(contains(startsWith("prefixed_test"))));
    }

    @Test
    public void shouldDeleteAllFilesInBucket() throws Exception {
        //given
        s3Service.upload(TESTBUCKET, createTempFile("test", ".json.zip").toFile());
        s3Service.upload(TESTBUCKET, createTempFile("prefixed_test", ".json.zip").toFile());

        //when
        s3Service.deleteAllObjectsInBucket(TESTBUCKET);

        //then
        List<String> allFiles = s3Service.listAllFiles(TESTBUCKET);
        assertThat(allFiles, hasSize(0));
    }

}
