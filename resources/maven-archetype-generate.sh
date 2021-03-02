#!/usr/bin/env bash

set -o nounset
set -o errexit

mvn archetype:generate \
-DarchetypeGroupId=com.ms3-inc.tavros \
-DarchetypeArtifactId=camel-openapi-archetype \
-DarchetypeVersion=0.2.6 \
-DspecificationUri=openapi.yaml
-DpackageInPathFormat \
-Dpackage=com.ms3_inc.camel \
-DgroupId=${GROUP_ID} \
-DartifactId=${ARTIFACT_ID} \
-Dversion=${VERSION} \
-DinteractiveMode=false \
-DoutputDirectory=..

# add if statement for package in path format based on group id
