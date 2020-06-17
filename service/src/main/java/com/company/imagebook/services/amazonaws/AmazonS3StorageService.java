package com.company.imagebook.services.amazonaws;

import static com.company.imagebook.services.amazonaws.AmazonS3Config.BUCKET_NAME;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.company.imagebook.services.StorageService;
import java.io.ByteArrayInputStream;
import java.net.URL;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class AmazonS3StorageService implements StorageService {

  @Autowired
  private AmazonS3 amazonS3;

  @Override
  public URL putObject(String key, byte[] bytes) {
    val metadata = new ObjectMetadata();
    metadata.setContentLength(bytes.length);

    log.info("Saving object with key {}", key);
    amazonS3.putObject(BUCKET_NAME, key, new ByteArrayInputStream(bytes), metadata);
    log.info("Success");

    return amazonS3.getUrl(BUCKET_NAME, key);
  }

  @Override
  public void removeObject(String key) {
    log.info("Saving object with key {}", key);
    amazonS3.deleteObject(BUCKET_NAME, key);
    log.info("Success");
  }
}
