package de.otto.edison.aws.dynamodb.jobs;

import com.google.common.collect.ImmutableMap;
import de.otto.edison.jobs.domain.JobMeta;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.*;

import java.util.Collections;
import java.util.Set;

import static de.otto.edison.aws.dynamodb.jobs.DynamoDbJobMetaRepository.JOB_TYPE;
import static de.otto.edison.aws.dynamodb.jobs.DynamoDbJobMetaRepository.KEY_DISABLED;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertThat;

@RunWith(SpringRunner.class)
@ActiveProfiles("test")
@ContextConfiguration(classes = DynamoDbTestConfiguration.class)
@SpringBootTest
public class DynamoDbJobMetaRepositoryTest {

    private static final String TABLE_NAME = "jobMeta";


    @Autowired
    private DynamoDbClient dynamoDBClient;
    private DynamoDbJobMetaRepository dynamoJobMetaRepository;

    @Before
    public void before() {
        createJobInfoTable();
        dynamoJobMetaRepository = new DynamoDbJobMetaRepository(dynamoDBClient, new DynamoDbJobRepoProperties("jobInfo", TABLE_NAME));
    }

    @After
    public void after() {
        deleteJobInfoTable();
    }

    private void createJobInfoTable() {
        dynamoDBClient.createTable(CreateTableRequest.builder()
                .tableName(TABLE_NAME)
                .keySchema(
                        KeySchemaElement.builder().attributeName(JOB_TYPE).keyType(KeyType.HASH).build()
                )
                .attributeDefinitions(
                        AttributeDefinition.builder().attributeName(JOB_TYPE).attributeType(ScalarAttributeType.S).build()
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
    public void shouldSetRunningJob() {
        //when
        boolean notRunning = dynamoJobMetaRepository.setRunningJob("myJobType", "myJobId");


        //given
        String jobId = dynamoJobMetaRepository.getRunningJob("myJobType");
        assertThat(jobId, is("myJobId"));
        assertThat(notRunning, is(true));
    }

    @Test
    public void shouldNotSetRunningWhenIsAlreadyRunning() {
        //given
        dynamoJobMetaRepository.setRunningJob("myJobType", "otherJobId");

        //when
        boolean notRunning = dynamoJobMetaRepository.setRunningJob("myJobType", "myJobId");

        //given
        String jobId = dynamoJobMetaRepository.getRunningJob("myJobType");
        assertThat(jobId, is("otherJobId"));
        assertThat(notRunning, is(false));
    }

    @Test
    public void shouldSetAndGetValue() {
        //given

        //when
        dynamoJobMetaRepository.setValue("myJobType", "someKey", "someValue");

        //given
        String value = dynamoJobMetaRepository.getValue("myJobType", "someKey");
        assertThat(value, is("someValue"));
    }

    @Test
    public void shouldReturnPreviousValueOnSetValue() {
        //given
        dynamoJobMetaRepository.setValue("myJobType", "someKey", "someOldValue");

        //when
        String previousValue = dynamoJobMetaRepository.setValue("myJobType", "someKey", "someNewValue");

        //given
        assertThat(previousValue, is("someOldValue"));
    }

    @Test
    public void shouldRemoveKeyWhenValueIsNull() {
        //given
        dynamoJobMetaRepository.setValue("myJobType", "someKey", "someOldValue");

        //when
        String previousValue = dynamoJobMetaRepository.setValue("myJobType", "someKey", null);

        //given
        assertThat(previousValue, is("someOldValue"));
        String value = dynamoJobMetaRepository.getValue("myJobType", "someKey");
        assertThat(value, nullValue());
    }

    @Test
    public void createValueShouldAddKeyIfNotExists() {

        //when
        boolean valueCreated = dynamoJobMetaRepository.createValue("myJobType", "someKey", "someValue");

        //then
        assertThat(valueCreated, is(true));
        String value = dynamoJobMetaRepository.getValue("myJobType", "someKey");
        assertThat(value, is("someValue"));
    }

    @Test
    public void createValueShouldNotAddKeyIfAlreadyExists() {
        //given
        dynamoJobMetaRepository.createValue("myJobType", "someKey", "someExistingValue");

        //when
        boolean valueCreated = dynamoJobMetaRepository.createValue("myJobType", "someKey", "someOtherValue");

        //then
        assertThat(valueCreated, is(false));
        String value = dynamoJobMetaRepository.getValue("myJobType", "someKey");
        assertThat(value, is("someExistingValue"));
    }

    @Test
    public void shouldSetDisabledWithComment() {
        //when
        dynamoJobMetaRepository.disable("someJobType", "someComment");

        //then
        String value = dynamoJobMetaRepository.getValue("someJobType", KEY_DISABLED);
        assertThat(value, is("someComment"));
    }

    @Test
    public void shouldSetDisabledWithoutComment() {
        //when
        dynamoJobMetaRepository.disable("someJobType", null);

        //then
        String value = dynamoJobMetaRepository.getValue("someJobType", KEY_DISABLED);
        assertThat(value, nullValue());
    }

    @Test
    public void shouldSetEnabled() {
        //given
        dynamoJobMetaRepository.disable("someJobType", "disabled");

        //when
        dynamoJobMetaRepository.enable("someJobType");

        //then
        String value = dynamoJobMetaRepository.getValue("someJobType", KEY_DISABLED);
        assertThat(value, nullValue());
    }

    @Test
    public void shouldClearRunningJob() {
        //given
        dynamoJobMetaRepository.setRunningJob("someJobType", "someJobId");

        //when
        dynamoJobMetaRepository.clearRunningJob("someJobType");

        //then
        String jobId = dynamoJobMetaRepository.getRunningJob("someJobType");
        assertThat(jobId, nullValue());
    }

    @Test
    public void shouldFindAllJobTypes() {
        //given
        dynamoJobMetaRepository.setRunningJob("someJobType", "someJobId1");
        dynamoJobMetaRepository.setRunningJob("someOtherJobType", "someJobId2");
        dynamoJobMetaRepository.enable("oneMoreJobType");

        //when
        Set<String> allJobTypes = dynamoJobMetaRepository.findAllJobTypes();

        //then
        assertThat(allJobTypes, contains("someJobType", "someOtherJobType", "oneMoreJobType"));
    }

    @Test
    public void shouldReturnJobMeta() {
        //given
        dynamoJobMetaRepository.setRunningJob("someJobType", "someJobId");
        dynamoJobMetaRepository.disable("someJobType", "because");
        dynamoJobMetaRepository.setValue("someJobType", "foo", "bar");

        //when
        JobMeta jobMeta = dynamoJobMetaRepository.getJobMeta("someJobType");

        //then
        assertThat(jobMeta.getJobType(), is("someJobType"));
        assertThat(jobMeta.isDisabled(), is(true));
        assertThat(jobMeta.getDisabledComment(), is("because"));
        assertThat(jobMeta.isRunning(), is(true));
        assertThat(jobMeta.getAll(), is(ImmutableMap.of("foo", "bar")));
    }

    @Test
    public void shouldSetDisabledCommentToEmptyStringWhenEnabled() {
        //given
        dynamoJobMetaRepository.setRunningJob("someJobType", "someJobId");
        dynamoJobMetaRepository.setValue("someJobType", "foo", "bar");

        //when
        JobMeta jobMeta = dynamoJobMetaRepository.getJobMeta("someJobType");

        //then
        assertThat(jobMeta.getJobType(), is("someJobType"));
        assertThat(jobMeta.isDisabled(), is(false));
        assertThat(jobMeta.getDisabledComment(), is(""));
    }

    @Test
    public void shouldReturnEmptyJobMetaWhenJobTypeDoesNotExist() {
        //when
        JobMeta jobMeta = dynamoJobMetaRepository.getJobMeta("someJobType");

        //then
        assertThat(jobMeta.getJobType(), is("someJobType"));
        assertThat(jobMeta.isDisabled(), is(false));
        assertThat(jobMeta.getDisabledComment(), is(""));
        assertThat(jobMeta.isRunning(), is(false));
        assertThat(jobMeta.getAll(), is(Collections.emptyMap()));
    }
}