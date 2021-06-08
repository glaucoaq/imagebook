package com.company.imagebook.services.storage.amazonaws;

import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.client.builder.AwsClientBuilder.EndpointConfiguration;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Slf4j
@Configuration
public class AmazonS3Config {

  public static final String BUCKET_NAME = "imagebook";

  @Value("${aws.s3.service-endpoint:@null}")
  private String endpoint;

  @Value("${aws.s3.access-key}")
  private String accessKey;

  @Value("${aws.s3.secret-key}")
  private String secretKey;

  @Value("${aws.s3.region}")
  private String region;

  @Bean
  AmazonS3 amazonS3() {
    val credentials = new BasicAWSCredentials(accessKey, secretKey);
    val builder = AmazonS3ClientBuilder
        .standard()
        .withCredentials(new AWSStaticCredentialsProvider(credentials));
    if (endpoint != null) {
      builder
          .withEndpointConfiguration(new EndpointConfiguration(endpoint, region))
          .withPathStyleAccessEnabled(true);
    } else {
      builder
          .withRegion(region);
    }
    val client = builder.build();
    if (!client.doesBucketExistV2(BUCKET_NAME)) {
      log.info("Bucket {} does not exist and will be create", BUCKET_NAME);
      client.createBucket(BUCKET_NAME);
      log.info("Bucket {} created.", BUCKET_NAME);
    }
    return client;
  }
}
