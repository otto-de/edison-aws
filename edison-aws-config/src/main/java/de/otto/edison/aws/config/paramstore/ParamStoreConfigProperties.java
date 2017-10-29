package de.otto.edison.aws.config.paramstore;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "edison.aws.config.paramstore")
public class ParamStoreConfigProperties {
    private boolean enabled;
    private String path;


    public ParamStoreConfigProperties() {
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }
}
