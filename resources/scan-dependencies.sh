#!/usr/bin/env bash
# Scan the resolved dependency tree for known vulnerabilities.
#
# Exit code is deliberately 0: this reports, it does not gate. Gating needs an
# agreed severity threshold and a suppression process, otherwise the first
# unfixable transitive CVE blocks every integration project on the platform.
# That is a policy decision, not a pipeline one. Findings are archived per build.
set -o nounset
set -o errexit

mkdir -p target/security

trivy filesystem --scanners vuln --severity HIGH,CRITICAL \
  --format table --output target/security/dependency-scan.txt --exit-code 0 .

trivy filesystem --scanners vuln \
  --format json --output target/security/dependency-scan.json --exit-code 0 .

echo "--- dependency findings (HIGH/CRITICAL) ---"
cat target/security/dependency-scan.txt
