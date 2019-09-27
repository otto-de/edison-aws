package de.otto.edison.metrics.cloudwatch.controller;

import io.micrometer.core.instrument.Meter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Statistic;
import org.springframework.boot.actuate.endpoint.annotation.Endpoint;
import org.springframework.boot.actuate.endpoint.annotation.ReadOperation;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.stream.Collectors;

import static java.util.Collections.singletonList;

@Endpoint(id = "metricvalues")
public class MetricValuesEndpoint {

    private final MeterRegistry registry;

    public MetricValuesEndpoint(final MeterRegistry registry) {
        this.registry = registry;
    }

    @ReadOperation
    public List<MetricResponse> metricValues() {

        Collection<Meter> meters = registry.getMeters();
        if (meters.isEmpty()) {
            return null;
        }
        final List<MetricResponse> result = new ArrayList<MetricResponse>();
        for (Meter meter : meters) {
            Map<Statistic, Double> samples = getSamples(singletonList(meter));
            Map<String, Set<String>> availableTags = getAvailableTags(singletonList(meter));
            Meter.Id meterId = meter.getId();
            result.add(new MetricResponse(meterId.getName(), meterId.getDescription(), meterId.getBaseUnit(),
                asList(samples, Sample::new), asList(availableTags, AvailableTag::new)));
        }
        return result;

    }

    private Map<Statistic, Double> getSamples(Collection<Meter> meters) {
        Map<Statistic, Double> samples = new LinkedHashMap<>();
        meters.forEach((meter) -> mergeMeasurements(samples, meter));
        return samples;
    }

    private void mergeMeasurements(Map<Statistic, Double> samples, Meter meter) {
        meter.measure().forEach((measurement) -> samples.merge(measurement.getStatistic(), measurement.getValue(),
            mergeFunction(measurement.getStatistic())));
    }

    private <K, V, T> List<T> asList(Map<K, V> map, BiFunction<K, V, T> mapper) {
        return map.entrySet().stream().map((entry) -> mapper.apply(entry.getKey(), entry.getValue()))
            .collect(Collectors.toList());
    }

    private BiFunction<Double, Double, Double> mergeFunction(Statistic statistic) {
        return Statistic.MAX.equals(statistic) ? Double::max : Double::sum;
    }

    private Map<String, Set<String>> getAvailableTags(Collection<Meter> meters) {
        Map<String, Set<String>> availableTags = new HashMap<>();
        meters.forEach((meter) -> mergeAvailableTags(availableTags, meter));
        return availableTags;
    }

    private void mergeAvailableTags(Map<String, Set<String>> availableTags, Meter meter) {
        meter.getId().getTags().forEach((tag) -> {
            Set<String> value = Collections.singleton(tag.getValue());
            availableTags.merge(tag.getKey(), value, this::merge);
        });
    }

    private <T> Set<T> merge(Set<T> set1, Set<T> set2) {
        Set<T> result = new HashSet<>(set1.size() + set2.size());
        result.addAll(set1);
        result.addAll(set2);
        return result;
    }

    /**
     * Response payload for a metric name selector.
     */
    public static final class MetricResponse {

        private final String name;

        private final String description;

        private final String baseUnit;

        private final List<Sample> measurements;

        private final List<AvailableTag> availableTags;

        MetricResponse(String name, String description, String baseUnit, List<Sample> measurements,
                       List<AvailableTag> availableTags) {
            this.name = name;
            this.description = description;
            this.baseUnit = baseUnit;
            this.measurements = measurements;
            this.availableTags = availableTags;
        }

        public String getName() {
            return this.name;
        }

        public String getDescription() {
            return this.description;
        }

        public String getBaseUnit() {
            return this.baseUnit;
        }

        public List<Sample> getMeasurements() {
            return this.measurements;
        }

        public List<AvailableTag> getAvailableTags() {
            return this.availableTags;
        }

    }

    /**
     * A set of tags for further dimensional drilldown and their potential values.
     */
    public static final class AvailableTag {

        private final String tag;

        private final Set<String> values;

        AvailableTag(String tag, Set<String> values) {
            this.tag = tag;
            this.values = values;
        }

        public String getTag() {
            return this.tag;
        }

        public Set<String> getValues() {
            return this.values;
        }

    }

    /**
     * A measurement sample combining a {@link Statistic statistic} and a value.
     */
    public static final class Sample {

        private final Statistic statistic;

        private final Double value;

        Sample(Statistic statistic, Double value) {
            this.statistic = statistic;
            this.value = value;
        }

        public Statistic getStatistic() {
            return this.statistic;
        }

        public Double getValue() {
            return this.value;
        }

        @Override
        public String toString() {
            return "MeasurementSample{" + "statistic=" + this.statistic + ", value=" + this.value + '}';
        }

    }
}
