package de.otto.edison.metrics.cloudwatch;

import com.codahale.metrics.Counter;
import com.codahale.metrics.Counting;
import com.codahale.metrics.Gauge;
import com.codahale.metrics.Histogram;
import com.codahale.metrics.Meter;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.ScheduledReporter;
import com.codahale.metrics.Timer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import software.amazon.awssdk.services.cloudwatch.CloudWatchAsyncClient;
import software.amazon.awssdk.services.cloudwatch.model.Dimension;
import software.amazon.awssdk.services.cloudwatch.model.MetricDatum;
import software.amazon.awssdk.services.cloudwatch.model.PutMetricDataRequest;

import java.time.Clock;
import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.SortedMap;

import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static java.util.concurrent.TimeUnit.SECONDS;

public class CloudWatchMetricsReporter extends ScheduledReporter {

    private static final Logger LOG = LoggerFactory.getLogger(CloudWatchMetricsReporter.class);

    private final CloudWatchAsyncClient cloudWatchClient;
    private final String namespace;
    private final Collection<Dimension> dimensions;
    private Clock clock;

    public CloudWatchMetricsReporter(final MetricRegistry registry,
                                     final List<String> allowedMetrics,
                                     final String namespace,
                                     final Collection<Dimension> dimensions,
                                     final CloudWatchAsyncClient cloudWatchClient) {
        super(
                registry,
                "cloudWatch-reporter",
                (name, metric) -> shouldReportToCloudWatch(name, allowedMetrics), SECONDS, MILLISECONDS
        );
        this.cloudWatchClient = cloudWatchClient;
        this.dimensions = dimensions;
        this.namespace = namespace;
    }

    protected static boolean shouldReportToCloudWatch(final String name, final List<String> allowedMetrics) {
        return allowedMetrics.stream().anyMatch(name::matches);
    }

    @Override
    public void report(final SortedMap<String, Gauge> gauges, final SortedMap<String, Counter> counters, final SortedMap<String, Histogram> histograms, final SortedMap<String, Meter> meters, final SortedMap<String, Timer> timers) {
        gauges.forEach(this::reportGauge);
        counters.forEach(this::reportCounter);
        histograms.forEach(this::reportHistogram);
        meters.forEach(this::reportMeter);
        timers.forEach(this::reportTimer);
    }

    public void setClock(final Clock clock) {
        this.clock = clock;
    }

    private void reportGauge(final String name, final Gauge<Number> gauge) {
        reportToCloudWatch(name, gauge.getValue().doubleValue());
    }

    private void reportCounter(final String name, final Counting counter) {
        reportToCloudWatch(name, counter.getCount());
    }

    private void reportHistogram(final String name, final Histogram histogram) {
        reportToCloudWatch(name, histogram.getCount());
    }

    private void reportMeter(final String name, final Meter meter) {
        reportToCloudWatch(name, meter.getCount());
        reportToCloudWatch(name + ".mean", meter.getMeanRate());
    }

    private void reportTimer(final String name, final Timer timer) {
        reportToCloudWatch(name, timer.getCount());
        reportToCloudWatch(name + ".mean", timer.getMeanRate());
    }

    private void reportToCloudWatch(final String name, final double value) {
        cloudWatchClient.putMetricData(PutMetricDataRequest.builder()
                .namespace(namespace)
                .metricData(MetricDatum.builder()
                        .metricName(name)
                        .dimensions(dimensions)
                        .value(value)
                        .timestamp(clock != null ? clock.instant() : Instant.now())
                        .build())
                .build());

        LOG.debug("sending metric to cloudWatch: {} : {}", name, value);
    }
}
