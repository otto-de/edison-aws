package de.otto.edison.s3properties;

import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.auth.AWSCredentialsProviderChain;
import com.amazonaws.auth.EnvironmentVariableCredentialsProvider;
import com.amazonaws.auth.InstanceProfileCredentialsProvider;
import com.amazonaws.auth.profile.ProfileCredentialsProvider;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.ArrayList;
import java.util.List;

@Configuration
@EnableConfigurationProperties(S3Properties.class)
@ConditionalOnProperty(name = "edison.s3-properties.enabled", havingValue = "true")
public class S3Configuration {

    @Bean(name = "s3CredentialsProvider")
    public AWSCredentialsProvider s3CredentialsProvider(@Value("${edison.s3-properties.aws.profile}") final String awsProfile) {
        final List<AWSCredentialsProvider> providerList = new ArrayList<>();

        providerList.add(InstanceProfileCredentialsProvider.getInstance());
        providerList.add(new EnvironmentVariableCredentialsProvider());
        providerList.add(new ProfileCredentialsProvider(awsProfile));

        return new AWSCredentialsProviderChain(providerList);
    }

    @Bean
    public AmazonS3 s3Client(final AWSCredentialsProvider s3CredentialsProvider, @Value("${edison.s3-properties.aws.region}") final String region) {
        return AmazonS3Client.builder()
                .withRegion(region)
                .withCredentials(s3CredentialsProvider)
                .build();
    }
}
