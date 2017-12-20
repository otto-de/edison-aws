package de.otto.edison.aws.dynamo.jobs;

import com.amazonaws.util.ImmutableMapParameter;
import de.otto.edison.jobs.domain.JobInfo;
import de.otto.edison.jobs.domain.JobMessage;
import de.otto.edison.jobs.repository.JobRepository;
import software.amazon.awssdk.services.dynamodb.DynamoDBClient;
import software.amazon.awssdk.services.dynamodb.model.*;

import java.time.OffsetDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import static de.otto.edison.aws.dynamo.jobs.JobInfoConverter.convertJobInfo;

public class DynamoJobRepository implements JobRepository {

    private DynamoDBClient dynamoDBClient;
    private DynamoJobRepoProperties dynamoJobRepoProperties;

    public DynamoJobRepository(final DynamoDBClient dynamoDBClient,
                               final DynamoJobRepoProperties dynamoJobRepoProperties) {
        this.dynamoDBClient = dynamoDBClient;
        this.dynamoJobRepoProperties = dynamoJobRepoProperties;
    }

    @Override
    public Optional<JobInfo> findOne(String jobId) {
        Map<String, AttributeValue> jobIdMap = JobInfoConverter.createJobIdMap(jobId);
        GetItemRequest itemRequest = GetItemRequest.builder()
                .tableName(dynamoJobRepoProperties.getTableName())
                .key(jobIdMap)
                .build();
        GetItemResponse itemResponse = dynamoDBClient.getItem(itemRequest);
        if (itemResponse == null || itemResponse.item() == null || itemResponse.item().isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(JobInfoConverter.convert(itemResponse.item()));
    }

    @Override
    public List<JobInfo> findLatest(int maxCount) {
        ScanRequest scanRequest = ScanRequest.builder()
                .tableName(dynamoJobRepoProperties.getTableName())
                .build();
        ScanResponse scanResponse = dynamoDBClient.scan(scanRequest);
        return toJobInfoList(scanResponse).stream()
                .sorted(Comparator.comparing(JobInfo::getStarted, OffsetDateTime::compareTo).reversed())
                .limit(maxCount)
                .collect(Collectors.toList());
    }

    @Override
    public List<JobInfo> findLatestJobsDistinct() {
        return null;
    }

    @Override
    public List<JobInfo> findLatestBy(String type, int maxCount) {
        return null;
    }

    @Override
    public List<JobInfo> findRunningWithoutUpdateSince(OffsetDateTime timeOffset) {
        return null;
    }

    @Override
    public List<JobInfo> findAll() {
        ScanResponse scanResponse = dynamoDBClient.scan(ScanRequest.builder().tableName(dynamoJobRepoProperties.getTableName()).build());
        return toJobInfoList(scanResponse);
    }

    @Override
    public List<JobInfo> findAllJobInfoWithoutMessages() {
        return null;
    }

    @Override
    public List<JobInfo> findByType(String jobType) {
        ScanRequest scanRequest = ScanRequest.builder()
                .tableName(dynamoJobRepoProperties.getTableName())
                .filterExpression(JobInfoConverter.JOB_TYPE + "= :jobType")
                .expressionAttributeValues(ImmutableMapParameter.of(":jobType", AttributeValue.builder().s(jobType).build()))
                .build();
        ScanResponse scanResponse = dynamoDBClient.scan(scanRequest);
        return toJobInfoList(scanResponse);
    }

    private List<JobInfo> toJobInfoList(ScanResponse scanResponse) {
        return scanResponse.items().stream().map(JobInfoConverter::convert).collect(Collectors.toList());
    }

    @Override
    public JobInfo createOrUpdate(JobInfo job) {
        Map<String, AttributeValue> item = convertJobInfo(job);
        PutItemRequest putItemRequest = PutItemRequest.builder()
                .tableName(dynamoJobRepoProperties.getTableName())
                .item(item)
                .build();
        dynamoDBClient.putItem(putItemRequest);
        return job;
    }

    @Override
    public void removeIfStopped(String jobId) {

    }

    @Override
    public JobInfo.JobStatus findStatus(String jobId) {
        return null;
    }

    @Override
    public void appendMessage(String jobId, JobMessage jobMessage) {

    }

    @Override
    public void setJobStatus(String jobId, JobInfo.JobStatus jobStatus) {

    }

    @Override
    public void setLastUpdate(String jobId, OffsetDateTime lastUpdate) {

    }

    @Override
    public long size() {
        return 0;
    }

    @Override
    public void deleteAll() {

    }
}
