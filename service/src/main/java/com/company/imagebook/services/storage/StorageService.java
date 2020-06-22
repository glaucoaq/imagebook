package com.company.imagebook.services.storage;

import java.net.URL;

public interface StorageService {

  URL putObject(String key, byte[] bytes);

  void removeObject(String key);
}
