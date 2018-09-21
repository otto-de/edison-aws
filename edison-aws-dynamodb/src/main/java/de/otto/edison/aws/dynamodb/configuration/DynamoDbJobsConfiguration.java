package de.otto.edison.aws.dynamodb.configuration;

import de.otto.edison.aws.configuration.AwsProperties;
import de.otto.edison.aws.dynamodb.jobs.DynamoDbJobMetaRepository;
import de.otto.edison.aws.dynamodb.jobs.DynamoDbJobRepoProperties;
import de.otto.edison.aws.dynamodb.jobs.DynamoDbJobRepository;
import de.otto.edison.jobs.repository.JobMetaRepository;
import de.otto.edison.jobs.repository.JobRepository;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;

@Configuration
@ConditionalOnClass(name = "de.otto.edison.jobs.configuration.JobsConfiguration")
@EnableConfigurationProperties({DynamoDbJobRepoProperties.class, AwsProperties.class})
public class DynamoDbJobsConfiguration {

    @Bean
    @ConditionalOnProperty(name = "edison.aws.dynamodb.jobs.job-info-table-name")
    public JobRepository jobRepository(final DynamoDbClient dynamoDBClient,
                                       final DynamoDbJobRepoProperties dynamoDbJobRepoProperties) {
        return new DynamoDbJobRepository(dynamoDBClient, dynamoDbJobRepoProperties);
    }

    @Bean
    @ConditionalOnProperty(name = "edison.aws.dynamodb.jobs.job-meta-table-name")
    public JobMetaRepository jobMetaRepository(final DynamoDbClient dynamoDBClient,
                                               final DynamoDbJobRepoProperties dynamoDbJobRepoProperties) {
        return new DynamoDbJobMetaRepository(dynamoDBClient, dynamoDbJobRepoProperties);
    }

}
