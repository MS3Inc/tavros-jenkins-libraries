#!/usr/bin/env bash

set -o nounset
set -o errexit

curl -sSL -X POST "https://${GIT_HOST}/api/v1/orgs" -i --fail \
  -u $GIT_CREDS_USR:$GIT_CREDS_PSW \
  -H "Accept: application/json" \
  -H "Content-Type: application/json" \
  -d "{ \
    \"full_name\": \"${ORG_NAME}\", \
    \"description\": \"${ORG_DESC}\", \
    \"location\":  \"${ORG_LOC}\", \
    \"repo_admin_change_team_access\": ${REPO_ADMIN_ACCESS_CONTROL}, \
    \"username\": \"${ORG_USER}\", \
    \"visibility\": \"${ORG_VIS}\", \
    \"website\": \"${ORG_WEBSITE}\", \
  }"
