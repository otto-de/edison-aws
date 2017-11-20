package de.otto.edison.aws.config.s3;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.services.s3.S3Client;

@Configuration
@EnableConfigurationProperties(S3ConfigProperties.class)
@ConditionalOnProperty(name = "edison.aws.config.s3.enabled", havingValue = "true")
public class S3BucketPropertyConfig {

    @Bean
    public S3BucketPropertySourcePostProcessor s3BucketPropertySourcePostProcessor(S3ConfigProperties s3ConfigProperties,
                                                                                   S3BucketPropertyReader s3BucketPropertyReader) {
        return new S3BucketPropertySourcePostProcessor(s3BucketPropertyReader, s3ConfigProperties);
    }

    @Bean
    public S3BucketPropertyReader s3BucketPropertyReader(S3Client s3Client,
                                                         S3ConfigProperties s3ConfigProperties) {
        return new S3BucketPropertyReader(s3Client, s3ConfigProperties);
    }
}
