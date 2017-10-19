package de.otto.edison.metrics.cloudwatch;

import com.amazonaws.services.cloudwatch.AmazonCloudWatchAsync;
import com.codahale.metrics.MetricRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.TimeUnit;

@Configuration
@EnableConfigurationProperties(CloudWatchMetricsProperties.class)
@ConditionalOnProperty(name = "edison.cloudWatch.metrics.enabled", havingValue = "true")
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
}
