#!/usr/bin/env bash
# Publish the image to the Tavros registry, using the same registry secret
# kaniko used (mounted read-only and read via REGISTRY_AUTH_FILE).
#
# This is artifact publication, not deployment. Nothing is deployed to a cluster
# here - the Update Helm Release stage commits a HelmRelease change to git and
# Flux reconciles it.
set -o nounset
set -o errexit

buildah push "${IMAGE}"

# Record the digest so signing targets an immutable reference rather than a tag.
buildah inspect --format '{{.FromImageDigest}}' "${IMAGE}" > target/security/image-digest.txt
cat target/security/image-digest.txt
