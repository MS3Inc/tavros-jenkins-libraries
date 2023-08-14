#!/usr/bin/env bash
set -o nounset
set -o errexit

echo "Making change to ${RELEASE_PATH}" \
&& sed -i "s/last-commit:.*/last-commit: '${GIT_COMMIT}'/g" ${RELEASE_PATH} \
&& sed -i "s/tag:.*/tag: '${VERSION}'/g" ${RELEASE_PATH} \
&& sed -i "s/<name>/${NAME}/g" ${RELEASE_PATH} \
&& sed -i "s/<namespace>/${NAMESPACE}/g" ${RELEASE_PATH} \
&& sed -i "s/<fqdn>/${TAVROS_HOST}/g" ${RELEASE_PATH} \
&& echo "Output of release.yaml" \
&& cat ${RELEASE_PATH} \
&& echo "Changing git origin" \
&& git config --global --add safe.directory "$WORKSPACE/tavros-platform" \
&& git remote remove origin \
&& git remote add origin "https://${GIT_CREDS_USR}:${GIT_CREDS_PSW}@${GIT_HOST}/tavros/platform.git" \
&& echo "Adding git configuration..." \
&& git config --global user.email "${BUILD_USER_EMAIL}" \
&& git config --global user.name "${BUILD_USER}" \
&& git add . \
&& GIT_STATUS=$(git status) \
&& git commit -m "Update ${RELEASE_PATH} with ${GIT_COMMIT}" \
&& git push -u origin main