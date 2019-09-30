package de.otto.edison.metrics.cloudwatch;

import com.google.common.collect.Streams;
import io.micrometer.core.instrument.Measurement;
import io.micrometer.core.instrument.Meter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Tag;
import io.micrometer.core.instrument.config.MeterFilter;
import io.micrometer.core.instrument.internal.DefaultMeter;
import org.apache.commons.lang3.StringUtils;
import software.amazon.awssdk.services.cloudwatch.model.StandardUnit;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static software.amazon.awssdk.services.cloudwatch.model.StandardUnit.UNKNOWN_TO_SDK_VERSION;

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

    protected Meter filter(final Meter meter) {
        if (matchMetric(meter.getId()))  {
            return new DefaultMeter(mapMetric(meter.getId()), meter.getId().getType(), getDefaultMeasurementWithoutStatistic(meter));
        }
        return null;
    }

    private List<Measurement> getDefaultMeasurementWithoutStatistic(final Meter meter) {
        return Streams.stream(meter.measure()).map(e->new Measurement(() -> e.getValue(), null)).collect(Collectors.toList());
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
        return allowedMetrics.stream().anyMatch(metricName::matches);
    }

    private Meter.Id mapMetric(Meter.Id metric) {
        final List<Tag> configuredCloudwatchTags = dimensions.entrySet().stream().map(e -> Tag.of(e.getKey(), e.getValue())).collect(Collectors.toList());
        final List<Tag> existingTags = metric.getTags();
        if (isNull(metric.getBaseUnit()) || !isValidUnit(metric.getBaseUnit())) {
            metric = metric.withBaseUnit("None");
        }
        if (nonNull(existingTags) && !existingTags.isEmpty()) {
            final String newName = metric.getName() + "." + String.join(".", existingTags.stream().map(e->toCamelCase(e.getValue(), true)).collect(Collectors.toList()));
            metric = metric.withName(newName);
        }
        return metric.replaceTags(configuredCloudwatchTags);
    }

    private boolean isValidUnit(final String baseUnit) {
        return StandardUnit.fromValue(baseUnit) != UNKNOWN_TO_SDK_VERSION;
    }

    private String toCamelCase(final String value, final boolean startWithLowerCase) {
        if (nonNull(value)) {
            String[] strings = StringUtils.split(value.toLowerCase(), " ");
            if (strings != null) {
                for (int i = startWithLowerCase ? 1 : 0; i < strings.length; i++) {
                    strings[i] = StringUtils.capitalize(strings[i]);
                }
                return String.join("", strings);
            }
        }
        return value;
    }

}
