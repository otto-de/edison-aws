package de.otto.edison.metrics.cloudwatch;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import io.micrometer.core.instrument.Measurement;
import io.micrometer.core.instrument.Meter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Statistic;
import io.micrometer.core.instrument.Tags;
import io.micrometer.core.instrument.internal.DefaultMeter;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import java.util.Collections;
import java.util.List;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.MockitoAnnotations.initMocks;

public class CloudWatchMetricFilterTest {

    @Mock
    private MeterRegistry meterRegistry;
    private CloudWatchMetricFilter cloudWatchMetricFilter;

    @Before
    public void setUp() throws Exception {
        initMocks(this);
        final CloudWatchMetricsProperties cloudWatchMetricsProperties = new CloudWatchMetricsProperties();
        cloudWatchMetricsProperties.setDimensions(ImmutableMap.of("By_Environment","live"));
        cloudWatchMetricsProperties.setAllowedmetrics(ImmutableList.of("jvm.memory.*"));
        cloudWatchMetricFilter = new CloudWatchMetricFilter((cloudWatchMetricsProperties));
    }

    @Test
    public void shouldCreateCloudwatchMetric() {
        // given
        final Meter.Id meterId = new Meter.Id("jvm.memory.max", Tags.empty(), null, null, null);
        final Measurement measurement = new Measurement(() -> 1.0D, Statistic.COUNT);
        final List<Measurement> measurementList = Collections.singletonList(measurement);
        final Meter metric =  new DefaultMeter(meterId, Meter.Type.COUNTER, measurementList);

        // when
        final Meter filterMetric = cloudWatchMetricFilter.filter(metric);

        // then
        assertThat(filterMetric.getId(), is(new Meter.Id("jvm.memory.max", Tags.of("By_Environment" , "live"), null, null, null)));
    }

    @Test
    public void shouldCreateCloudwatchMetricWithTags() {
        // given
        final Tags tags = Tags.of("area", "heap", "id", "PS Eden Space");
        final Meter.Id meterId = new Meter.Id("jvm.memory.max", tags, null, null, null);
        final Measurement measurement = new Measurement(() -> 1.0D, Statistic.COUNT);
        final List<Measurement> measurementList = Collections.singletonList(measurement);
        final Meter metric = new DefaultMeter(meterId, Meter.Type.COUNTER, measurementList);

        // when
        final Meter filterMetric = cloudWatchMetricFilter.filter(metric);

        // then
        assertThat(filterMetric.getId(), is(new Meter.Id("jvm.memory.max.heap.psEdenSpace", Tags.of("By_Environment" , "live"), null, null, null)));
    }

}