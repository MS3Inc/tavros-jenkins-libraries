#!/usr/bin/env bash
# Build the container image from the Dockerfile the archetype ships.
# Rootless and daemonless - no privileged sidecar involved.
#
# The image is also exported to a docker-archive tarball in the workspace.
# Scanning and SBOM generation run in their own containers, which cannot see
# buildah's local image store, and the shared workspace is the only thing all
# three containers have in common. Exporting also means the image is scanned
# BEFORE it is pushed, so a failing image never reaches the registry.
set -o nounset
set -o errexit

mkdir -p target/security

buildah bud \
  --format docker \
  --layers \
  --tag "${IMAGE}" \
  --file Dockerfile \
  .

buildah push "${IMAGE}" "docker-archive:target/security/image.tar:${IMAGE}"

buildah images
ls -lh target/security/image.tar
