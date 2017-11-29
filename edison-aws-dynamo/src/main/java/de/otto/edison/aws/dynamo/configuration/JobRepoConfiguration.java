package de.otto.edison.aws.dynamo.configuration;

import de.otto.edison.aws.configuration.AwsProperties;
import de.otto.edison.aws.dynamo.jobs.DynamoJobRepoProperties;
import de.otto.edison.aws.dynamo.jobs.DynamoJobRepository;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.services.dynamodb.DynamoDBClient;

@Configuration
@ConditionalOnClass(name = "de.otto.edison.jobs.configuration.JobsConfiguration")
@EnableConfigurationProperties({DynamoJobRepoProperties.class, AwsProperties.class})
public class JobRepoConfiguration {

    @Bean
    @ConditionalOnProperty(name = "edison.aws.dynamo.jobs.table-name")
    public DynamoJobRepository dynamoJobRepository(final DynamoDBClient dynamoDBClient,
                                                   final DynamoJobRepoProperties dynamoJobRepoProperties) {
        return new DynamoJobRepository(dynamoDBClient, dynamoJobRepoProperties);
    }

}
