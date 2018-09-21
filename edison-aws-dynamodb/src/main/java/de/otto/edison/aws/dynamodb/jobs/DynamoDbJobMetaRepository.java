package de.otto.edison.aws.dynamodb.jobs;

import de.otto.edison.jobs.domain.JobMeta;
import de.otto.edison.jobs.repository.JobMetaRepository;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.*;
import software.amazon.awssdk.utils.ImmutableMap;

import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static java.util.Collections.emptyMap;
import static java.util.stream.Collectors.toMap;

public class DynamoDbJobMetaRepository implements JobMetaRepository {

    static final String JOB_TYPE = "JOB_TYPE";
    static final String KEY_RUNNING = "running";
    static final String KEY_DISABLED = "disabled";

    private final DynamoDbClient dynamoDBClient;
    private final String jobMetaTableName;

    public DynamoDbJobMetaRepository(DynamoDbClient dynamoDBClient, DynamoDbJobRepoProperties properties) {
        this.dynamoDBClient = dynamoDBClient;
        this.jobMetaTableName = properties.getJobMetaTableName();
    }

    @Override
    public JobMeta getJobMeta(String jobType) {
        GetItemResponse response = getItem(jobType);
        Map<String, AttributeValue> item = response.item();
        if (item != null) {
            final Map<String, String> meta = item.keySet()
                    .stream()
                    .filter(key -> !(key.startsWith(KEY_RUNNING) || key.startsWith(KEY_DISABLED) || key.startsWith(JOB_TYPE)))
                    .collect(toMap(
                            key -> key,
                            key -> item.get(key).s()
                    ));
            final boolean isRunning = item.containsKey(KEY_RUNNING);
            final boolean isDisabled = item.containsKey(KEY_DISABLED);
            String comment = null;
            if (isDisabled && item.get(KEY_DISABLED) != null) {
                comment = item.get(KEY_DISABLED).s();
            }
            return new JobMeta(jobType, isRunning, isDisabled, comment, meta);
        } else {
            return new JobMeta(jobType, false, false, "", emptyMap());
        }

    }

    @Override
    public boolean createValue(String jobType, String key, String value) {
        UpdateItemRequest request = UpdateItemRequest.builder()
                .tableName(jobMetaTableName)
                .key(ImmutableMap.of(JOB_TYPE, AttributeValue.builder().s(jobType).build()))
                .updateExpression("SET " + key + " = :value")
                .conditionExpression("attribute_not_exists(" + key + ")")
                .expressionAttributeValues(ImmutableMap.of(":value", AttributeValue.builder().s(value).build()))
                .returnValues(ReturnValue.ALL_NEW)
                .build();

        try {
            dynamoDBClient.updateItem(request);
            return true;
        } catch (ConditionalCheckFailedException e) {
            return false;
        }

    }

    @Override
    public boolean setRunningJob(String jobType, String jobId) {
        return createValue(jobType, KEY_RUNNING, jobId);
    }

    @Override
    public String getRunningJob(String jobType) {
        return getValue(jobType, KEY_RUNNING);
    }

    @Override
    public void clearRunningJob(String jobType) {
        setValue(jobType, KEY_RUNNING, null);
    }

    @Override
    public void disable(String jobType, String comment) {
        setValue(jobType, KEY_DISABLED, comment != null ? comment : "");
    }

    @Override
    public void enable(String jobType) {
        setValue(jobType, KEY_DISABLED, null);
    }

    @Override
    public String setValue(String jobType, String key, String value) {
        UpdateItemRequest request;
        if (value == null) {
            request = removeKeyRequest(jobType, key);
        } else {
            request = updateKeyRequest(jobType, key, value);
        }
        UpdateItemResponse updateItemResponse = dynamoDBClient.updateItem(request);
        if (updateItemResponse.attributes() != null) {
            return updateItemResponse.attributes().getOrDefault(key, AttributeValue.builder().build()).s();
        } else {
            return null;
        }

    }

    private UpdateItemRequest updateKeyRequest(String jobType, String key, String value) {
        AttributeValue attributeValue;
        if (value.isEmpty()) {
            //dynamodb does not allow empty strings => keep key but set value to null
            attributeValue = AttributeValue.builder().nul(true).build();
        } else {
            attributeValue = AttributeValue.builder().s(value).build();
        }

        return UpdateItemRequest.builder()
                .tableName(jobMetaTableName)
                .key(ImmutableMap.of(JOB_TYPE, AttributeValue.builder().s(jobType).build()))
                .returnValues(ReturnValue.UPDATED_OLD)
                .updateExpression("SET " + key + " = :value")
                .expressionAttributeValues(ImmutableMap.of(":value", attributeValue))
                .build();
    }

    private UpdateItemRequest removeKeyRequest(String jobType, String key) {
        return UpdateItemRequest.builder()
                .tableName(jobMetaTableName)
                .key(ImmutableMap.of(JOB_TYPE, AttributeValue.builder().s(jobType).build()))
                .returnValues(ReturnValue.UPDATED_OLD)
                .updateExpression("REMOVE " + key)
                .build();
    }

    @Override
    public String getValue(String jobType, String key) {
        GetItemResponse response = getItem(jobType);
        return response.item().getOrDefault(key, AttributeValue.builder().build()).s();
    }

    private GetItemResponse getItem(String jobType) {
        GetItemRequest request = GetItemRequest.builder()
                .tableName(jobMetaTableName)
                .key(ImmutableMap.of(JOB_TYPE, AttributeValue.builder().s(jobType).build()))
                .consistentRead(true)
                .build();
        return dynamoDBClient.getItem(request);
    }

    @Override
    public Set<String> findAllJobTypes() {
        ScanRequest scanRequest = ScanRequest.builder()
                .tableName(jobMetaTableName)
                .build();

        ScanResponse response = dynamoDBClient.scan(scanRequest);
        return response.items().stream().map(item -> item.get(JOB_TYPE).s()).collect(Collectors.toSet());
    }

    @Override
    public void deleteAll() {
        throw new UnsupportedOperationException();
    }
}
