#!/usr/bin/env bash
# Compile and run unit tests. Deliberately `verify`, not `deploy`: nothing is
# published until the later stages have had a chance to reject the build.
set -o nounset
set -o errexit

mvn -V --no-transfer-progress --batch-mode clean verify
