package de.otto.edison.aws.dynamodb.status;

import de.otto.edison.status.domain.StatusDetail;
import de.otto.edison.status.indicator.StatusDetailIndicator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.services.dynamodb.DynamoDBClient;

import static de.otto.edison.status.domain.Status.ERROR;
import static de.otto.edison.status.domain.Status.OK;

@Component
@ConditionalOnProperty(prefix = "edison.aws.dynamo.status", name = "enabled", havingValue = "true", matchIfMissing = true)
public class DynamoDbStatusDetailIndicator implements StatusDetailIndicator {

    private static final String NAME = "DynamoDB Status";

    private final DynamoDBClient dynamoClient;

    @Autowired
    public DynamoDbStatusDetailIndicator(final DynamoDBClient dynamoClient) {
        this.dynamoClient = dynamoClient;
    }

    @Override
    public StatusDetail statusDetail() {
        try {
            dynamoClient.listTables();
            return StatusDetail.statusDetail(NAME, OK, "Dynamo database is reachable.");
        } catch (final RuntimeException e) {
            return StatusDetail.statusDetail(NAME, ERROR, "Error accessing DynamoDB: " + e.getMessage());
        }
    }
}
