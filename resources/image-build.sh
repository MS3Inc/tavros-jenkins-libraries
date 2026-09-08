#!/usr/bin/env bash
# Build the container image from the project's Dockerfile, rootless and
# daemonless. Replaces the kaniko executor invocation.
#
# The image is also exported to a docker-archive tarball in the workspace. The
# scanners run in their own containers and cannot see buildah's local image
# store; the shared workspace is the only thing all three have in common.
# Exporting also means the image is scanned BEFORE it is pushed, so a failing
# image never reaches the registry.
set -o nounset
set -o errexit

mkdir -p target/security

buildah bud --format docker --layers \
  --tag "${IMAGE}" \
  --file "$(pwd)/Dockerfile" \
  "$(pwd)"

buildah push "${IMAGE}" "docker-archive:target/security/image.tar:${IMAGE}"

buildah images
ls -lh target/security/image.tar
