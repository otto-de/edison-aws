package de.otto.edison.aws.dynamo.jobs;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "edison.aws.dynamo.jobs")
public class DynamoDbJobRepoProperties {

    private boolean enabled;
    private String jobInfoTableName;
    private String jobMetaTableName;

    public DynamoDbJobRepoProperties() {}

    public DynamoDbJobRepoProperties(boolean enabled, String jobInfoTableName, String jobMetaTableName) {
        this.enabled = enabled;
        this.jobInfoTableName = jobInfoTableName;
        this.jobMetaTableName = jobMetaTableName;
    }

    public String getJobInfoTableName() {
        return jobInfoTableName;
    }

    public void setJobInfoTableName(String jobInfoTableName) {
        this.jobInfoTableName = jobInfoTableName;
    }

    public String getJobMetaTableName() {
        return jobMetaTableName;
    }

    public void setJobMetaTableName(String jobMetaTableName) {
        this.jobMetaTableName = jobMetaTableName;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }
}
