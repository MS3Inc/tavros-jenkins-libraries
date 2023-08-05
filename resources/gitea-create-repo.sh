#!/usr/bin/env bash

set -o nounset
set -o errexit

curl -sSL -X POST "https://${GIT_HOST}/api/v1/orgs/${ORG}/repos" -i --fail \
  -u $GIT_CREDS_USR:$GIT_CREDS_PSW \
  -H "Accept: application/json" \
  -H "Content-Type: application/json" \
  -d "{ \
    \"name\": \"${REPO_NAME}\", \
    \"description\": \"${REPO_DESC}\", \
    \"private\":  ${IS_PRIVATE}, \
    \"default_branch\": \"main\", \
    \"auto_init\": false, \
    \"template\": false, \
    \"trust_model\": \"default\"
  }"
