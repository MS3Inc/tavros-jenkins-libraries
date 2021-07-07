#!/usr/bin/env bash

mvn test
mvn clean package

# docker build -t dind-pongo:latest -f dind-pongo.Dockerfile .