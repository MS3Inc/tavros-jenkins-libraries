#!/usr/bin/env bash

set -o nounset
set -o errexit

# Change this to be the actual archetype

mvn archetype:generate \
-DarchetypeGroupId=org.apache.maven.archetypes \
-DarchetypeArtifactId=maven-archetype-plugin \
-DarchetypeVersion=1.4 \
-DgroupId=${GROUP_ID} \
-DartifactId=${ARTIFACT_ID} \
-Dversion=${VERSION} \
-DinteractiveMode=false \
-DoutputDirectory=..