#!/bin/sh

mvn clean verify
docker run -d --name s3mock -p 9090:9090 -p 9191:9191 -t adobe/s3mock
docker run --name mysqldb -p 3306:3306 -e MYSQL_ROOT_PASSWORD=secret -d mysql:5.7.22
mvn spring-boot:run
pushd ./frontend
ng e2e
popd
docker stop s3mock
docker stop mysqldb
docker container rm s3mock
docker container rm mysqldb

