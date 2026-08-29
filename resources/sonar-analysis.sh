#!/usr/bin/env bash
# SonarQube analysis. Only invoked when TAVROS_SONARQUBE_SERVER is configured;
# withSonarQubeEnv supplies SONAR_HOST_URL and SONAR_AUTH_TOKEN.
set -o nounset
set -o errexit

mvn --no-transfer-progress --batch-mode sonar:sonar
