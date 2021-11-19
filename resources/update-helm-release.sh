#!/usr/bin/env bash
set -o nounset
set -o errexit

CURRENT_API=$(echo ${GIT_URL%".git"} | grep -Eo '[^/]+$')

echo ${GIT_URL} \
  && cd tavros-platform/${ENV}/$CURRENT_API \
  && sed -i "s/tag:.*/tag: '${PROJECT_VERSION}'/g" release.yaml \
  && cat release.yaml \
  && cd ../.. \
  && git remote remove origin \
  && git remote add origin "https://${GIT_CREDS_USR}:${GIT_CREDS_PSW}@${GIT_HOST}/tavros/platform.git" \
  && git config --global user.email "${BUILD_USER_EMAIL}" \
  && git config --global user.name "${BUILD_USER}" \
  && git add . \
  && GIT_STATUS=$(git status) \
  && if [[ $GIT_STATUS == *"nothing to commit"* ]]; then
      printf "\nNo changes to helm release version, no need to commit.";
  else
      git commit -m "Bump ${ENV}/$CURRENT_API/release.yaml to ${PROJECT_VERSION}" \
      && git push -u origin main
  fi