package de.otto.edison.aws.config.paramstore;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.AwsCredentialsProvider;
import software.amazon.awssdk.services.ssm.SSMClient;

@Configuration
@EnableConfigurationProperties(ParamStoreConfigProperties.class)
@ConditionalOnProperty(name = "edison.aws.config.paramstore.enabled", havingValue = "true")
public class ParamStoreConfiguration {

    @Bean
    public SSMClient awsSSM(AwsCredentialsProvider awsCredentialsProvider) {
        return SSMClient.builder()
            .credentialsProvider(awsCredentialsProvider)
            .build();
    }
}
