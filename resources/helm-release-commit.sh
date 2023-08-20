#!/usr/bin/env bash
set -o nounset
set -o errexit

printf "\nAdding release...\n" \
&& git add . \
&& git commit -m "Update ${RELEASE_PATH} with ${GIT_COMMIT}" \
&& printf "\nPulling main to get latest...\n" \
&& git pull origin main \
&& git push -u origin main