package de.otto.edison.aws.dynamo.jobs;

import de.otto.edison.jobs.domain.JobInfo;
import de.otto.edison.jobs.domain.JobMessage;
import de.otto.edison.jobs.domain.Level;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;

import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static de.otto.edison.jobs.domain.JobInfo.JobStatus.valueOf;
import static java.util.stream.Collectors.toList;

public class JobInfoConverter {

    public static final String ID = "id";
    public static final String HOSTNAME = "hostname";
    public static final String JOB_TYPE = "jobType";
    public static final String JOB_STATUS = "jobStatus";
    public static final String STARTED = "started";
    public static final String STOPPED = "stopped";
    public static final String LAST_UPDATED = "lastUpdated";
    public static final String MESSAGES = "messages";
    public static final String MSG_LEVEL = "level";
    public static final String MSG = "message";
    public static final String MSG_TIMESTAMP = "timestamp";

    public static Map<String, AttributeValue> convertJobInfo(JobInfo jobInfo) {
        Map<String, AttributeValue> itemsMap = new HashMap<>();
        itemsMap.put(ID, createStringAttributeValue(jobInfo.getJobId()));
        itemsMap.put(HOSTNAME, createStringAttributeValue(jobInfo.getHostname()));
        itemsMap.put(JOB_TYPE, createStringAttributeValue(jobInfo.getJobType()));
        itemsMap.put(JOB_STATUS, createStringAttributeValue(jobInfo.getStatus().name()));
        itemsMap.put(STARTED, createDateAttributeValue(jobInfo.getStarted()));
        itemsMap.put(LAST_UPDATED, createDateAttributeValue(jobInfo.getLastUpdated()));
        if (jobInfo.getStopped().isPresent()) {
            itemsMap.put(STOPPED, createDateAttributeValue(jobInfo.getStopped().get()));
        }
        List<AttributeValue> attributeValues = jobInfo.getMessages().stream().map(JobInfoConverter::mapJobMessage).collect(toList());
        itemsMap.put(MESSAGES, AttributeValue.builder().l(attributeValues).build());
        return itemsMap;
    }

    private static AttributeValue mapJobMessage(JobMessage msg) {
        Map<String, AttributeValue> msgMap = new HashMap<>();
        msgMap.put(MSG, createStringAttributeValue(msg.getMessage()));
        msgMap.put(MSG_LEVEL, createStringAttributeValue(msg.getLevel().getKey()));
        msgMap.put(MSG_TIMESTAMP, createDateAttributeValue(msg.getTimestamp()));
        return AttributeValue.builder().m(msgMap).build();
    }

    public static Map<String, AttributeValue> createJobIdMap(String jobId) {
        Map<String, AttributeValue> idMap = new HashMap<>();
        idMap.put(ID, createStringAttributeValue(jobId));
        return idMap;
    }

    public static JobInfo convert(Map<String, AttributeValue> attributeValueMap) {
        JobInfo.Builder builder = JobInfo.builder();
        stringValueFromAttribute(attributeValueMap, ID).ifPresent(builder::setJobId);
        stringValueFromAttribute(attributeValueMap, HOSTNAME).ifPresent(builder::setHostname);
        stringValueFromAttribute(attributeValueMap, JOB_TYPE).ifPresent(builder::setJobType);
        stringValueFromAttribute(attributeValueMap, JOB_STATUS).ifPresent(s -> builder.setStatus(valueOf(s)));
        dateValueFromAttribute(attributeValueMap, STARTED).ifPresent(builder::setStarted);
        dateValueFromAttribute(attributeValueMap, STOPPED).ifPresent(builder::setStopped);
        dateValueFromAttribute(attributeValueMap, LAST_UPDATED).ifPresent(builder::setLastUpdated);

        List<AttributeValue> messages = attributeValueMap.get(MESSAGES).l();
        List<JobMessage> jobMessages = messages.stream().map(JobInfoConverter::mapAttributeValueToJobMessage).collect(toList());
        builder.setMessages(jobMessages);
        return builder.build();
    }

    private static JobMessage mapAttributeValueToJobMessage(AttributeValue attr) {
        Map<String, AttributeValue> valueMap = attr.m();

        String level = stringValueFromAttribute(valueMap, MSG_LEVEL).orElse(Level.INFO.getKey());
        String msg = stringValueFromAttribute(valueMap, MSG).orElse("");
        OffsetDateTime dateTime = dateValueFromAttribute(valueMap, MSG_TIMESTAMP).orElse(null);
        return JobMessage.jobMessage(Level.ofKey(level), msg, dateTime);
    }

    private static AttributeValue createStringAttributeValue(String value) {
        return AttributeValue.builder().s(value).build();
    }

    private static AttributeValue createDateAttributeValue(OffsetDateTime dateTime) {
        String formatedDateTime = DateTimeFormatter.ISO_OFFSET_DATE_TIME.format(dateTime);
        return AttributeValue.builder().s(formatedDateTime).build();
    }

    private static final Optional<String> stringValueFromAttribute(Map<String, AttributeValue> attributeValueMap, String key) {
        if (attributeValueMap.containsKey(key)) {
            return Optional.of(attributeValueMap.get(key).s());
        }
        return Optional.empty();
    }

    private static final Optional<OffsetDateTime> dateValueFromAttribute(Map<String, AttributeValue> attributeValueMap, String key) {
        if (attributeValueMap.containsKey(key)) {;
            String formattedDateTime = attributeValueMap.get(key).s();
            OffsetDateTime offsetDateTime = OffsetDateTime.parse(formattedDateTime, DateTimeFormatter.ISO_OFFSET_DATE_TIME);
            return Optional.of(offsetDateTime);
        }
        return Optional.empty();
    }
}
