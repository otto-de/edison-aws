package de.otto.edison.aws.dynamo.jobs;

import de.otto.edison.jobs.domain.JobInfo;
import de.otto.edison.jobs.domain.JobMessage;
import de.otto.edison.jobs.domain.Level;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import software.amazon.awssdk.services.dynamodb.DynamoDBClient;
import software.amazon.awssdk.services.dynamodb.model.*;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.IntStream;

import static de.otto.edison.aws.dynamo.jobs.JobInfoConverter.ID;
import static de.otto.edison.aws.dynamo.jobs.testsupport.JobInfoMother.jobInfo;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

@RunWith(SpringRunner.class)
@ActiveProfiles("test")
@ContextConfiguration(classes = DynamoDbTestConfiguration.class)
@SpringBootTest
public class DynamoJobRepositoryTest {

    private static final String TABLE_NAME = "jobInfo";

    @Autowired
    private DynamoDBClient dynamoDBClient;
    private DynamoJobRepository dynamoJobRepository;

    @Before
    public void before() {
        createJobInfoTable();
        dynamoJobRepository = new DynamoJobRepository(dynamoDBClient, new DynamoJobRepoProperties(true, TABLE_NAME));
    }

    @After
    public void after() {
        deleteJobInfoTable();
    }

    private void createJobInfoTable() {
        dynamoDBClient.createTable(CreateTableRequest.builder()
                .tableName(TABLE_NAME)
                .keySchema(
                        KeySchemaElement.builder().attributeName(ID).keyType(KeyType.HASH).build()
                )
                .attributeDefinitions(
                        AttributeDefinition.builder().attributeName(ID).attributeType(ScalarAttributeType.S).build()
                )
                .provisionedThroughput(ProvisionedThroughput.builder()
                        .readCapacityUnits(1L)
                        .writeCapacityUnits(1L)
                        .build())
                .build());
    }

    private void deleteJobInfoTable() {
        dynamoDBClient.deleteTable(DeleteTableRequest.builder().tableName(TABLE_NAME).build());
    }

    @Test
    public void shouldWriteAndReadJobInfo() {
        // given
        JobInfo jobInfo = jobInfo("someJobId").build();
        dynamoJobRepository.createOrUpdate(jobInfo);

        // when
        Optional<JobInfo> jobInfoFromDb = dynamoJobRepository.findOne("someJobId");

        // then
        assertThat(jobInfoFromDb.get(), is(jobInfo));
    }

    @Test
    public void shouldFindAllJobInfos() {
        //given
        IntStream.range(0, 50)
                .mapToObj(i -> jobInfo("someJobId-" + i).build())
                .forEach(jobInfo -> dynamoJobRepository.createOrUpdate(jobInfo));

        //when
        List<JobInfo> jobInfos = dynamoJobRepository.findAll();

        //then
        assertThat(jobInfos, hasSize(50));
    }

    @Test
    public void shouldFindByType() {
        //given
        IntStream.range(0, 10)
                .mapToObj(i -> jobInfo("someJobId-" + i).setJobType("type-a").build())
                .forEach(jobInfo -> dynamoJobRepository.createOrUpdate(jobInfo));
        IntStream.range(10, 20)
                .mapToObj(i -> jobInfo("someJobId-" + i).setJobType("type-b").build())
                .forEach(jobInfo -> dynamoJobRepository.createOrUpdate(jobInfo));

        //when
        List<JobInfo> jobInfos = dynamoJobRepository.findByType("type-b");

        //then
        assertThat(jobInfos, hasSize(10));
        assertThat(jobInfos.stream().filter(jobInfo -> jobInfo.getJobType().equals("type-b")).count(), is(10L));
    }

    @Test
    public void shouldFindLatest() {
        //given
        OffsetDateTime now = OffsetDateTime.now();
        JobInfo jobInfo1 = jobInfo("someJobId1").setStarted(now.minusSeconds(7)).build();
        JobInfo jobInfo2 = jobInfo("someJobId2").setStarted(now.minusSeconds(5)).build();
        JobInfo jobInfo3 = jobInfo("someJobId3").setStarted(now.minusMinutes(3)).build();

        dynamoJobRepository.createOrUpdate(jobInfo1);
        dynamoJobRepository.createOrUpdate(jobInfo2);
        dynamoJobRepository.createOrUpdate(jobInfo3);

        //when
        List<JobInfo> latest = dynamoJobRepository.findLatest(2);

        //then
        assertThat(latest, hasSize(2));
        assertThat(latest.get(0), is(jobInfo2));
        assertThat(latest.get(1), is(jobInfo1));


    }

    @Test
    public void shouldFindLatestJobsDistinct() {
        //given
        OffsetDateTime now = OffsetDateTime.now();
        JobInfo jobInfo1 = jobInfo("someJobId1").setStarted(now.minusSeconds(7)).setJobType("foo").build();
        JobInfo jobInfo2 = jobInfo("someJobId2").setStarted(now.minusSeconds(5)).setJobType("bar").build();
        JobInfo jobInfo3 = jobInfo("someJobId3").setStarted(now.minusMinutes(3)).setJobType("baz").build();
        JobInfo jobInfo4 = jobInfo("someJobId4").setStarted(now.minusMinutes(5)).setJobType("foo").build();

        dynamoJobRepository.createOrUpdate(jobInfo1);
        dynamoJobRepository.createOrUpdate(jobInfo2);
        dynamoJobRepository.createOrUpdate(jobInfo3);
        dynamoJobRepository.createOrUpdate(jobInfo4);

        //when
        List<JobInfo> latest = dynamoJobRepository.findLatestJobsDistinct();

        //then
        assertThat(latest, hasSize(3));
        assertThat(latest.get(0), is(jobInfo2));
        assertThat(latest.get(1), is(jobInfo1));
        assertThat(latest.get(2), is(jobInfo3));
    }

    @Test
    public void shouldFindLatestByType() {
        //given
        OffsetDateTime now = OffsetDateTime.now();
        JobInfo jobInfo1 = jobInfo("someJobId1").setStarted(now.minusSeconds(7)).setJobType("foo").build();
        JobInfo jobInfo2 = jobInfo("someJobId2").setStarted(now.minusSeconds(5)).setJobType("bar").build();
        JobInfo jobInfo3 = jobInfo("someJobId3").setStarted(now.minusMinutes(3)).setJobType("baz").build();
        JobInfo jobInfo4 = jobInfo("someJobId4").setStarted(now.minusMinutes(5)).setJobType("foo").build();
        JobInfo jobInfo5 = jobInfo("someJobId5").setStarted(now.minusMinutes(9)).setJobType("foo").build();

        dynamoJobRepository.createOrUpdate(jobInfo1);
        dynamoJobRepository.createOrUpdate(jobInfo2);
        dynamoJobRepository.createOrUpdate(jobInfo3);
        dynamoJobRepository.createOrUpdate(jobInfo4);
        dynamoJobRepository.createOrUpdate(jobInfo5);

        //when
        List<JobInfo> latest = dynamoJobRepository.findLatestBy("foo", 2);

        //then
        assertThat(latest, hasSize(2));
        assertThat(latest.get(0), is(jobInfo1));
        assertThat(latest.get(1), is(jobInfo4));
    }

    @Test
    public void shouldFindRunningWithoutUpdateSince() {
        //given
        OffsetDateTime now = OffsetDateTime.now();
        JobInfo jobInfo1 = jobInfo("someJobId1").setStarted(now.minusSeconds(7)).setLastUpdated(now.minusMinutes(2)).setStopped(null).build();
        JobInfo jobInfo2 = jobInfo("someJobId2").setStarted(now.minusSeconds(5)).setLastUpdated(now.minusSeconds(10)).setStopped(null).build();
        JobInfo jobInfo3 = jobInfo("someJobId3").setStarted(now.minusMinutes(3)).setLastUpdated(now.minusSeconds(61)).setStopped(null).build();

        dynamoJobRepository.createOrUpdate(jobInfo1);
        dynamoJobRepository.createOrUpdate(jobInfo2);
        dynamoJobRepository.createOrUpdate(jobInfo3);

        //when
        List<JobInfo> latest = dynamoJobRepository.findRunningWithoutUpdateSince(now.minusMinutes(1));

        //then
        assertThat(latest, hasSize(2));
        assertThat(latest.get(0), is(jobInfo1));
        assertThat(latest.get(1), is(jobInfo3));
    }

    @Test
    public void shouldFindAllJobInfoWithoutMessages() {
        //given
        OffsetDateTime now = OffsetDateTime.now();
        JobInfo jobInfo1 = jobInfo("someJobId1").setStarted(now.minusSeconds(7)).setMessages(singletonList(JobMessage.jobMessage(Level.INFO, "some message", now))).build();
        JobInfo jobInfo2 = jobInfo("someJobId2").setStarted(now.minusSeconds(5)).setMessages(emptyList()).build();
        JobInfo jobInfo3 = jobInfo("someJobId3").setStarted(now.minusMinutes(3)).setMessages(singletonList(JobMessage.jobMessage(Level.ERROR, "some other message", now))).build();

        dynamoJobRepository.createOrUpdate(jobInfo1);
        dynamoJobRepository.createOrUpdate(jobInfo2);
        dynamoJobRepository.createOrUpdate(jobInfo3);

        //when
        List<JobInfo> latest = dynamoJobRepository.findAllJobInfoWithoutMessages();

        //then
        assertThat(latest, hasSize(3));
        assertThat(latest.get(1).getMessages(), is(emptyList()));
        assertThat(latest.get(0).getMessages(), is(emptyList()));
        assertThat(latest.get(2).getMessages(), is(emptyList()));
    }

    @Test
    public void shouldRemoveIfStopped() {
        OffsetDateTime now = OffsetDateTime.now();
        JobInfo jobInfo1 = jobInfo("someJobId1").setStarted(now.minusSeconds(7)).setStopped(now.minusSeconds(1)).build();
        JobInfo jobInfo2 = jobInfo("someJobId2").setStarted(now.minusSeconds(5)).setStopped(null).build();

        dynamoJobRepository.createOrUpdate(jobInfo1);
        dynamoJobRepository.createOrUpdate(jobInfo2);

        //when
        dynamoJobRepository.removeIfStopped("someJobId1");
        dynamoJobRepository.removeIfStopped("someJobId2");

        //then
        List<JobInfo> allJobs = dynamoJobRepository.findAll();
        assertThat(allJobs, hasSize(1));
        assertThat(allJobs.get(0), is(jobInfo2));
    }
}
