package de.otto.edison.aws.configuration.ldap;

import de.otto.edison.authentication.configuration.LdapProperties;
import de.otto.edison.authentication.connection.LdapConnectionFactory;
import de.otto.edison.authentication.connection.SSLLdapConnectionFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(LdapProperties.class)
@ConditionalOnProperty(prefix = "edison.ldap", name = "enabled", havingValue = "true")
public class AwsLdapConfiguration {

    @Bean
    public LdapConnectionFactory ldapConnectionFactory(final LdapProperties ldapProperties) {
        return new SSLLdapConnectionFactory(ldapProperties);
    }
}
