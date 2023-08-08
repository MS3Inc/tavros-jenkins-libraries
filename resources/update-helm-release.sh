#!/usr/bin/env bash
set -o nounset
set -o errexit

cd "test/${NAME}" \
  && sed -i "s/tag:.*/tag: '${VERSION}'/g" release.yaml \
  && cat release.yaml \
  && cd ../.. \
  && echo "Changing git origin" \
  && git config --global --add safe.directory "$WORKSPACE/tavros-platform" \
  && git remote remove origin \
  && git remote add origin "https://${GIT_CREDS_USR}:${GIT_CREDS_PSW}@${GIT_HOST}/tavros/platform.git" \
  && echo "Adding git configuration..." \
  && git config --global user.email "${BUILD_USER_EMAIL}" \
  && git config --global user.name "${BUILD_USER}" \
  && git add . \
  && GIT_STATUS=$(git status) \
  && if [[ $GIT_STATUS == *"nothing to commit"* ]]; then
      printf "\nNo changes to helm release version, no need to commit.";
  else
      git commit -m "Bump test/${NAME}/release.yaml to ${VERSION}" \
      && git push -u origin main
  fi