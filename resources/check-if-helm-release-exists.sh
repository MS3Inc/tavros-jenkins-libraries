#!/usr/bin/env bash
set -o nounset
set -o errexit

CURRENT_API=$(echo ${GIT_URL%".git"} | grep -Eo '[^/]+$')
FILE="test/$CURRENT_API/release.yaml"

echo ${GIT_URL} \
  && cd tavros-platform \
  && echo "Working directory: $PWD" \
  && if [ -f "$FILE" ]; then
         echo "$FILE exists." && exit 0
     else
         echo "$FILE does not exist." && exit 1
     fi