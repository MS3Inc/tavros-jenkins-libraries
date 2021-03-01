#!/usr/bin/env bash

# See: https://github.com/swagger-api/validator-badge/tree/v2.0.5

set -o nounset
set -o errexit

RESULT=$(curl -sS -X "POST" "http://localhost:8080/validator/debug" \
  --header "Content-Type: application/yaml" --data-binary @openapi.yaml)

if [[ $RESULT != "{}" ]]; then
    echo -e "\nInvalid OpenAPI Document:\n$RESULT"
    exit 1
fi;