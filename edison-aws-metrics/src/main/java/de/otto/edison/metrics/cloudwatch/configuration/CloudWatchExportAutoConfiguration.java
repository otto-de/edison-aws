/*
 * Copyright 2013-2019 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package de.otto.edison.metrics.cloudwatch.configuration;

import de.otto.edison.aws.configuration.AwsProperties;
import de.otto.edison.metrics.cloudwatch.CloudWatchMetricFilter;
import de.otto.edison.metrics.cloudwatch.CloudWatchMetricsProperties;
import de.otto.edison.metrics.cloudwatch.CloudWatchProperties;
import de.otto.edison.metrics.cloudwatch.FilteredCloudWatchMeterRegistry;
import io.micrometer.cloudwatch2.CloudWatchConfig;
import io.micrometer.cloudwatch2.CloudWatchMeterRegistry;
import io.micrometer.core.instrument.Clock;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.AwsCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.cloudwatch.CloudWatchAsyncClient;


/**
 * Configuration for exporting metrics to CloudWatch.
 *
 * @author Jon Schneider
 * @author Dawid Kublik
 * @author Jan Sauer
 * @since 2.0.0
 */
@Configuration
@EnableConfigurationProperties({CloudWatchProperties.class, CloudWatchMetricsProperties.class})
@ConditionalOnProperty(prefix = "management.metrics.export.cloudwatch", name = "namespace")
@ConditionalOnClass({ CloudWatchMeterRegistry.class })
public class CloudWatchExportAutoConfiguration {

	@Bean
	@ConditionalOnProperty(value = "management.metrics.export.cloudwatch.enabled", matchIfMissing = true)
	public CloudWatchMeterRegistry cloudWatchMeterRegistry(final CloudWatchMetricFilter cloudWatchMetricFilter,
															 final CloudWatchConfig cloudWatchConfig,
                                                           final Clock micrometerClock,
                                                           final CloudWatchAsyncClient cloudWatchAsyncClient) {
		return new FilteredCloudWatchMeterRegistry(cloudWatchMetricFilter, cloudWatchConfig, micrometerClock, cloudWatchAsyncClient);
	}

    @Bean
    public CloudWatchMetricFilter cloudWatchMetricFilter(
            final CloudWatchMetricsProperties metricsProperties) {
        final CloudWatchMetricFilter cloudWatchMetricFilter = new CloudWatchMetricFilter(metricsProperties);
        return cloudWatchMetricFilter;
    }

	@Bean
	@ConditionalOnMissingBean
	public CloudWatchConfig cloudWatchConfig(final CloudWatchProperties cloudWatchProperties) {
		return new CloudWatchPropertiesConfigAdapter(cloudWatchProperties);
	}

	@Bean
	@ConditionalOnMissingBean
	public Clock micrometerClock() {
		return Clock.SYSTEM;
	}

    @Bean
    @ConditionalOnMissingBean
    public CloudWatchAsyncClient cloudWatchAsyncClient(final AwsCredentialsProvider credentialsProvider, final AwsProperties awsProperties) {
        return CloudWatchAsyncClient.builder()
            .credentialsProvider(credentialsProvider)
            .region(Region.of(awsProperties.getRegion()))
            .build();
    }

}
