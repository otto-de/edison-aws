package de.otto.edison.aws.dynamodb.status;

import de.otto.edison.status.domain.StatusDetail;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import software.amazon.awssdk.services.dynamodb.DynamoDBClient;

import static de.otto.edison.status.domain.Status.ERROR;
import static de.otto.edison.status.domain.Status.OK;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class DynamoDbStatusDetailIndicatorTest {

    @Mock
    private DynamoDBClient dynamoClient;

    @InjectMocks
    private DynamoDbStatusDetailIndicator dynamoDbStatusDetailIndicator;

    @Test
    public void shouldReturnOKStatus() throws Exception {
        // when
        final StatusDetail statusDetail = dynamoDbStatusDetailIndicator.statusDetail();
        // then
        assertThat(statusDetail.getStatus(), is(OK));
    }

    @Test
    public void shouldReturnErrorStatusWhenDatabaseThrowsException() throws Exception {
        // given
        when(dynamoClient.listTables()).thenThrow(new RuntimeException("Kawummmmm!!!"));
        // when
        final StatusDetail statusDetail = dynamoDbStatusDetailIndicator.statusDetail();
        // then
        assertThat(statusDetail.getStatus(), is(ERROR));
        assertThat(statusDetail.getMessage().contains("Kawummmmm!!!"), is(true));
    }
}