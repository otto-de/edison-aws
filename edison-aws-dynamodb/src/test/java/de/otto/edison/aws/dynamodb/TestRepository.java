package de.otto.edison.aws.dynamodb;

import org.springframework.stereotype.Repository;
import software.amazon.awssdk.services.dynamodb.DynamoDBClient;
import software.amazon.awssdk.services.dynamodb.document.Item;
import software.amazon.awssdk.services.dynamodb.model.*;

import java.util.UUID;

@Repository
public class TestRepository extends AbstractDynamoRepository<TestObject> {

    private final DynamoDBClient dynamoClient;

    public TestRepository(final DynamoDBClient dynamoClient) {
        this.dynamoClient = dynamoClient;
    }

    void createTable() {
        if (!dynamoClient.listTables().tableNames().contains(table().getTableName())) {
            dynamoClient.createTable(CreateTableRequest.builder()
                    .tableName(table().getTableName())
                    .provisionedThroughput(ProvisionedThroughput.builder().readCapacityUnits(1L).writeCapacityUnits(1L).build())
                    .attributeDefinitions(AttributeDefinition.builder().attributeName(getKeyFieldName()).attributeType(ScalarAttributeType.S).build())
                    .keySchema(KeySchemaElement.builder().attributeName(getKeyFieldName()).keyType(KeyType.HASH).build())
                    .build());
        }
    }

    @Override
    protected String tableName() {
        return "test";
    }

    @Override
    protected String keyOf(final TestObject value) {
        return value.getId();
    }

    @Override
    protected Item encode(final TestObject testObject) {
        return new Item()
                .withPrimaryKey("id", testObject.getId())
                .withString("eTag", UUID.randomUUID().toString())
                .with("value", testObject.getValue());
    }

    @Override
    protected TestObject decode(final Item item) {
        return new TestObject(item.getString("id"), item.getString("value"), item.getString("eTag"));
    }

    @Override
    protected String getKeyFieldName() {
        return "id";
    }
}
