package de.otto.edison.metrics.cloudwatch;

import com.codahale.metrics.Counter;
import com.codahale.metrics.MetricRegistry;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import software.amazon.awssdk.services.cloudwatch.CloudWatchAsyncClient;
import software.amazon.awssdk.services.cloudwatch.model.MetricDatum;
import software.amazon.awssdk.services.cloudwatch.model.PutMetricDataRequest;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;

import static java.util.Collections.singletonList;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.MockitoAnnotations.initMocks;

public class CloudWatchMetricsReporterTest {
    @Mock
    private CloudWatchAsyncClient cloudWatchClient;

    private CloudWatchMetricsReporter testee;

    @Before
    public void setUp() throws Exception {
        initMocks(this);
    }

    @Test
    public void shouldReportMetrics() throws Exception {
        // given
        final Clock clock = Clock.fixed(Instant.now(), ZoneId.systemDefault());
        final MetricRegistry registry = new MetricRegistry();
        registry.register("cpu", new Counter());
        registry.register("ram", new Counter());
        testee = new CloudWatchMetricsReporter(registry, singletonList("cpu"), "metrics", cloudWatchClient);
        testee.setClock(clock);

        // when
        testee.report();

        // then
        verify(cloudWatchClient).putMetricData(PutMetricDataRequest.builder()
                .namespace("metrics")
                .metricData(MetricDatum.builder()
                        .metricName("cpu")
                        .value(0.0D)
                        .timestamp(clock.instant())
                        .build())
                .build());
        verifyNoMoreInteractions(cloudWatchClient);
    }

    @Test
    public void shouldReportToCloudWatch() {
        assertTrue(CloudWatchMetricsReporter.shouldReportToCloudWatch("heap", singletonList("heap.*")));
        assertTrue(CloudWatchMetricsReporter.shouldReportToCloudWatch("heap.used", singletonList("heap.*")));
    }

}
