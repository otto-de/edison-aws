package de.otto.edison.aws.config.paramstore;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "edison.aws.config.paramstore")
public class ParamStoreConfigProperties {
    private boolean enabled;
    private String path;
    private boolean addWithLowestPrecedence;

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

    public boolean isAddWithLowestPrecedence() {
        return addWithLowestPrecedence;
    }

    public void setAddWithLowestPrecedence(final boolean addWithLowestPrecedence) {
        this.addWithLowestPrecedence = addWithLowestPrecedence;
    }
}
