package de.otto.edison.aws.dynamo.jobs;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import software.amazon.awssdk.core.auth.AwsCredentials;
import software.amazon.awssdk.core.auth.StaticCredentialsProvider;
import software.amazon.awssdk.services.dynamodb.DynamoDBClient;

import java.net.URI;

@Configuration
public class DynamoDbTestConfiguration {
    @Bean
    @Profile("test")
    @Primary
    public DynamoDBClient dynamoDBClient() {
        return DynamoDBClient.builder()
                .endpointOverride(URI.create("http://localhost:4569")) // 172.17.0.2
                .credentialsProvider(new StaticCredentialsProvider(new AwsCredentials("foobar", "foobar")))
                .build();
    }
}
