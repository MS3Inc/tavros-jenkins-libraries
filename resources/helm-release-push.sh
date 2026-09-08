#!/usr/bin/env bash
set -o nounset
set -o errexit

printf "\nPulling main to get latest...\n" \
&& git pull origin main \
&& git push -u origin main