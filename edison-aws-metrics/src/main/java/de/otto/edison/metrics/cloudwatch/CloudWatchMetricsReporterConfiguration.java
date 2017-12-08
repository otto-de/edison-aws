package de.otto.edison.metrics.cloudwatch;

import com.amazonaws.auth.*;
import com.amazonaws.auth.profile.ProfileCredentialsProvider;
import com.amazonaws.services.cloudwatch.AmazonCloudWatchAsync;
import com.amazonaws.services.cloudwatch.AmazonCloudWatchAsyncClient;
import com.codahale.metrics.MetricRegistry;
import de.otto.edison.aws.configuration.AwsProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Configuration
@EnableConfigurationProperties({AwsProperties.class, CloudWatchMetricsProperties.class})
@ConditionalOnProperty(name = "edison.aws.metrics.cloudWatch.enabled", havingValue = "true")
public class CloudWatchMetricsReporterConfiguration {

    private static final Logger LOG = LoggerFactory.getLogger(CloudWatchMetricsReporterConfiguration.class);

    @Bean
    public CloudWatchMetricsReporter cloudWatchReporter(final MetricRegistry metricRegistry,
                                                        final AmazonCloudWatchAsync cloudWatchAsync,
                                                        final CloudWatchMetricsProperties metricsProperties) {
        final CloudWatchMetricsReporter reporter = new CloudWatchMetricsReporter(
                metricRegistry,
                metricsProperties.getAllowedMetrics(),
                metricsProperties.getNamespace(),
                cloudWatchAsync);
        reporter.start(1, TimeUnit.MINUTES);
        LOG.info("started cloudWatch metrics reporter");
        return reporter;
    }

    @Bean
    public AmazonCloudWatchAsync cloudWatchAsync(final AWSCredentialsProvider awsCredentialsProvider,
                                                 final AwsProperties awsProperties) {
        return AmazonCloudWatchAsyncClient.asyncBuilder()
                .withRegion(awsProperties.getRegion())
                .withCredentials(awsCredentialsProvider)
                .build();
    }
}
