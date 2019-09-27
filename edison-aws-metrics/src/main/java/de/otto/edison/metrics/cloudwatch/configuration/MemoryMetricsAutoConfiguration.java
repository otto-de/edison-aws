package de.otto.edison.metrics.cloudwatch.configuration;

import de.otto.edison.metrics.cloudwatch.MemoryUsageMetric;
import io.micrometer.cloudwatch2.CloudWatchMeterRegistry;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConditionalOnProperty(prefix = "management.metrics.export.cloudwatch", name = "namespace")
@ConditionalOnClass({ CloudWatchMeterRegistry.class })
public class MemoryMetricsAutoConfiguration {
    @Bean
    @ConditionalOnProperty(value = "management.metrics.export.cloudwatch.enabled", matchIfMissing = true)
    public MemoryUsageMetric memoryUsageMetric(MeterRegistry meterRegistry) {
        return new MemoryUsageMetric(meterRegistry);
    }
}
