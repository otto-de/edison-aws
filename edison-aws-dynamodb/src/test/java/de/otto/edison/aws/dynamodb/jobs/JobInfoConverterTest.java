package de.otto.edison.aws.dynamodb.jobs;

import de.otto.edison.jobs.domain.JobInfo;
import de.otto.edison.jobs.domain.JobMessage;
import de.otto.edison.jobs.domain.Level;
import org.junit.Test;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static java.time.format.DateTimeFormatter.ISO_OFFSET_DATE_TIME;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

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

}
