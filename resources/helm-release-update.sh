#!/usr/bin/env bash
set -o nounset
set -o errexit

echo "Making change to ${RELEASE_PATH}" \
&& sed -i "s/last-commit:.*/last-commit: \"${GIT_COMMIT}\"/g;
           s/tag:.*/tag: '${VERSION}'/g;
           s/<name>/${NAME}/g;
           s/<namespace>/${NAMESPACE}/g;
           s/<fqdn>/${TAVROS_HOST}/g;
           " ${RELEASE_PATH} \
&& echo "Output of release.yaml:" \
&& cat ${RELEASE_PATH}