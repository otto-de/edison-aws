package de.otto.edison.metrics.cloudwatch;

import software.amazon.awssdk.services.cloudwatch.model.Dimension;

import java.util.HashSet;
import java.util.Map;

class DimensionsConverter {
    static HashSet<Dimension> convertDimensions(final Map<String, String> dimensionsProperty) {
        final HashSet<Dimension> dimensions = new HashSet<>();

        dimensionsProperty.forEach((key, value) -> dimensions.add(Dimension.builder().name(key).value(value).build()));

        return dimensions;
    }
}
