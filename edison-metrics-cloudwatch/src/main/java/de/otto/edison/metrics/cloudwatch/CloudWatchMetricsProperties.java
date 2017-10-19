package de.otto.edison.metrics.cloudwatch;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import javax.validation.constraints.NotEmpty;
import java.util.List;

@Validated
@ConfigurationProperties(prefix = "edison.metrics.cloudWatch")
public class CloudWatchMetricsProperties {

    private @NotEmpty List<String> allowedMetrics;

    private @NotEmpty String namespace;

    private boolean enabled;

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(final boolean enabled) {
        this.enabled = enabled;
    }

    public List<String> getAllowedMetrics() {
        return allowedMetrics;
    }

    public void setAllowedMetrics(final List<String> allowedMetrics) {
        this.allowedMetrics = allowedMetrics;
    }

    public String getNamespace() {
        return namespace;
    }

    public void setNamespace(final String namespace) {
        this.namespace = namespace;
    }
}
