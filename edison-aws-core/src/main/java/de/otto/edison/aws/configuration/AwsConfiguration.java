package de.otto.edison.aws.configuration;

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.*;

@Configuration
@EnableConfigurationProperties(AwsProperties.class)
public class AwsConfiguration {

    @Bean
    @ConditionalOnMissingBean(AwsCredentialsProvider.class)
    public AwsCredentialsProvider awsCredentialsProvider(final AwsProperties awsProperties) {
        return AwsCredentialsProviderChain
                .builder()
                .credentialsProviders(
                        // instance profile is also needed for people not using ecs but directly using ec2 instances!!
                        ContainerCredentialsProvider.builder().build(),
                        InstanceProfileCredentialsProvider.builder().build(),
                        EnvironmentVariableCredentialsProvider.create(),
                        ProfileCredentialsProvider
                                .builder()
                                .profileName(awsProperties.getProfile())
                                .build())
                .build();
    }
}
