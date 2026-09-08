#!/usr/bin/env bash
# Scan the built image from the workspace tarball, before it is pushed.
# Reports rather than gates - see scan-dependencies.sh for the rationale.
set -o nounset
set -o errexit

mkdir -p target/security

trivy image --input target/security/image.tar --severity HIGH,CRITICAL \
  --format table --output target/security/image-scan.txt --exit-code 0

trivy image --input target/security/image.tar \
  --format json --output target/security/image-scan.json --exit-code 0

echo "--- image findings (HIGH/CRITICAL) ---"
cat target/security/image-scan.txt
