#!/usr/bin/env bash
# Sign the pushed image with cosign, by digest.
#
# Signing a tag is close to meaningless - tags move. The digest recorded at push
# time is what gets signed, so the signature is bound to exact bytes.
set -o nounset
set -o errexit

DIGEST=$(cat target/security/image-digest.txt)
REPO="${IMAGE%%:*}"

cosign sign --yes --key "${COSIGN_KEY}" "${REPO}@${DIGEST}"
cosign verify --key "${COSIGN_KEY}" "${REPO}@${DIGEST}"
