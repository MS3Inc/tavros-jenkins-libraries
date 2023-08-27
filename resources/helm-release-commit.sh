#!/usr/bin/env bash
set -o nounset
set -o errexit

printf "\nAdding release and committing...\n" \
&& git add . \
&& git commit -m "Update ${RELEASE_PATH} with ${GIT_COMMIT}"