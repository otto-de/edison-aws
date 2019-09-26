package de.otto.edison.metrics.cloudwatch;

import io.micrometer.core.instrument.Measurement;
import io.micrometer.core.instrument.Meter;

public class FilteredMeter implements Meter {

    private final Id id;
    private final Iterable<Measurement> measure;


    public FilteredMeter(final Id id, final Iterable<Measurement> measure) {
        this.id = id;
        this.measure = measure;
    }

    @Override
    public Id getId() {
        return id;
    }

    @Override
    public Iterable<Measurement> measure() {
        return measure;
    }

}
