#!/usr/bin/env bash
set -o nounset
set -o errexit

  sed -i "s/last-commit:.*/last-commit: '123'/g" "test/${NAME}/release.yaml" \
  && cat "test/${NAME}/release.yaml" \
  && echo "Changing git origin" \
  && git config --global --add safe.directory "$WORKSPACE/tavros-platform" \
  && git remote remove origin \
  && git remote add origin "https://${GIT_CREDS_USR}:${GIT_CREDS_PSW}@${GIT_HOST}/tavros/platform.git" \
  && echo "Adding git configuration..." \
  && git config --global user.email "${BUILD_USER_EMAIL}" \
  && git config --global user.name "${BUILD_USER}" \
  && git add . \
  && GIT_STATUS=$(git status) \
  && git commit -m "Bump test/${NAME}/release.yaml to ${VERSION}" \
  && git push -u origin main