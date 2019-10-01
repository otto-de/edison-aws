package de.otto.edison.metrics.cloudwatch;

import io.micrometer.core.instrument.MeterRegistry;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import java.lang.management.MemoryMXBean;
import java.lang.management.MemoryUsage;

import static java.util.Collections.emptyList;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.*;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

public class MemoryUsageMetricTest {

    @Mock
    private MemoryMXBean mxBean;

    @Mock
    private MeterRegistry meterRegistry;

    private MemoryUsageMetric memoryUsageMetric;

    @Before
    public void setUp() {
        initMocks(this);
        memoryUsageMetric = new MemoryUsageMetric(mxBean, emptyList(), meterRegistry);
    }

    @Test
    public void shouldCalculateHeapUsage() {
        // given
        when(mxBean.getHeapMemoryUsage()).thenReturn(new MemoryUsage(10L, 100L, 200L, 400L));

        // when
        final double result = memoryUsageMetric.getHeapUsage();

        // then
        assertThat(result, is(0.25D));

    }

}