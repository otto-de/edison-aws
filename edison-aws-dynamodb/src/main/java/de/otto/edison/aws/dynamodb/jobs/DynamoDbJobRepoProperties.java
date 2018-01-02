package de.otto.edison.aws.dynamodb.jobs;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "edison.aws.dynamodb.jobs")
public class DynamoDbJobRepoProperties {

    private String jobInfoTableName;
    private String jobMetaTableName;

    public DynamoDbJobRepoProperties() {}

    public DynamoDbJobRepoProperties(String jobInfoTableName, String jobMetaTableName) {
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

}
