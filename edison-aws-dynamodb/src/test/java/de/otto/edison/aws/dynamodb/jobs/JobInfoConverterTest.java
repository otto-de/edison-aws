package de.otto.edison.aws.dynamodb.jobs;

import com.google.common.collect.ImmutableMap;
import de.otto.edison.jobs.domain.JobInfo;
import de.otto.edison.jobs.domain.JobMessage;
import de.otto.edison.jobs.domain.Level;
import org.assertj.core.util.Lists;
import org.junit.Test;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static java.time.format.DateTimeFormatter.ISO_OFFSET_DATE_TIME;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNull.nullValue;

public class JobInfoConverterTest {

    @Test
    public void shouldConvertJobInfo() {
        // given
        OffsetDateTime started = OffsetDateTime.now().minusMinutes(1);
        OffsetDateTime stopped = OffsetDateTime.now();
        OffsetDateTime lastUpdated = OffsetDateTime.now().minusSeconds(7);
        List<JobMessage> messages = new ArrayList<>();
        messages.add(JobMessage.jobMessage(Level.INFO, "someMessage", started));
        messages.add(JobMessage.jobMessage(Level.WARNING, "someOtherMessage", lastUpdated));
        JobInfo jobInfo = JobInfo.builder()
                .setJobId("someJobId")
                .setHostname("someHostName")
                .setStatus(JobInfo.JobStatus.OK)
                .setJobType("someJobeType")
                .setStarted(started)
                .setStopped(stopped)
                .setLastUpdated(lastUpdated)
                .setMessages(messages)
                .build();

        // when
        Map<String, AttributeValue> jobInfoAttributeMap = JobInfoConverter.convertJobInfo(jobInfo);

        // then
        assertThat(jobInfoAttributeMap.get(JobInfoConverter.ID).s(), is("someJobId"));
        assertThat(jobInfoAttributeMap.get(JobInfoConverter.HOSTNAME).s(), is("someHostName"));
        assertThat(jobInfoAttributeMap.get(JobInfoConverter.JOB_STATUS).s(), is("OK"));
        assertThat(jobInfoAttributeMap.get(JobInfoConverter.JOB_TYPE).s(), is("someJobeType"));
        assertThat(jobInfoAttributeMap.get(JobInfoConverter.STARTED).s(), is(ISO_OFFSET_DATE_TIME.format(started)));
        assertThat(jobInfoAttributeMap.get(JobInfoConverter.STOPPED).s(), is(ISO_OFFSET_DATE_TIME.format(stopped)));
        assertThat(jobInfoAttributeMap.get(JobInfoConverter.LAST_UPDATED).s(), is(ISO_OFFSET_DATE_TIME.format(lastUpdated)));

        AttributeValue messagesAttribute = jobInfoAttributeMap.get(JobInfoConverter.MESSAGES);
        List<AttributeValue> msgs = messagesAttribute.l();
        assertThat(msgs.size(), is(2));
        assertThat(msgs.get(0).m().get(JobInfoConverter.MSG_LEVEL).s(), is("info"));
        assertThat(msgs.get(0).m().get(JobInfoConverter.MSG).s(), is("someMessage"));
        assertThat(msgs.get(0).m().get(JobInfoConverter.MSG_TIMESTAMP).s(), is(ISO_OFFSET_DATE_TIME.format(started)));
        assertThat(msgs.get(1).m().get(JobInfoConverter.MSG_LEVEL).s(), is("warning"));
        assertThat(msgs.get(1).m().get(JobInfoConverter.MSG).s(), is("someOtherMessage"));
        assertThat(msgs.get(1).m().get(JobInfoConverter.MSG_TIMESTAMP).s(), is(ISO_OFFSET_DATE_TIME.format(lastUpdated)));

        // when
        JobInfo convertJobInfo = JobInfoConverter.convert(jobInfoAttributeMap);

        // then
        assertThat(convertJobInfo, is(jobInfo));
    }

    @Test
    public void shouldUseNullForEmptyStrings() {
        OffsetDateTime date = OffsetDateTime.now();
        JobInfo jobInfo = JobInfo.builder()
                .setJobId("someJobId")
                .setHostname("")
                .setStatus(JobInfo.JobStatus.OK)
                .setJobType("someJobeType")
                .setStarted(date)
                .setStopped(date)
                .setLastUpdated(date)
                .build();

        Map<String, AttributeValue> result = JobInfoConverter.convertJobInfo(jobInfo);

        assertThat(result.get(JobInfoConverter.HOSTNAME).nul(), is(true));
    }

    @Test
    public void shouldHandleNullValuesWhenReading() {
        Map<String, AttributeValue> attrMap = ImmutableMap.<String, AttributeValue>builder()
                .put(JobInfoConverter.ID, attrValS("someJobId"))
                .put(JobInfoConverter.STARTED, attrValS(ISO_OFFSET_DATE_TIME.format(OffsetDateTime.now())))
                .put(JobInfoConverter.JOB_STATUS, attrValS("OK"))
                .put(JobInfoConverter.LAST_UPDATED, attrValS(ISO_OFFSET_DATE_TIME.format(OffsetDateTime.now())))
                .put(JobInfoConverter.HOSTNAME, AttributeValue.builder().nul(true).build())
                .put(JobInfoConverter.MESSAGES, AttributeValue.builder().l(Lists.emptyList()).build())
                .build();

        JobInfo result = JobInfoConverter.convert(attrMap);

        assertThat(result.getHostname(), is(nullValue()));
    }

    private AttributeValue attrValS(String stringValue) {
        return AttributeValue.builder().s(stringValue).build();
    }
}
