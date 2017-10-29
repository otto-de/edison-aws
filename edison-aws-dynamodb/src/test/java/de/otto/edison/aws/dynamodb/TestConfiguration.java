package de.otto.edison.aws.dynamodb;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.AwsCredentials;
import software.amazon.awssdk.auth.AwsCredentialsProvider;
import software.amazon.awssdk.auth.StaticCredentialsProvider;

@Configuration
public class TestConfiguration {

    @Bean
    public AwsCredentialsProvider awsCredentialsProvider() {
        return new StaticCredentialsProvider(
                new AwsCredentials("test", "test")
        );
    }

}
