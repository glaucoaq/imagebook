delete from image;

insert into image
  (id, content_size, image_url, description, image_type, created_date, last_modified_date)
values
  ('key0', 10, 'http://store/key0', '0 all even', 'PNG', now(), now());

insert into image
  (id, content_size, image_url, description, image_type, created_date, last_modified_date)
values
  ('key1', 100, 'http://store/key1', '1 all odd', 'JPG', now(), now());

insert into image
  (id, content_size, image_url, description, image_type, created_date, last_modified_date)
values
  ('key2', 500, 'http://store/key2', '2 all even', 'JPG', now(), now());
