package de.otto.edison.metrics.cloudwatch;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.config.MeterFilter;

import java.util.List;

public class CloudWatchMetricFilter {

    private final MeterRegistry meterRegistry;
    private final List<String> allowedMetrics;

    public CloudWatchMetricFilter(final MeterRegistry meterRegistry, final List<String> allowedMetrics) {
        this.meterRegistry = meterRegistry;
        this.allowedMetrics = allowedMetrics;
        registerFilter(meterRegistry);
    }


    private void registerFilter(final MeterRegistry meterRegistry) {
        final MeterRegistry.Config config = meterRegistry.config();
        //allowedMetrics.forEach(e->{config.meterFilter(MeterFilter.acceptNameStartsWith(e));});
        allowedMetrics.forEach(e->{config.meterFilter(MeterFilter.accept(p->e.matches(p.getName())));});
    }

}
