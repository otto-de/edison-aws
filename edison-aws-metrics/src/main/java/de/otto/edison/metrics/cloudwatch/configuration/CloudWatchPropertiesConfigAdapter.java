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

import de.otto.edison.metrics.cloudwatch.CloudWatchProperties;
import io.micrometer.cloudwatch2.CloudWatchConfig;
import org.springframework.boot.actuate.autoconfigure.metrics.export.properties.StepRegistryPropertiesConfigAdapter;

/**
 * Adapter to convert {@link CloudWatchProperties} to a {@link CloudWatchConfig}.
 *
 * @author Jon Schneider
 * @author Dawid Kublik
 * @since 2.0.0
 */
class CloudWatchPropertiesConfigAdapter
		extends StepRegistryPropertiesConfigAdapter<CloudWatchProperties>
		implements CloudWatchConfig {

	CloudWatchPropertiesConfigAdapter(CloudWatchProperties properties) {
		super(properties);
	}

	@Override
	public String namespace() {
		return get(CloudWatchProperties::getNamespace, CloudWatchConfig.super::namespace);
	}

	@Override
	public int batchSize() {
		return get(CloudWatchProperties::getBatchSize, CloudWatchConfig.super::batchSize);
	}

}
