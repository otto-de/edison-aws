package de.otto.edison.metrics.cloudwatch;

import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.auth.AWSCredentialsProviderChain;
import com.amazonaws.auth.EnvironmentVariableCredentialsProvider;
import com.amazonaws.auth.InstanceProfileCredentialsProvider;
import com.amazonaws.auth.profile.ProfileCredentialsProvider;
import com.amazonaws.services.cloudwatch.AmazonCloudWatchAsync;
import com.amazonaws.services.cloudwatch.AmazonCloudWatchAsyncClient;
import com.codahale.metrics.MetricRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Configuration
@EnableConfigurationProperties(CloudWatchMetricsProperties.class)
@ConditionalOnProperty(name = "edison.aws.metrics.cloudWatch.enabled", havingValue = "true")
public class CloudWatchMetricsReporterConfiguration {

    private static final Logger LOG = LoggerFactory.getLogger(CloudWatchMetricsReporterConfiguration.class);

    @Bean
    public CloudWatchMetricsReporter cloudWatchReporter(final MetricRegistry metricRegistry,
                                                        final AmazonCloudWatchAsync cloudWatchAsync,
                                                        final CloudWatchMetricsProperties properties) {
        final CloudWatchMetricsReporter reporter = new CloudWatchMetricsReporter(metricRegistry, properties.getAllowedMetrics(),
                properties.getNamespace(), cloudWatchAsync);
        reporter.start(1, TimeUnit.MINUTES);
        LOG.info("started cloudWatch metrics reporter");
        return reporter;
    }

    @Bean(name = "cloudWatchCredentialsProvider")
    public AWSCredentialsProvider cloudWatchCredentialsProvider(
            @Value("${edison.aws.metrics.cloudWatch.aws.profile}") final String awsProfile) {
        final List<AWSCredentialsProvider> providerList = new ArrayList<>();

        providerList.add(InstanceProfileCredentialsProvider.getInstance());
        providerList.add(new EnvironmentVariableCredentialsProvider());
        providerList.add(new ProfileCredentialsProvider(awsProfile));

        return new AWSCredentialsProviderChain(providerList);
    }

    @Bean
    public AmazonCloudWatchAsync cloudWatchAsync(final AWSCredentialsProvider cloudWatchCredentialsProvider,
                                                 @Value("${edison.aws.metrics.cloudWatch.aws.region}") final String region) {
        return AmazonCloudWatchAsyncClient.asyncBuilder()
                .withRegion(region)
                .withCredentials(cloudWatchCredentialsProvider)
                .build();
    }
}
