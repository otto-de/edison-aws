package de.otto.edison.aws.dynamodb.togglz;

import de.otto.edison.aws.dynamodb.AbstractDynamoRepository;
import de.otto.edison.togglz.FeatureClassProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.togglz.core.Feature;
import org.togglz.core.repository.FeatureState;
import org.togglz.core.repository.StateRepository;
import org.togglz.core.user.UserProvider;
import software.amazon.awssdk.services.dynamodb.DynamoDBClient;
import software.amazon.awssdk.services.dynamodb.document.Item;
import software.amazon.awssdk.services.dynamodb.model.*;

import java.util.Map.Entry;
import java.util.Optional;

import static org.springframework.util.StringUtils.isEmpty;

@ConditionalOnMissingBean(StateRepository.class)
public class DynamoTogglzRepository extends AbstractDynamoRepository<FeatureState> implements StateRepository {

    private static final Logger LOG = LoggerFactory.getLogger(DynamoTogglzRepository.class);

    private static final String ENABLED = "enabled";
    private static final String STRATEGY = "strategy";
    private static final String PARAMETERS = "parameters";

    private final DynamoDBClient dynamoClient;
    private final FeatureClassProvider featureClassProvider;
    private final UserProvider userProvider;

    public DynamoTogglzRepository(final DynamoDBClient dynamoClient,
                                  final FeatureClassProvider featureClassProvider,
                                  final UserProvider userProvider) {
        this.dynamoClient = dynamoClient;
        this.featureClassProvider = featureClassProvider;
        this.userProvider = userProvider;
    }

    /**
     * Get the persisted state of a feature from the repository. If the repository doesn't contain any information regarding
     * this feature it must return <code>null</code>.
     *
     * @param feature The feature to read the state for
     * @return The persisted feature state or <code>null</code>
     */
    @Override
    public FeatureState getFeatureState(final Feature feature) {
        final Optional<FeatureState> featureState = findOne(feature.name());
        return featureState.orElse(null);
    }

    /**
     * Persist the supplied feature state. The repository implementation must ensure that subsequent calls to
     * {@link #getFeatureState(Feature)} return the same state as persisted using this method.
     *
     * @param featureState The feature state to persist
     * @throws UnsupportedOperationException if this state repository does not support updates
     */
    @Override
    public void setFeatureState(final FeatureState featureState) {
        createOrUpdate(featureState);
        LOG.info((!isEmpty(userProvider.getCurrentUser().getName()) ?
                "User '" + userProvider.getCurrentUser().getName() + "'" :
                "Unknown user")
                + (featureState.isEnabled() ? " enabled " : " disabled ") + "feature " + featureState.getFeature().name());
    }

    @Override
    protected String tableName() {
        return "togglz";
    }

    @Override
    protected String keyOf(final FeatureState value) {
        return value.getFeature().name();
    }

    @Override
    protected Item encode(final FeatureState value) {
        final Item item = new Item().withPrimaryKey(getKeyFieldName(), keyOf(value))
                .withBoolean(ENABLED, value.isEnabled());
        if (value.getStrategyId() != null) {
            item.withString(STRATEGY, value.getStrategyId());
        }
        if (value.getParameterMap() != null) {
            item.withMap(PARAMETERS, value.getParameterMap());
        }
        return item;
    }

    @Override
    protected FeatureState decode(final Item item) {
        final FeatureState featureState = new FeatureState(resolveEnumValue(item.getString(getKeyFieldName())));
        featureState.setEnabled(item.getBoolean(ENABLED));
        featureState.setStrategyId(item.getString(STRATEGY));
        for (final Entry<String, String> parameter : item.<String>getMap(PARAMETERS).entrySet()) {
            featureState.setParameter(parameter.getKey(), parameter.getValue());
        }
        return featureState;
    }

    @Override
    protected String getKeyFieldName() {
        return "id";
    }

    private Feature resolveEnumValue(final String name) {
        final Class enumType = featureClassProvider.getFeatureClass();
        return (Feature) Enum.valueOf(enumType, name);
    }

    void createTable() {
        if (!dynamoClient.listTables().tableNames().contains(table().getTableName())) {
            dynamoClient.createTable(CreateTableRequest.builder()
                    .tableName(
                            table().getTableName())
                    .provisionedThroughput(
                            ProvisionedThroughput.builder().readCapacityUnits(1L).writeCapacityUnits(1L).build())
                    .attributeDefinitions(
                            AttributeDefinition.builder().attributeName(getKeyFieldName()).attributeType(ScalarAttributeType.S).build())
                    .keySchema(
                            KeySchemaElement.builder().attributeName(getKeyFieldName()).keyType(KeyType.HASH).build())
                    .build()
            );
        }
    }
}
