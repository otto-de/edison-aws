package de.otto.edison.metrics.cloudwatch;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import jakarta.validation.constraints.NotEmpty;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Validated
@ConfigurationProperties(prefix = "edison.aws.metrics.cloudwatch")
public class CloudWatchMetricsProperties {

    private @NotEmpty List<String> allowedmetrics;

    private @NotEmpty String namespace;

    private boolean enabled;

    private Map<String, String> dimensions = new HashMap<>();

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(final boolean enabled) {
        this.enabled = enabled;
    }

    public List<String> getAllowedmetrics() {
        return allowedmetrics;
    }

    public void setAllowedmetrics(final List<String> allowedmetrics) {
        this.allowedmetrics = allowedmetrics;
    }

    public String getNamespace() {
        return namespace;
    }

    public void setNamespace(final String namespace) { this.namespace = namespace; }

    public Map<String, String> getDimensions() {
        return dimensions;
    }

    public void setDimensions(final Map<String, String> dimensions) { this.dimensions = dimensions; }
}
