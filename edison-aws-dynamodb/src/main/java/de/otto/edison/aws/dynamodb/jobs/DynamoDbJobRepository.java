package de.otto.edison.aws.dynamodb.jobs;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import de.otto.edison.jobs.domain.JobInfo;
import de.otto.edison.jobs.domain.JobMessage;
import de.otto.edison.jobs.repository.JobRepository;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.*;

import java.time.OffsetDateTime;
import java.util.*;
import java.util.stream.Collectors;

import static de.otto.edison.aws.dynamodb.jobs.JobInfoConverter.MESSAGES;
import static de.otto.edison.aws.dynamodb.jobs.JobInfoConverter.convertJobInfo;
import static java.util.Collections.emptyList;
import static java.util.Objects.nonNull;
import static java.util.stream.Collectors.toList;

public class DynamoDbJobRepository implements JobRepository {

    private static final Comparator<JobInfo> STARTED_TIME_DESC_COMPARATOR = Comparator.comparing(JobInfo::getStarted, OffsetDateTime::compareTo).reversed();
    private DynamoDbClient dynamoDBClient;
    private DynamoDbJobRepoProperties dynamoJobRepoProperties;

    public DynamoDbJobRepository(final DynamoDbClient dynamoDBClient,
                                 final DynamoDbJobRepoProperties dynamoJobRepoProperties) {
        this.dynamoDBClient = dynamoDBClient;
        this.dynamoJobRepoProperties = dynamoJobRepoProperties;
    }

    @Override
    public Optional<JobInfo> findOne(String jobId) {
        Map<String, AttributeValue> jobIdMap = JobInfoConverter.createJobIdMap(jobId);
        GetItemRequest itemRequest = GetItemRequest.builder()
                .tableName(dynamoJobRepoProperties.getJobInfoTableName())
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
        return findAll()
                .stream()
                .sorted(STARTED_TIME_DESC_COMPARATOR)
                .limit(maxCount)
                .collect(Collectors.toList());
    }

    @Override
    public List<JobInfo> findLatestJobsDistinct() {
        Set<String> typeSet = new HashSet<>();

        return findAll()
                .stream()
                .sorted(STARTED_TIME_DESC_COMPARATOR)
                .filter(j -> nonNull(j.getJobType()))
                .filter(j -> typeSet.add(j.getJobType()))
                .collect(toList());

    }

    @Override
    public List<JobInfo> findLatestBy(String type, int maxCount) {
        return findAll()
                .stream()
                .sorted(STARTED_TIME_DESC_COMPARATOR)
                .filter(jobInfo -> jobInfo.getJobType().equalsIgnoreCase(type))
                .limit(maxCount)
                .collect(toList());
    }

    @Override
    public List<JobInfo> findRunningWithoutUpdateSince(OffsetDateTime timeOffset) {
        return findAll()
                .stream()
                .filter(jobInfo -> !jobInfo.isStopped() && jobInfo.getLastUpdated().isBefore(timeOffset))
                .sorted(STARTED_TIME_DESC_COMPARATOR)
                .collect(toList());
    }

    @Override
    public List<JobInfo> findAll() {
        ScanResponse scanResponse = dynamoDBClient.scan(ScanRequest.builder().tableName(dynamoJobRepoProperties.getJobInfoTableName()).build());
        return toJobInfoList(scanResponse);
    }

    @Override
    public List<JobInfo> findAllJobInfoWithoutMessages() {
        return findAll().stream()
                .sorted(STARTED_TIME_DESC_COMPARATOR)
                .map(job -> job.copy().setMessages(emptyList()).build())
                .collect(toList());
    }

    @Override
    public List<JobInfo> findByType(String jobType) {
        ScanRequest scanRequest = ScanRequest.builder()
                .tableName(dynamoJobRepoProperties.getJobInfoTableName())
                .filterExpression(JobInfoConverter.JOB_TYPE + "= :jobType")
                .expressionAttributeValues(ImmutableMap.of(":jobType", AttributeValue.builder().s(jobType).build()))
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
                .tableName(dynamoJobRepoProperties.getJobInfoTableName())
                .item(item)
                .build();
        dynamoDBClient.putItem(putItemRequest);
        return job;
    }

    @Override
    public void removeIfStopped(String jobId) {
        Optional<JobInfo> jobInfo = findOne(jobId);
        jobInfo.ifPresent(job -> {
            if (job.isStopped()) {
                remove(jobId);
            }
        });
    }

    private void remove(String jobId) {
        DeleteItemRequest deleteItemRequest = DeleteItemRequest.builder()
                .tableName(dynamoJobRepoProperties.getJobInfoTableName())
                .key(ImmutableMap.of(JobInfoConverter.ID, AttributeValue.builder().s(jobId).build()))
                .build();
        dynamoDBClient.deleteItem(deleteItemRequest);
    }

    @Override
    public JobInfo.JobStatus findStatus(String jobId) {
        Optional<JobInfo> jobInfo = findOne(jobId);
        if (jobInfo.isPresent()) {
            return jobInfo.get().getStatus();
        } else {
            return null;
        }
    }

    @Override
    public void appendMessage(String jobId, JobMessage jobMessage) {
        AttributeValue listOfJobMessageMap = AttributeValue.builder()
                .l(JobInfoConverter.mapJobMessage(jobMessage))
                .build();
        
        dynamoDBClient.updateItem(UpdateItemRequest.builder()
                .tableName(dynamoJobRepoProperties.getJobInfoTableName())
                .key(JobInfoConverter.createJobIdMap(jobId))
                .updateExpression("SET " + MESSAGES + " = list_append(" + MESSAGES + ", :m)")
                .expressionAttributeValues(ImmutableMap.of(":m", listOfJobMessageMap))
                .build());
    }

    @Override
    public void setJobStatus(String jobId, JobInfo.JobStatus jobStatus) {
        Optional<JobInfo> jobInfo = findOne(jobId);
        jobInfo.ifPresent(jobInfo1 -> {
            JobInfo modifiedJobInfo = jobInfo1.copy().setStatus(jobStatus).build();
            createOrUpdate(modifiedJobInfo);
        });
    }

    @Override
    public void setLastUpdate(String jobId, OffsetDateTime lastUpdate) {
        Optional<JobInfo> jobInfo = findOne(jobId);
        jobInfo.ifPresent(jobInfo1 -> {
            JobInfo modifiedJobInfo = jobInfo1.copy().setLastUpdated(lastUpdate).build();
            createOrUpdate(modifiedJobInfo);
        });
    }

    @Override
    public long size() {
        return findAll().size();
    }

    @Override
    public void deleteAll() {
        List<WriteRequest> deleteRequests = findAll().stream().map(jobInfo -> WriteRequest.builder()
                .deleteRequest(DeleteRequest.builder()
                        .key(ImmutableMap.of(JobInfoConverter.ID, AttributeValue.builder().s(jobInfo.getJobId()).build()))
                        .build())
                .build()
        ).collect(toList());

        Lists.partition(deleteRequests, 25).forEach(this::deleteParts);
    }

    private void deleteParts(List<WriteRequest> part) {
        BatchWriteItemResponse batchWriteItemResponse = batchDeleteItems(ImmutableMap.of(dynamoJobRepoProperties.getJobInfoTableName(), part));
        while (!batchWriteItemResponse.unprocessedItems().isEmpty()) {
            batchDeleteItems(batchWriteItemResponse.unprocessedItems());
        }
    }

    private BatchWriteItemResponse batchDeleteItems(Map<String, List<WriteRequest>> requestItems) {
        BatchWriteItemRequest batchWriteItemRequest = BatchWriteItemRequest.builder()
                .requestItems(requestItems)
                .build();
        return dynamoDBClient.batchWriteItem(batchWriteItemRequest);
    }
}
