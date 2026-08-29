#!/usr/bin/env bash
# Publish the image to the Tavros registry. This is artifact publication, the
# image equivalent of `mvn deploy` - it does not deploy to any cluster. Flux
# decides what actually runs.
set -o nounset
set -o errexit

buildah login --username "${REG_CREDS_USR}" --password "${REG_CREDS_PSW}" "${REG_HOST}"
buildah push "${IMAGE}"

# Record the digest so signing targets an immutable reference rather than a tag.
buildah inspect --format '{{.FromImageDigest}}' "${IMAGE}" > target/security/image-digest.txt
cat target/security/image-digest.txt
