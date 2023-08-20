#!/usr/bin/env bash
set -o nounset
set -o errexit

printf "\nChanging git origin..." \
&& git config --global --add safe.directory "$WORKSPACE/tavros-platform" \
&& git remote remove origin \
&& git remote add origin "https://${GIT_CREDS_USR}:${GIT_CREDS_PSW}@${GIT_HOST}/tavros/platform.git" \
&& printf "\nAdding git configuration..." \
&& git config --global user.email "${BUILD_USER_EMAIL}" \
&& git config --global user.name "${BUILD_USER}" \
&& git config pull.ff only