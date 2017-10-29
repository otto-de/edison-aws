package de.otto.edison.aws.s3.configuration;

import de.otto.edison.aws.s3.togglz.S3StateRepository;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.togglz.core.repository.StateRepository;
import org.togglz.core.repository.cache.CachingStateRepository;
import software.amazon.awssdk.services.s3.S3Client;

@Configuration
@EnableConfigurationProperties(S3TogglzProperties.class)
@ConditionalOnProperty(name = "edison.aws.s3.togglz.enabled", havingValue = "true", matchIfMissing = true)
public class S3TogglzConfiguration {

    @Bean
    @ConditionalOnProperty(name = "edison.aws.s3.togglz.bucket-name")
    public StateRepository stateRepository(final S3TogglzProperties s3TogglzProperties,
                                           final S3Client s3Client) {
        S3StateRepository togglzRepository = new S3StateRepository(s3TogglzProperties, s3Client);
        return new CachingStateRepository(togglzRepository, 30000);
    }
}
