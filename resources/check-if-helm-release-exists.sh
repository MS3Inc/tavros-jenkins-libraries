#!/usr/bin/env bash
set -o nounset
set -o errexit

CURRENT_API=$(echo ${GIT_URL%".git"} | grep -Eo '[^/]+$')
FILE="test/$CURRENT_API/release.yaml"

echo ${GIT_URL} \
  && cd tavros-platform \
  && echo "Working directory: $PWD" \
  && if [ -f "$FILE" ]; then
         echo "$FILE exists."
     else
         echo "$FILE does not exist."
     fi