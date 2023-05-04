package de.otto.edison.metrics.cloudwatch;

import io.micrometer.core.instrument.MeterRegistry;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;

import java.lang.management.MemoryMXBean;
import java.lang.management.MemoryUsage;

import static java.util.Collections.emptyList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.openMocks;

public class MemoryUsageMetricTest {

    @Mock
    private MemoryMXBean mxBean;

    @Mock
    private MeterRegistry meterRegistry;

    private MemoryUsageMetric memoryUsageMetric;

    private AutoCloseable mocks;

    @BeforeEach
    public void setUp() {
        mocks = openMocks(this);
        memoryUsageMetric = new MemoryUsageMetric(mxBean, emptyList(), meterRegistry);
    }

    @AfterEach
    public void tearDown() throws Exception {
        mocks.close();
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