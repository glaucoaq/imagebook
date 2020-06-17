package com.company.imagebook.entities;

import com.company.imagebook.common.ObjectId;
import java.net.URL;
import java.time.Instant;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EntityListeners;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Id;
import javax.validation.constraints.Max;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;
import lombok.Data;
import lombok.experimental.Accessors;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@Data
@Entity
@EntityListeners(AuditingEntityListener.class)
@Accessors(chain = true)
public class Image {

  public static final int MAX_DESCRIPTION_LENGTH = 120;

  public static final long MAX_CONTENT_SIZE = 500;

  @Id
  @Column(length = ObjectId.ID_LENGTH)
  private String id;

  @NotNull
  @Column(length = MAX_DESCRIPTION_LENGTH, nullable = false)
  private String description;

  @NotNull
  @Column(nullable = false)
  private URL imageUrl;

  @NotNull
  @Positive
  @Max(MAX_CONTENT_SIZE)
  @Column(nullable = false)
  private Integer contentSize;

  @NotNull
  @Enumerated(EnumType.STRING)
  @Column(length = 3, nullable = false)
  private ImageType imageType;

  @Column(nullable = false)
  @CreatedDate
  private Instant createdDate;

  @Column(nullable = false)
  @LastModifiedDate
  private Instant lastModifiedDate;
}
