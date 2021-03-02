#!/usr/bin/env bash

set -o nounset
set -o errexit

group=${GROUP_ID}

if [[ group == *"-"* ]]; then
  echo group && mvn archetype:generate \
  -DarchetypeGroupId=com.ms3-inc.tavros \
  -DarchetypeArtifactId=camel-openapi-archetype \
  -DarchetypeVersion=0.2.6 \
  -DpackageInPathFormat \
  -Dpackage=${group/-/_} \
  -DgroupId=com.ms3-inc.camel \
  -DspecificationUri=openapi.yaml \
  -DartifactId=${ARTIFACT_ID} \
  -Dversion=${VERSION} \
  -DinteractiveMode=false \
  -DoutputDirectory=..
else
  mvn archetype:generate \
  -DarchetypeGroupId=com.ms3-inc.tavros \
  -DarchetypeArtifactId=camel-openapi-archetype \
  -DarchetypeVersion=0.2.6 \
  -DgroupId=com.ms3-inc.camel \
  -DspecificationUri=openapi.yaml \
  -DartifactId=${ARTIFACT_ID} \
  -Dversion=${VERSION} \
  -DinteractiveMode=false \
  -DoutputDirectory=..
fi

# add if statement for package in path format based on group id
