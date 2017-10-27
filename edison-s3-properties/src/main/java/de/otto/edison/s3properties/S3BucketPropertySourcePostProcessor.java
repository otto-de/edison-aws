package de.otto.edison.s3properties;

import com.amazonaws.services.s3.AmazonS3;
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

import static java.util.Objects.requireNonNull;

@Component
@ConditionalOnProperty(name = "edison.aws.s3-properties.enabled", havingValue = "true")
public class S3BucketPropertySourcePostProcessor implements BeanFactoryPostProcessor, EnvironmentAware {

    private static final String BUCKET_PROPERTY_SOURCE = "bucketPropertySource";
    private static final String EDISON_S3_PROPERTIES_BUCKETNAME = "edison.aws.s3-properties.bucketname";
    private static final String EDISON_S3_PROPERTIES_FILENAME = "edison.aws.s3-properties.filename";
    private static final String EDISON_S3_PROPERTIES_REGION = "edison.aws.s3-properties.aws.region";
    private String awsProfile;
    private S3Properties properties;

    @Override
    public void postProcessBeanFactory(final ConfigurableListableBeanFactory beanFactory) throws BeansException {
        final S3Configuration s3Configuration = new S3Configuration();
        final ConfigurableEnvironment env = beanFactory.getBean(ConfigurableEnvironment.class);
        final String region = requireNonNull(env.getProperty(EDISON_S3_PROPERTIES_REGION),
                "property '" + EDISON_S3_PROPERTIES_REGION + "' must not be null");
        final AmazonS3 s3Client = s3Configuration.s3Client(s3Configuration.s3CredentialsProvider(awsProfile), region);

        final S3BucketPropertyReader s3BucketPropertyReader = new S3BucketPropertyReader(s3Client, properties);

        final MutablePropertySources propertySources = env.getPropertySources();
        propertySources.addLast(new PropertiesPropertySource(BUCKET_PROPERTY_SOURCE, s3BucketPropertyReader.getPropertiesFromS3()));
    }

    @Override
    public void setEnvironment(final Environment environment) {
        awsProfile = environment.getProperty("edison.aws.s3-properties.aws.profile", "default");

        final String bucketName = requireNonNull(environment.getProperty(EDISON_S3_PROPERTIES_BUCKETNAME),
                "property '" + EDISON_S3_PROPERTIES_BUCKETNAME + "' must not be null");
        final String filename = requireNonNull(environment.getProperty(EDISON_S3_PROPERTIES_FILENAME),
                "property '" + EDISON_S3_PROPERTIES_FILENAME + "' must not be null");

        properties = new S3Properties();
        properties.setBucketname(bucketName);
        properties.setFilename(filename);
    }
}
