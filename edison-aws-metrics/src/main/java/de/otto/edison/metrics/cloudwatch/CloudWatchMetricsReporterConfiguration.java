package de.otto.edison.metrics.cloudwatch;

import de.otto.edison.aws.configuration.AwsProperties;
import io.micrometer.core.instrument.MeterRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


@Configuration
@EnableConfigurationProperties({AwsProperties.class, CloudWatchMetricsProperties.class})
@ConditionalOnProperty(name = "edison.aws.metrics.cloudWatch.enabled", havingValue = "true")
public class CloudWatchMetricsReporterConfiguration {

    private static final Logger LOG = LoggerFactory.getLogger(CloudWatchMetricsReporterConfiguration.class);

    @Bean
    public CloudWatchMetricFilter cloudWatchMetricFilter(
            final CloudWatchMetricsProperties metricsProperties,
            final MeterRegistry meterRegistry) {
        final CloudWatchMetricFilter cloudWatchMetricFilter = new CloudWatchMetricFilter(meterRegistry, metricsProperties.getAllowedMetrics());
        return cloudWatchMetricFilter;
    }
}
