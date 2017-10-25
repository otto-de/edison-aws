package de.otto.edison.paramstoreproperties;

import com.amazonaws.services.simplesystemsmanagement.AWSSimpleSystemsManagement;
import com.amazonaws.services.simplesystemsmanagement.AWSSimpleSystemsManagementClientBuilder;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(ParamStoreProperties.class)
@ConditionalOnProperty(name = "edison.aws.paramstore-properties.enabled", havingValue = "true")
public class ParamStoreConfiguration {

    @Bean
    public AWSSimpleSystemsManagement awsSSM() {
        return AWSSimpleSystemsManagementClientBuilder.defaultClient();
    }
}
