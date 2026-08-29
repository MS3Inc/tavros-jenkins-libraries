#!/usr/bin/env bash
# Generate an SBOM from the same tarball the scanner used, so the SBOM
# describes exactly the artifact that was scanned and will be pushed.
#
# Emitted in both CycloneDX and SPDX: federal software supply-chain guidance
# (EO 14028 / NTIA minimum elements) asks for a machine-readable SBOM, and which
# format a given reviewer wants varies.
set -o nounset
set -o errexit

mkdir -p target/security

syft docker-archive:target/security/image.tar -o cyclonedx-json=target/security/sbom.cyclonedx.json
syft docker-archive:target/security/image.tar -o spdx-json=target/security/sbom.spdx.json

echo "--- SBOM component count (cyclonedx) ---"
grep -o '"bom-ref"' target/security/sbom.cyclonedx.json | wc -l
