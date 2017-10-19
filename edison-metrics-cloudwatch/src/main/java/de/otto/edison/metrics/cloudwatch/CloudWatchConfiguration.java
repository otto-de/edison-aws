package de.otto.edison.metrics.cloudwatch;

import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.auth.AWSCredentialsProviderChain;
import com.amazonaws.auth.EnvironmentVariableCredentialsProvider;
import com.amazonaws.auth.InstanceProfileCredentialsProvider;
import com.amazonaws.auth.profile.ProfileCredentialsProvider;
import com.amazonaws.services.cloudwatch.AmazonCloudWatchAsync;
import com.amazonaws.services.cloudwatch.AmazonCloudWatchAsyncClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.ArrayList;
import java.util.List;

@Configuration
public class CloudWatchConfiguration {

  @Bean(name = "cloudWatchCredentialsProvider")
  public AWSCredentialsProvider cloudWatchCredentialsProvider(
    @Value("${edison.metrics.cloudWatch.aws.profile}") final String awsProfile) {
    final List<AWSCredentialsProvider> providerList = new ArrayList<>();

    providerList.add(InstanceProfileCredentialsProvider.getInstance());
    providerList.add(new EnvironmentVariableCredentialsProvider());
    providerList.add(new ProfileCredentialsProvider(awsProfile));

    return new AWSCredentialsProviderChain(providerList);
  }

  @Bean
  public AmazonCloudWatchAsync cloudWatchAsync(final AWSCredentialsProvider cloudWatchCredentialsProvider,
                                               @Value("${edison.metrics.cloudWatch.aws.region}") final String region) {
    return AmazonCloudWatchAsyncClient.asyncBuilder()
      .withRegion(region)
      .withCredentials(cloudWatchCredentialsProvider)
      .build();
  }
}
