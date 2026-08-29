#!/usr/bin/env bash
# Publish the Maven artifact to Nexus.
#
# No `clean` and no test re-run: the Build & Unit Test stage already produced and
# verified these artifacts. Cleaning here would discard them and rebuild
# something that was never tested.
set -o nounset
set -o errexit

mvn -V --no-transfer-progress --batch-mode deploy -DskipTests
