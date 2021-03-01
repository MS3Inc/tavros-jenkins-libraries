#!/usr/bin/env bash

set -o nounset
set -o errexit

git config --global user.email "${BUILD_USER_EMAIL}" \
  && git config --global user.name "${BUILD_USER}" \
  && git init \
  && git checkout -b main \
  && git add . \
  && git commit -m "${COMMIT_MSG}" \
  && git remote add origin "https://${GIT_CREDS_USR}:${GIT_CREDS_PSW}@${GIT_HOST}/${ORG}/${REPO_NAME}.git" \
  && git push -u origin main
