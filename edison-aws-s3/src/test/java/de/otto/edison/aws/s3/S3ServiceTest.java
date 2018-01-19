package de.otto.edison.aws.s3;

import org.junit.Before;
import org.junit.Test;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectResponse;

import java.io.File;
import java.nio.file.Path;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class S3ServiceTest {

    private S3Client s3Client;
    private S3Service testee;

    @Before
    public void setUp() {
        s3Client = mock(S3Client.class);

        testee = new S3Service(s3Client);
    }

    @Test
    public void shouldPassServerSideEncryptionToClient() {
        // given
        when(s3Client.putObject(any(), any(Path.class))).thenReturn(PutObjectResponse.builder().build());


        // when
        File someFile = new File("someFile");
        testee.upload("someBucket", someFile, S3Service.EncryptionType.AWS_KMS);

        // then
        PutObjectRequest expectedRequest = PutObjectRequest.builder()
                .bucket("someBucket")
                .key(someFile.getName())
                .serverSideEncryption("aws:kms")
                .build();
        verify(s3Client).putObject(expectedRequest, someFile.toPath());
    }

    @Test
    public void shouldNotPassServerSideEncryptionToClientIfNotRequested() {
        // given
        when(s3Client.putObject(any(), any(Path.class))).thenReturn(PutObjectResponse.builder().build());

        // when
        File someFile = new File("someFile");
        testee.upload("someBucket", someFile);

        // then
        PutObjectRequest expectedRequest = PutObjectRequest.builder()
                .bucket("someBucket")
                .key(someFile.getName())
                .build();
        verify(s3Client).putObject(expectedRequest, someFile.toPath());
    }
}