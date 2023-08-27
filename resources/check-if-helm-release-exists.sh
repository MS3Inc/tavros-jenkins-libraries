#!/usr/bin/env bash
set -o nounset
set -o errexit

echo ${GIT_URL} \
  && echo "Working directory: $PWD" \
  && if [ -f "${RELEASE_PATH}" ]; then
         echo "${RELEASE_PATH} exists." && exit 0
     else
         echo "${RELEASE_PATH} does not exist." && exit 1
     fi