package de.otto.edison.paramstoreproperties;

import com.amazonaws.services.simplesystemsmanagement.AWSSimpleSystemsManagement;
import com.amazonaws.services.simplesystemsmanagement.AWSSimpleSystemsManagementClientBuilder;
import com.amazonaws.services.simplesystemsmanagement.model.GetParametersByPathRequest;
import com.amazonaws.services.simplesystemsmanagement.model.GetParametersByPathResult;
import com.amazonaws.services.simplesystemsmanagement.model.ParameterType;
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

import java.util.Properties;

import static java.util.Objects.requireNonNull;

@Component
@ConditionalOnProperty(name = "edison.aws.paramstore-properties.enabled", havingValue = "true")
public class ParamStorePropertySourcePostProcessor implements BeanFactoryPostProcessor, EnvironmentAware {

    private static final Logger LOG =  LoggerFactory.getLogger(ParamStorePropertySourcePostProcessor.class);

    private static final String PARAMETER_STORE_PROPERTY_SOURCE = "parameterStorePropertySource";
    private ParamStoreProperties properties;

    @Override
    public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {
        AWSSimpleSystemsManagement awsSSM = AWSSimpleSystemsManagementClientBuilder.defaultClient();

        GetParametersByPathRequest request = new GetParametersByPathRequest()
                .withPath(properties.getPath())
                .withWithDecryption(true);

        GetParametersByPathResult result = awsSSM.getParametersByPath(request);

        Properties propertiesSource = new Properties();
        result.getParameters().forEach(p -> {
            String name = p.getName().substring(properties.getPath().length() + 1);
            String loggingValue = ParameterType.SecureString.toString().equals(p.getType()) ? "*****" : p.getValue();
            LOG.info("Loaded '" + name + "' from ParametersStore, value='" + loggingValue + "', length=" + p.getValue().length());

            propertiesSource.setProperty(name, p.getValue());
        });

        final ConfigurableEnvironment env = beanFactory.getBean(ConfigurableEnvironment.class);
        final MutablePropertySources propertySources = env.getPropertySources();
        propertySources.addLast(new PropertiesPropertySource(PARAMETER_STORE_PROPERTY_SOURCE, propertiesSource));
    }

    @Override
    public void setEnvironment(Environment environment) {
        String pathProperty = "edison.aws.paramstore-properties.path";
        String path = requireNonNull(environment.getProperty(pathProperty),
                "Property '" + pathProperty + "' must not be null");

        properties = new ParamStoreProperties();
        properties.setPath(path);
    }
}
