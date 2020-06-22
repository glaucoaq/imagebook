package com.company.imagebook.controllers;

import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.AnonymousAWSCredentials;
import com.amazonaws.client.builder.AwsClientBuilder.EndpointConfiguration;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.company.imagebook.services.storage.amazonaws.AmazonS3Config;
import lombok.val;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

@Configuration
class S3MockConfig {

  public static final int S3MOCK_PORT = 9090;

  public static final String S3MOCK_IMAGE = "adobe/s3mock:latest";

  @Bean
  @Primary
  AmazonS3 mockAmazonS3(@Value("${aws.s3.service-endpoint}") String endpointUrl,
      @Value("${aws.s3.region}") String region) {
    val credentials = new AnonymousAWSCredentials();
    val endpoint = new EndpointConfiguration(endpointUrl, region);
    val client = AmazonS3ClientBuilder
        .standard()
        .withPathStyleAccessEnabled(true)
        .withCredentials(new AWSStaticCredentialsProvider(credentials))
        .withEndpointConfiguration(endpoint)
        .build();
    client.createBucket(AmazonS3Config.BUCKET_NAME);
    return client;
  }
}
