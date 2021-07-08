#!/usr/bin/env bash

docker login ${REG_HOST} -u ${REG_CREDS_USR} -p ${REG_CREDS_PSW}
docker build -t ${REG_HOST}/rm/test-camel:latest .
docker push ${REG_HOST}/rm/test-camel:latest
