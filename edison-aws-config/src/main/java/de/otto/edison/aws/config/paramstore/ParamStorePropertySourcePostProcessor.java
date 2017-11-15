package de.otto.edison.aws.config.paramstore;

import de.otto.edison.aws.configuration.AwsConfiguration;
import de.otto.edison.aws.configuration.AwsProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.EnvironmentAware;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.Environment;
import org.springframework.core.env.MutablePropertySources;
import org.springframework.core.env.PropertiesPropertySource;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.services.ssm.SSMClient;
import software.amazon.awssdk.services.ssm.model.GetParametersByPathRequest;
import software.amazon.awssdk.services.ssm.model.GetParametersByPathResponse;

import java.util.Properties;

import static java.util.Objects.requireNonNull;
import static software.amazon.awssdk.core.regions.Region.EU_CENTRAL_1;
import static software.amazon.awssdk.services.ssm.model.ParameterType.SECURE_STRING;

@Component
@ConditionalOnProperty(name = "edison.aws.config.paramstore.enabled", havingValue = "true")
public class ParamStorePropertySourcePostProcessor implements BeanFactoryPostProcessor, EnvironmentAware {

    private static final Logger LOG = LoggerFactory.getLogger(ParamStorePropertySourcePostProcessor.class);

    private static final String PARAMETER_STORE_PROPERTY_SOURCE = "parameterStorePropertySource";
    private ParamStoreConfigProperties properties;
    private AwsProperties awsProperties;

    @Override
    public void postProcessBeanFactory(final ConfigurableListableBeanFactory beanFactory) throws BeansException {
        final AwsConfiguration awsConfig = new AwsConfiguration();

        final SSMClient awsSSM = SSMClient.builder()
                .credentialsProvider(awsConfig.awsCredentialsProvider(awsProperties))
                .build();

        final GetParametersByPathRequest request = GetParametersByPathRequest
                .builder()
                .path(properties.getPath())
                .withDecryption(true)
                .build();

        final GetParametersByPathResponse result = awsSSM.getParametersByPath(request);

        final Properties propertiesSource = new Properties();
        result.parameters().forEach(p -> {
            final String name = p.name().substring(properties.getPath().length() + 1);
            final String loggingValue = SECURE_STRING == p.type() ? "*****" : p.value();
            LOG.info("Loaded '" + name + "' from ParametersStore, value='" + loggingValue + "', length=" + p.value().length());

            propertiesSource.setProperty(name, p.value());
        });

        final ConfigurableEnvironment env = beanFactory.getBean(ConfigurableEnvironment.class);
        final MutablePropertySources propertySources = env.getPropertySources();
        propertySources.addLast(new PropertiesPropertySource(PARAMETER_STORE_PROPERTY_SOURCE, propertiesSource));
    }

    @Override
    public void setEnvironment(final Environment environment) {
        awsProperties = new AwsProperties();
        awsProperties.setProfile(environment.getProperty("aws.profile", "default"));
        awsProperties.setRegion(environment.getProperty("aws.region", EU_CENTRAL_1.value()));

        final String pathProperty = "edison.aws.config.paramstore.path";
        final String path = requireNonNull(environment.getProperty(pathProperty),
                "Property '" + pathProperty + "' must not be null");

        properties = new ParamStoreConfigProperties();
        properties.setPath(path);
    }
}
