#!/usr/bin/env bash
set -o nounset
set -o errexit

echo "Making change to ${RELEASE_PATH}" \
&& sed -i "s/last-commit:.*/last-commit: \"${GIT_COMMIT}\"/g" ${RELEASE_PATH} \
&& sed -i "s/tag:.*/tag: '${VERSION}'/g" ${RELEASE_PATH} \
&& sed -i "s/<name>/${NAME}/g" ${RELEASE_PATH} \
&& sed -i "s/<namespace>/${NAMESPACE}/g" ${RELEASE_PATH} \
&& sed -i "s/<fqdn>/${TAVROS_HOST}/g" ${RELEASE_PATH} \
&& echo "Output of release.yaml:" \
&& cat ${RELEASE_PATH}