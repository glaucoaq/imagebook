package com.company.imagebook.common;

import java.nio.ByteBuffer;
import java.util.Base64;
import java.util.UUID;
import lombok.experimental.UtilityClass;
import lombok.val;

@UtilityClass
public class ObjectId {

  public static final int ID_LENGTH = 22;

  public String randomObjectId() {
    val id = UUID.randomUUID();
    val buffer = ByteBuffer.wrap(new byte[16]);
    buffer.putLong(id.getMostSignificantBits());
    buffer.putLong(id.getLeastSignificantBits());
    return Base64.getUrlEncoder()
        .withoutPadding()
        .encodeToString(buffer.array());
  }
}
