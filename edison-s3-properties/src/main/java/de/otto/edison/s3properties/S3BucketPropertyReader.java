package de.otto.edison.s3properties;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.GetObjectRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

@Component
@ConditionalOnProperty(name = "edison.aws.s3-properties.enabled", havingValue = "true")
public class S3BucketPropertyReader {

    private final AmazonS3 s3Client;
    private final S3Properties s3Properties;

    @Autowired
    public S3BucketPropertyReader(final AmazonS3 s3Client, final S3Properties s3Properties) {
        this.s3Client = s3Client;
        this.s3Properties = s3Properties;
    }

    Properties getPropertiesFromS3() {
        final GetObjectRequest getObjectRequest = new GetObjectRequest(s3Properties.getBucketname(), s3Properties.getFilename());
        return makeProperties(s3Client.getObject(getObjectRequest).getObjectContent());
    }

    private Properties makeProperties(final InputStream inputStream) {
        try {
            final Properties properties = new Properties();
            properties.load(inputStream);
            return properties;
        } catch (final IOException exception) {
            throw new RuntimeException(exception);
        }
    }
}
