package de.otto.edison.metrics.cloudwatch;

import com.codahale.metrics.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import software.amazon.awssdk.services.cloudwatch.CloudWatchAsyncClient;
import software.amazon.awssdk.services.cloudwatch.model.MetricDatum;
import software.amazon.awssdk.services.cloudwatch.model.PutMetricDataRequest;

import java.util.List;
import java.util.SortedMap;

import static java.time.Instant.now;
import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static java.util.concurrent.TimeUnit.SECONDS;

public class CloudWatchMetricsReporter extends ScheduledReporter {

    private static final Logger LOG = LoggerFactory.getLogger(CloudWatchMetricsReporter.class);

    private final CloudWatchAsyncClient cloudWatchClient;
    private final String namespace;

    public CloudWatchMetricsReporter(final MetricRegistry registry,
                                     final List<String> allowedMetrics,
                                     final String namespace,
                                     final CloudWatchAsyncClient cloudWatchClient) {
        super(
                registry,
                "cloudWatch-reporter",
                (name, metric) -> shouldReportToCloudWatch(name, allowedMetrics), SECONDS, MILLISECONDS
        );
        this.cloudWatchClient = cloudWatchClient;
        this.namespace = namespace;
    }

    private static boolean shouldReportToCloudWatch(final String name, final List<String> allowedMetrics) {
        return allowedMetrics.contains(name);
    }

    @Override
    public void report(final SortedMap<String, Gauge> gauges, final SortedMap<String, Counter> counters, final SortedMap<String, Histogram> histograms, final SortedMap<String, Meter> meters, final SortedMap<String, Timer> timers) {
        gauges.forEach(this::reportGauge);
        counters.forEach(this::reportCounter);
        histograms.forEach(this::reportCounter);
        meters.forEach(this::reportCounter);
        timers.forEach(this::reportCounter);
    }

    private void reportGauge(final String name, final Gauge<Number> gauge) {
        reportToCloudWatch(name, gauge.getValue().doubleValue());
    }

    private void reportCounter(final String name, final Counting counter) {
        reportToCloudWatch(name, counter.getCount());
    }

    private void reportToCloudWatch(final String name, final double value) {
        cloudWatchClient.putMetricData(PutMetricDataRequest.builder()
                .namespace(namespace)
                .metricData(MetricDatum.builder()
                        .metricName(name)
                        .value(value)
                        .timestamp(now())
                        .build())
                .build()
        );

        LOG.info("sending metric to cloudWatch: " + name + " : " + value);
    }
}
