package de.otto.edison.metrics.cloudwatch;

import com.amazonaws.services.cloudwatch.AmazonCloudWatchAsync;
import com.amazonaws.services.cloudwatch.model.Dimension;
import com.amazonaws.services.cloudwatch.model.MetricDatum;
import com.amazonaws.services.cloudwatch.model.PutMetricDataRequest;
import com.codahale.metrics.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Date;
import java.util.List;
import java.util.SortedMap;

import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static java.util.concurrent.TimeUnit.SECONDS;

public class CloudWatchMetricsReporter extends ScheduledReporter {

    private static final Logger LOG = LoggerFactory.getLogger(CloudWatchMetricsReporter.class);

    private final AmazonCloudWatchAsync cloudWatchClient;
    private final String namespace;

    public CloudWatchMetricsReporter(final MetricRegistry registry, final List<String> allowedMetrics, final String namespace,
                                     final AmazonCloudWatchAsync cloudWatchClient) {
        super(registry, "cloudWatch-reporter", (name, metric) -> shouldReportToCloudWatch(name, allowedMetrics), SECONDS,
                MILLISECONDS);
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
        cloudWatchClient.putMetricDataAsync(new PutMetricDataRequest()
                .withNamespace(namespace)
                .withMetricData(new MetricDatum()
                        .withDimensions(new Dimension()
                                .withName("default-dimension")
                                .withValue("total-count"))
                        .withMetricName(name)
                        .withValue(value)
                        .withTimestamp(new Date())
                ));

        LOG.info("sending metric to cloudWatch: " + name + " : " + value);
    }
}
