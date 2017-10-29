package de.otto.edison.aws.config.s3;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

@Component
@ConditionalOnProperty(name = "edison.aws.config.s3.enabled", havingValue = "true")
public class S3BucketPropertyReader {

    private final S3Client s3Client;
    private final S3ConfigProperties s3ConfigProperties;

    @Autowired
    public S3BucketPropertyReader(final S3Client s3Client,
                                  final S3ConfigProperties s3ConfigProperties) {
        this.s3Client = s3Client;
        this.s3ConfigProperties = s3ConfigProperties;
    }

    Properties getPropertiesFromS3() {
        return s3Client.getObject(GetObjectRequest.builder()
                        .bucket(s3ConfigProperties.getBucketname())
                        .key(s3ConfigProperties.getFilename())
                        .build(),
                (response, in) -> makeProperties(in));
    }

    private Properties makeProperties(InputStream inputStream) {
        try {
            Properties properties = new Properties();
            properties.load(inputStream);
            return properties;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}
