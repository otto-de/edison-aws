package de.otto.edison.metrics.cloudwatch;

import io.micrometer.core.instrument.Meter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Tag;
import io.micrometer.core.instrument.config.MeterFilter;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static java.util.Objects.nonNull;

public class CloudWatchMetricFilter {

    private final List<String> allowedMetrics;
    private final Map<String, String> dimensions;


    public CloudWatchMetricFilter(final CloudWatchMetricsProperties cloudWatchMetricsProperties) {
        this.allowedMetrics = cloudWatchMetricsProperties.getAllowedmetrics();
        this.dimensions = cloudWatchMetricsProperties.getDimensions();
        //registerFilter(meterRegistry);
    }

    public List<Meter> filter(final List<Meter> meters) {
        return meters.stream().map(e->filter(e)).filter(e->nonNull(e)).collect(Collectors.toList());
    }

    private Meter filter(final Meter meter) {
        if (matchMetric(meter.getId()))  {
            return new FilteredMeter(mapMetric(meter.getId()), meter.measure());
        }
        return null;
    }

    private void registerFilter(final MeterRegistry meterRegistry) {
        final MeterRegistry.Config config = meterRegistry.config();
        config.meterFilter(MeterFilter.denyUnless(m-> matchMetric(m)));
        config.meterFilter(new MeterFilter() {
            @Override
            public Meter.Id map(Meter.Id id) {
                return mapMetric(id);
            }
        });
    }

    private boolean matchMetric(final Meter.Id metric) {
        return matchMetricName(metric.getName());
    }

    private boolean matchMetricName(final String metricName) {
        return allowedMetrics.stream().anyMatch(e->e.matches(metricName));
    }

    private Meter.Id mapMetric(final Meter.Id metric) {
        final List<Tag> tags = dimensions.entrySet().stream().map(e -> Tag.of(e.getKey(), e.getValue())).collect(Collectors.toList());
        return metric.withTags(tags);
    }

}
