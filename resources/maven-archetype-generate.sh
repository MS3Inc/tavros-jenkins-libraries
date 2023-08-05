#!/usr/bin/env bash

set -o nounset
set -o errexit

mvn -V --no-transfer-progress --batch-mode archetype:generate \
  -DarchetypeGroupId=com.ms3-inc.tavros \
  -DarchetypeArtifactId=camel-openapi-archetype \
  -DarchetypeVersion="${ARCHETYPE_VERSION}" \
  $(if [[ ${GROUP_ID} == *"-"* ]]; then echo -DpackageInPathFormat; fi) \
  $(if [[ ${GROUP_ID} == *"-"* ]]; then echo -Dpackage=${GROUP_ID/-/_}; fi) \
  -DgroupId=${GROUP_ID} \
  -DspecificationUri="../openapi/${API_REPO_NAME}/openapi.yaml" \
  -DartifactId=${REPO_NAME} \
  -Dversion=${VERSION} \
  -DinteractiveMode=false
