package de.otto.edison.aws.dynamodb.jobs.testsupport;

import de.otto.edison.jobs.domain.JobInfo;
import de.otto.edison.jobs.domain.JobMessage;
import de.otto.edison.jobs.domain.Level;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

public class JobInfoMother {

    public static JobInfo.Builder jobInfo(String jobId) {
        OffsetDateTime started = OffsetDateTime.now().minusMinutes(1);
        List<JobMessage> messages = new ArrayList<>();
        messages.add(JobMessage.jobMessage(Level.INFO, "someMessage", started));

        JobInfo.Builder jobInfoBuilder = JobInfo.builder()
                .setJobId(jobId)
                .setHostname("someHostName")
                .setStatus(JobInfo.JobStatus.OK)
                .setJobType("someJobeType")
                .setStarted(started)
                .setStopped(OffsetDateTime.now())
                .setLastUpdated(OffsetDateTime.now().minusSeconds(7))
                .setMessages(messages);
        return jobInfoBuilder;
    }
}
