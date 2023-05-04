package de.otto.edison.metrics.cloudwatch;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import io.micrometer.core.instrument.Measurement;
import io.micrometer.core.instrument.Meter;
import io.micrometer.core.instrument.Statistic;
import io.micrometer.core.instrument.Tags;
import io.micrometer.core.instrument.internal.DefaultMeter;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.List;

import static java.util.Collections.singletonList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.samePropertyValuesAs;
import static org.mockito.MockitoAnnotations.openMocks;

@SuppressWarnings("ConstantConditions") // passing nulls to @NonNulls here
public class CloudWatchMetricFilterTest {

    private CloudWatchMetricFilter cloudWatchMetricFilter;
    private AutoCloseable mocks;

    @BeforeEach
    public void setUp() {
        mocks = openMocks(this);
        final CloudWatchMetricsProperties cloudWatchMetricsProperties = new CloudWatchMetricsProperties();
        cloudWatchMetricsProperties.setDimensions(ImmutableMap.of("By_Environment","live"));
        cloudWatchMetricsProperties.setAllowedmetrics(ImmutableList.of("jvm.memory.*","logback.events.*"));
        cloudWatchMetricFilter = new CloudWatchMetricFilter((cloudWatchMetricsProperties));
    }

    @AfterEach
    public void tearDown() throws Exception {
        mocks.close();
    }

    @Test
    public void shouldCreateCloudwatchMetric() {
        // given
        final Meter.Id meterId = new Meter.Id("jvm.memory.max", Tags.empty(), null, null, null);
        final Measurement measurement = new Measurement(() -> 1.0D, null);
        final List<Measurement> measurementList = Collections.singletonList(measurement);
        final Meter metric =  new DefaultMeter(meterId, Meter.Type.COUNTER, measurementList);

        // when
        final Meter filterMetric = cloudWatchMetricFilter.filter(metric);

        // then
        assertThat(filterMetric.getId(), is(new Meter.Id("jvm.memory.max", Tags.of("By_Environment" , "live"), null, null, null)));
        assertThat(filterMetric.getId().getBaseUnit(), is("None"));
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

    @Test
    public void shouldCreateCloudwatchMetricWithUnknownUnit() {
        // given
        final Meter.Id meterId = new Meter.Id("logback.events.count", Tags.empty(), "events", null, null);
        final Measurement measurement = new Measurement(() -> 1.0D, Statistic.COUNT);
        final List<Measurement> measurementList = Collections.singletonList(measurement);
        final Meter metric =  new DefaultMeter(meterId, Meter.Type.COUNTER, measurementList);

        // when
        final Meter filterMetric = cloudWatchMetricFilter.filter(metric);

        // then
        assertThat(filterMetric.getId(), is(new Meter.Id("logback.events.count", Tags.of("By_Environment" , "live"), null, null, null)));
        assertThat(filterMetric.getId().getBaseUnit(), is("None"));
    }

    @Test
    public void shouldCreateCloudwatchMetricWithStatistic() {
        // given
        final Meter.Id meterId = new Meter.Id("jvm.memory.max", Tags.empty(), "Bytes", null, null);
        final Measurement measurement = new Measurement(() -> 1.0D, Statistic.COUNT);
        final List<Measurement> measurementList = Collections.singletonList(measurement);
        final Meter metric =  new DefaultMeter(meterId, Meter.Type.COUNTER, measurementList);

        // when
        final Meter filterMetric = cloudWatchMetricFilter.filter(metric);

        // then
        assertThat(filterMetric.getId(), is(new Meter.Id("jvm.memory.max", Tags.of("By_Environment" , "live"), null, null, null)));
        assertThat(filterMetric.getId().getBaseUnit(), is("Bytes"));
        assertThat(filterMetric.measure().iterator().next(), is(samePropertyValuesAs(singletonList(new Measurement(() -> 1.0D, Statistic.COUNT)).get(0))));
    }
}