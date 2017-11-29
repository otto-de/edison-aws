package de.otto.edison.aws.dynamo.jobs;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "edison.aws.dynamo.jobs")
public class DynamoJobRepoProperties {

    private boolean enabled;
    private String tableName;

    public DynamoJobRepoProperties() {}

    public DynamoJobRepoProperties(boolean enabled, String tableName) {
        this.enabled = enabled;
        this.tableName = tableName;
    }

    public String getTableName() {
        return tableName;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }
}
