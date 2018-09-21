package de.otto.edison.aws.dynamodb.jobs;

import com.amazonaws.services.dynamodbv2.local.main.ServerRunner;
import com.amazonaws.services.dynamodbv2.local.server.DynamoDBProxyServer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;

import java.net.URI;

@Configuration
public class DynamoDbTestConfiguration {

    @Bean
    @Profile("test")
    @Primary
    public DynamoDbClient dynamoDBClient() {
        return DynamoDbClient.builder()
                .endpointOverride(URI.create("http://localhost:8000"))
                .credentialsProvider(StaticCredentialsProvider.create(AwsBasicCredentials.create("foobar", "foobar")))
                .build();
    }

    @Bean
    @Profile("test")
    public DynamoDBProxyServer dynamoDBProxyServer() throws Exception {
        DynamoDBProxyServer server = ServerRunner.createServerFromCommandLineArgs(new String[]{"-inMemory"});
        server.start();
        return server;
    }

}
