package de.otto.edison.metrics.cloudwatch;

import org.junit.Test;
import software.amazon.awssdk.services.cloudwatch.model.Dimension;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import static de.otto.edison.metrics.cloudwatch.DimensionsConverter.convertDimensions;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;

public class DimensionsConverterTest {

    @Test
    public void shouldConvertDimensionsFromMapToHashSet() {
        // given
        final Map<String, String> dimensionsInPropertyFile = new HashMap<>();
        dimensionsInPropertyFile.put("environment", "local");
        dimensionsInPropertyFile.put("anotherDimension", "anotherValue");

        // when
        final Dimension expectedDimension1 = Dimension.builder().name("environment").value("local").build();
        final Dimension expectedDimension2 = Dimension.builder().name("anotherDimension").value("anotherValue").build();

        final HashSet<Dimension> response = convertDimensions(dimensionsInPropertyFile);

        // then
        assertThat(response, containsInAnyOrder(expectedDimension1, expectedDimension2));
    }

}