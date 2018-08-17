package de.otto.edison.aws.config.s3;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import software.amazon.awssdk.core.sync.ResponseTransformer;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;

import java.util.Properties;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

public class S3BucketPropertyReaderTest {

    @Mock
    private S3Client s3Client;

    @Mock
    private S3ConfigProperties s3ConfigProperties;

    @InjectMocks
    private S3BucketPropertyReader testee;

    @Before
    public void setUp() {
        initMocks(this);
    }

    @SuppressWarnings("unchecked")
    @Test
    public void shouldReadPropertiesFromS3() {
        // given
        when(s3ConfigProperties.getBucketname()).thenReturn("someBucket");
        when(s3ConfigProperties.getFilename()).thenReturn("someFileName");

        final Properties properties = new Properties();
        properties.put("foo", "bar");
        properties.put("key", "value");

        when(s3Client.getObject(any(GetObjectRequest.class), (ResponseTransformer<GetObjectResponse, Properties>) any(ResponseTransformer.class))).thenReturn(properties);

        // when
        final Properties propertiesFromS3 = testee.getPropertiesFromS3();

        //then
        assertThat(propertiesFromS3.getProperty("foo"), is("bar"));
        assertThat(propertiesFromS3.getProperty("key"), is("value"));
    }
}
