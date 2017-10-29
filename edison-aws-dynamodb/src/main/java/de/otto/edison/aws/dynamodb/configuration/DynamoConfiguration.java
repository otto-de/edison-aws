package de.otto.edison.aws.dynamodb.configuration;

import org.slf4j.Logger;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.AwsCredentialsProvider;
import software.amazon.awssdk.services.dynamodb.DynamoDBClient;
import software.amazon.awssdk.services.dynamodb.document.DynamoDb;

import java.net.URI;

import static org.slf4j.LoggerFactory.getLogger;
import static software.amazon.awssdk.regions.Region.of;

@Configuration
@EnableConfigurationProperties(DynamoProperties.class)
public class DynamoConfiguration {

    private static final Logger LOG = getLogger(DynamoConfiguration.class);

    @Bean
    @ConditionalOnMissingBean(name = "dynamoClient", value = DynamoDBClient.class)
    public DynamoDBClient dynamoClient(final DynamoProperties dynamoProperties,
                                       final AwsCredentialsProvider awsCredentialsProvider) {
        LOG.info("Creating DynamoClient with " + dynamoProperties.toString());

        return DynamoDBClient.builder()
                .region(of(dynamoProperties.getRegion()))
                .endpointOverride(URI.create(dynamoProperties.getEndpoint()))
                .credentialsProvider(awsCredentialsProvider)
                .build();
    }

    @Bean
    @ConditionalOnMissingBean(name = "dynamoDatabase", value = DynamoDb.class)
    DynamoDb dynamoDatabase(final DynamoDBClient dynamoClient) {
        return new DynamoDb(dynamoClient);
    }

}
