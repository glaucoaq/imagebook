CREATE TABLE image (
  id VARCHAR(22) NOT NULL,
  content_size INTEGER NOT NULL,
  image_url VARCHAR(255) NOT NULL,
  created_date DATETIME(6) NOT NULL,
  description VARCHAR(120) NOT NULL,
  image_type VARCHAR(3) NOT NULL,
  last_modified_date DATETIME(6) NOT NULL,
  primary key (id)
) engine=InnoDB
