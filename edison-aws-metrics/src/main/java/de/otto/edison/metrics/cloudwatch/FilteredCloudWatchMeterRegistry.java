package de.otto.edison.metrics.cloudwatch;

import io.micrometer.cloudwatch2.CloudWatchConfig;
import io.micrometer.cloudwatch2.CloudWatchMeterRegistry;
import io.micrometer.core.instrument.Clock;
import io.micrometer.core.instrument.Meter;
import io.micrometer.core.instrument.util.NamedThreadFactory;
import software.amazon.awssdk.services.cloudwatch.CloudWatchAsyncClient;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class FilteredCloudWatchMeterRegistry extends CloudWatchMeterRegistry {

    public static final String CLOUDWATCH_METRICS_PUBLISHER = "cloudwatch-metrics-publisher";
    final CloudWatchMetricFilter cloudWatchMetricFilter;

    public FilteredCloudWatchMeterRegistry(final CloudWatchMetricFilter cloudWatchMetricFilter, final CloudWatchConfig config, final Clock clock, final CloudWatchAsyncClient cloudWatchAsyncClient) {
        super(config, clock, cloudWatchAsyncClient, new NamedThreadFactory(CLOUDWATCH_METRICS_PUBLISHER));
        this.cloudWatchMetricFilter = cloudWatchMetricFilter;
    }

    @Override
    public List<Meter> getMeters() {
        final List<Meter> meters = super.getMeters();
        // Workaround check thread name to deside context
        // only filter for cloudwatch-metrics-publisher
        if (Objects.equals(Thread.currentThread().getName(), CLOUDWATCH_METRICS_PUBLISHER)) {
            return cloudWatchMetricFilter.filter(meters);
        } else {
            return meters;
        }
    }
}
