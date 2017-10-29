package de.otto.edison.aws.dynamodb.configuration;

import de.otto.edison.aws.dynamodb.jobs.DynamoJobMetaRepository;
import de.otto.edison.aws.dynamodb.jobs.DynamoJobRepository;
import de.otto.edison.jobs.repository.JobMetaRepository;
import de.otto.edison.jobs.repository.JobRepository;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.services.dynamodb.DynamoDBClient;

import static org.slf4j.LoggerFactory.getLogger;

@Configuration
@ConditionalOnClass(name = "de.otto.edison.jobs.configuration.JobsConfiguration")
public class DynamoJobConfiguration {

    private static final Logger LOG = getLogger(DynamoJobConfiguration.class);

    @Bean
    public JobRepository jobRepository(final DynamoDBClient dynamoClient, @Value("${edison.jobs.collection.jobinfo:jobinfo}") final String collectionName) {
        LOG.info("===============================");
        LOG.info("Using DynamoJobRepository with {} DynamoDatabase impl.", dynamoClient.getClass().getSimpleName());
        LOG.info("===============================");
        return new DynamoJobRepository(dynamoClient, collectionName);
    }

    @Bean
    public JobMetaRepository jobMetaRepository(final DynamoDBClient dynamoClient, @Value("${edison.jobs.collection.jobmeta:jobmeta}") final String collectionName) {
        LOG.info("===============================");
        LOG.info("Using DynamoJobMetaRepository with {} DynamoDatabase impl.", dynamoClient.getClass().getSimpleName());
        LOG.info("===============================");
        return new DynamoJobMetaRepository(dynamoClient, collectionName);
    }
}
