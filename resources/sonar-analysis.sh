#!/usr/bin/env bash
# SonarQube analysis. Only invoked when TAVROS_SONARQUBE_SERVER is configured;
# withSonarQubeEnv supplies SONAR_HOST_URL and SONAR_AUTH_TOKEN.
#
# ADR-0015 specifies SonarQube as an intended platform component, but no Ansible
# role provisions it yet, so this must not be a hard dependency of every build.
set -o nounset
set -o errexit

if [ -f .settings.xml ]; then
    mvn -s .settings.xml --no-transfer-progress --batch-mode sonar:sonar
else
    mvn --no-transfer-progress --batch-mode sonar:sonar
fi
