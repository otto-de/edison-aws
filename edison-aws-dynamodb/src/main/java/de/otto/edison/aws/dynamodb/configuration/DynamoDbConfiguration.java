package de.otto.edison.aws.dynamodb.configuration;

import de.otto.edison.aws.configuration.AwsProperties;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.AwsCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.dynamodb.DynamoDBClient;

@Configuration
@ConditionalOnProperty(name = "edison.aws.dynamodb.enabled", havingValue = "true", matchIfMissing = true)
public class DynamoDbConfiguration {

    @Bean
    public DynamoDBClient dynamoDBClient(AwsCredentialsProvider credentialsProvider, AwsProperties awsProperties) {
        return DynamoDBClient.builder()
                .region(Region.of(awsProperties.getRegion()))
                .credentialsProvider(credentialsProvider)
                .build();
    }

}
