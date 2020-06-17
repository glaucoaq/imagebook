package com.company.imagebook;

import static org.springframework.context.annotation.FilterType.ASSIGNABLE_TYPE;

import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.AnonymousAWSCredentials;
import com.amazonaws.client.builder.AwsClientBuilder.EndpointConfiguration;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.company.imagebook.services.amazonaws.AmazonS3Config;
import io.findify.s3mock.S3Mock;
import lombok.val;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.ComponentScan.Filter;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@Configuration
@EnableJpaAuditing
@EnableAutoConfiguration
@ComponentScan(
    basePackages = "com.company.imagebook",
    excludeFilters = @Filter(type = ASSIGNABLE_TYPE, classes = AmazonS3Config.class)
)
public class IntegrationTestConfig {

  public static final int S3MOCK_PORT = 9090;

  public static final String S3MOCK_REGION = "us-east-1";

  @Bean
  S3Mock s3Mock() {
    return new S3Mock.Builder()
        .withPort(S3MOCK_PORT)
        .withInMemoryBackend()
        .build();
  }

  @Bean
  @Primary
  AmazonS3 amazonS3(@Autowired S3Mock s3Mock) {
    val credentials = new AnonymousAWSCredentials();
    val endpoint = new EndpointConfiguration("http://localhost:" + S3MOCK_PORT, S3MOCK_REGION);
    val client = AmazonS3ClientBuilder
        .standard()
        .withPathStyleAccessEnabled(true)
        .withCredentials(new AWSStaticCredentialsProvider(credentials))
        .withEndpointConfiguration(endpoint)
        .build();
    s3Mock.start();
    client.createBucket(AmazonS3Config.BUCKET_NAME);
    return client;
  }
}
