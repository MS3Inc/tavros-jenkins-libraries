#!/usr/bin/env bash

set -o nounset
set -o errexit

mvn archetype:generate \
-DarchetypeGroupId=com.ms3-inc.tavros \
-DarchetypeArtifactId=camel-openapi-archetype \
-DarchetypeVersion=0.2.6 \
$(if [[ ${GROUP_ID} == *"-"* ]]; then echo -DpackageInPathFormat; fi) \
$(if [[ ${GROUP_ID} == *"-"* ]]; then echo -Dpackage=${GROUP_ID/-/_}; fi) \
-DgroupId=${GROUP_ID} \
-DspecificationUri=openapi.yaml \
-DartifactId=${ARTIFACT_ID} \
-Dversion=${VERSION} \
-DinteractiveMode=false \
-DoutputDirectory=..
