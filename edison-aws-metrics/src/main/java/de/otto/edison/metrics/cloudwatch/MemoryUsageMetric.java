package de.otto.edison.metrics.cloudwatch;

import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.binder.BaseUnits;

import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.MemoryPoolMXBean;
import java.lang.management.MemoryUsage;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * A set of gauges for JVM memory usage, including stats on heap vs. non-heap memory, plus
 * GC-specific memory pools.
 */
public class MemoryUsageMetric {

    private final MemoryMXBean mxBean;
    private final List<MemoryPoolMXBean> memoryPools;
    private final MeterRegistry meterRegistry;

    public MemoryUsageMetric(MeterRegistry meterRegistry) {
        this(ManagementFactory.getMemoryMXBean(),
             ManagementFactory.getMemoryPoolMXBeans(),
             meterRegistry);
    }

    public MemoryUsageMetric(MemoryMXBean mxBean,
                             Collection<MemoryPoolMXBean> memoryPools,
                             MeterRegistry meterRegistry) {
        this.mxBean = mxBean;
        this.memoryPools = new ArrayList<MemoryPoolMXBean>(memoryPools);
        this.meterRegistry = meterRegistry;
        registerMetrics(meterRegistry);
    }

    private void registerMetrics(MeterRegistry meterRegistry) {
        Gauge.builder("memory.heap.usage", this, MemoryUsageMetric::getHeapUsage)
                .baseUnit("None")
                .register(meterRegistry);

        Gauge.builder("memory.heap.used", this, MemoryUsageMetric::getHeapUsed)
                .baseUnit("Bytes")
                .register(meterRegistry);

        Gauge.builder("memory.heap.max", this, MemoryUsageMetric::getHeapMax)
                .baseUnit("Bytes")
                .register(meterRegistry);
    }

    double getHeapUsage() {
        final MemoryUsage usage = mxBean.getHeapMemoryUsage();
        final double memoryUsed =  usage.getUsed();
        final double memoryMax = usage.getMax();
        return memoryUsed / memoryMax;
    }

    double getHeapUsed() {
        return mxBean.getHeapMemoryUsage().getUsed();
    }

    double getHeapMax() {
        return mxBean.getHeapMemoryUsage().getMax();
    }
}