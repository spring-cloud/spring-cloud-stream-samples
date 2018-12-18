package demo.config;

import com.amazonaws.ClientConfiguration;
import com.amazonaws.SDKGlobalConfiguration;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBAsync;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBAsyncClient;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient;
import com.amazonaws.services.kinesis.AmazonKinesis;
import com.amazonaws.services.kinesis.AmazonKinesisAsyncClientBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

@Configuration
public class AmazonConfig {

    @Primary
    @Bean
    public AmazonKinesis amazonKinesis() {
        System.setProperty(SDKGlobalConfiguration.AWS_CBOR_DISABLE_SYSTEM_PROPERTY, "true");

        AmazonKinesis a = AmazonKinesisAsyncClientBuilder.standard()
                .withEndpointConfiguration(
                        new AwsClientBuilder.EndpointConfiguration("http://localhost:4568",
                                Regions.DEFAULT_REGION.getName()))
                .withCredentials(new AWSStaticCredentialsProvider(new BasicAWSCredentials("foo", "bar")))
                .build();

       // a.createStream("test_stream",2);
        return a;
    }

    @Primary
    @Bean
    public AmazonDynamoDBAsync amazonDynamoDB() {
        return AmazonDynamoDBAsyncClient.asyncBuilder().withEndpointConfiguration(
                new AwsClientBuilder.EndpointConfiguration("http://localhost:4569", Regions.DEFAULT_REGION.getName())
        ).withCredentials(new AWSStaticCredentialsProvider(new BasicAWSCredentials("foo", "bar"))).build();
    }

}
