package de.otto.edison.aws.config.paramstore;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.core.env.*;
import org.springframework.mock.env.MockEnvironment;
import software.amazon.awssdk.services.ssm.SSMClient;
import software.amazon.awssdk.services.ssm.model.GetParametersByPathRequest;
import software.amazon.awssdk.services.ssm.model.GetParametersByPathResponse;
import software.amazon.awssdk.services.ssm.model.Parameter;
import software.amazon.awssdk.services.ssm.model.ParameterMetadata;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

public class ParamStorePropertySourcePostProcessorTest {

    @Mock
    private Environment environment;

    @Mock
    private ConfigurableListableBeanFactory beanFactory;

    @Mock
    private SSMClient ssmClient;

    @InjectMocks
    private ParamStorePropertySourcePostProcessor postProcessor;

    @Before
    public void setUp() throws Exception {
        initMocks(this);
        when(environment.getProperty("edison.aws.config.paramstore.path")).thenReturn("/the/path");
        postProcessor.setEnvironment(environment);
        postProcessor.setSsmClient(ssmClient);
    }

    @Test
    public void shouldReadPropertiesFromSsm() {
        // given
        final GetParametersByPathResponse resultMock = mock(GetParametersByPathResponse.class);
        final List<Parameter> parameters = new ArrayList<>();
        parameters.add(Parameter.builder().name("/the/path/param1").value("val1").build());
        parameters.add(Parameter.builder().name("/the/path/param2").value("val2").build());
        when(resultMock.parameters()).thenReturn(parameters);
        when(ssmClient.getParametersByPath(any(GetParametersByPathRequest.class))).thenReturn(resultMock);
        final ConfigurableEnvironment envMock = mock(ConfigurableEnvironment.class);
        when(beanFactory.getBean(ConfigurableEnvironment.class)).thenReturn(envMock);
        final MutablePropertySources propertySourcesMock = mock(MutablePropertySources.class);
        when(envMock.getPropertySources()).thenReturn(propertySourcesMock);

        final GetParametersByPathRequest expectedRequest = GetParametersByPathRequest
                .builder()
                .path("/the/path")
                .withDecryption(true)
                .recursive(true)
                .build();
        final Properties propertiesSource = new Properties();
        propertiesSource.setProperty("/the/path/param1", "val1");
        propertiesSource.setProperty("/the/path/param2", "val2");
        final PropertiesPropertySource parameterStorePropertySource =
                new PropertiesPropertySource("parameterStorePropertySource", propertiesSource);

        // when
        postProcessor.postProcessBeanFactory(beanFactory);

        // then
        verify(ssmClient).getParametersByPath(eq(expectedRequest));
        verify(propertySourcesMock).addFirst(parameterStorePropertySource);
    }

    @Test
    public void shouldReadPropertiesFromSsmWithPaging() {
        // given
        final GetParametersByPathResponse firstPage = mock(GetParametersByPathResponse.class);
        final List<Parameter> parametersFirstPage = new ArrayList<>();
        parametersFirstPage.add(Parameter.builder().name("/the/path/param1").value("val1").build());
        when(firstPage.parameters()).thenReturn(parametersFirstPage);
        when(firstPage.nextToken()).thenReturn("firstPageNextToken");
        GetParametersByPathRequest firstRequest = GetParametersByPathRequest.builder()
                .path("/the/path")
                .withDecryption(true)
                .recursive(true)
                .nextToken(null)
                .build();
        when(ssmClient.getParametersByPath(eq(firstRequest))).thenReturn(firstPage);

        final GetParametersByPathResponse secondPage = mock(GetParametersByPathResponse.class);
        final List<Parameter> parametersSecondPage = new ArrayList<>();
        parametersSecondPage.add(Parameter.builder().name("/the/path/param2").value("val2").build());
        when(secondPage.parameters()).thenReturn(parametersSecondPage);
        when(secondPage.nextToken()).thenReturn(null);
        GetParametersByPathRequest secondRequest = GetParametersByPathRequest.builder()
                .path("/the/path")
                .withDecryption(true)
                .recursive(true)
                .nextToken("firstPageNextToken")
                .build();
        when(ssmClient.getParametersByPath(eq(secondRequest))).thenReturn(secondPage);

        final ConfigurableEnvironment envMock = new MockEnvironment();
        when(beanFactory.getBean(ConfigurableEnvironment.class)).thenReturn(envMock);

        // when
        postProcessor.postProcessBeanFactory(beanFactory);

        // then
        PropertySource<?> propertySource = envMock.getPropertySources().get("parameterStorePropertySource");
        assertThat(propertySource.getProperty("/the/path/param1"), is("val1"));
        assertThat(propertySource.getProperty("/the/path/param2"), is("val2"));
    }
}
