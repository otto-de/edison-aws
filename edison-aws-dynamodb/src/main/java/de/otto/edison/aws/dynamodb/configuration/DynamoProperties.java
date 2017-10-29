package de.otto.edison.aws.dynamodb.configuration;

import org.springframework.boot.context.properties.ConfigurationProperties;
import software.amazon.awssdk.regions.Region;

/**
 * Properties used to configure DynamoDB clients.
 *
 * @since 1.0.0
 */
@ConfigurationProperties(prefix = "edison.aws.dynamo")
public class DynamoProperties {

    private String region = Region.EU_CENTRAL_1.value();
    private String endpoint = "http://localhost:8000/";
    private String profileName = "test";
    private String tableNamePrefix = "test";
    private String tableNameSeparator = "-";

    public String getEndpoint() {
        return endpoint;
    }

    public String getRegion() {
        return region;
    }

    public void setRegion(String region) {
        this.region = region;
    }

    public void setEndpoint(final String endpoint) {
        this.endpoint = endpoint;
    }

    public String getProfileName() {
        return profileName;
    }

    public void setProfileName(final String profileName) {
        this.profileName = profileName;
    }

    public String getTableNamePrefix() {
        return tableNamePrefix;
    }

    public void setTableNamePrefix(final String tableNamePrefix) {
        this.tableNamePrefix = tableNamePrefix;
    }

    public String getTableNameSeparator() {
        return tableNameSeparator;
    }

    public void setTableNameSeparator(final String tableNameSeparator) {
        this.tableNameSeparator = tableNameSeparator;
    }

    @Override
    public String toString() {
        return "DynamoProperties{" +
                "region='" + region + '\'' +
                ", endpoint='" + endpoint + '\'' +
                ", profileName='" + profileName + '\'' +
                ", tableNamePrefix='" + tableNamePrefix + '\'' +
                ", tableNameSeparator='" + tableNameSeparator + '\'' +
                '}';
    }
}
