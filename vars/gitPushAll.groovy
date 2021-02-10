#!/usr/bin/env groovy

def gitPushAll(String commitMessage, String repoName) {
    sh '''git config --global http.sslVerify false \
        && git config --global user.email "${BUILD_USER_EMAIL}" \
        && git config --global user.name "${BUILD_USER}" \
        && git init \
        && git checkout -b main \
        && git add . \
        && git commit -m "''' + commitMessage + '''" \
        && git remote add origin https://$USERNAME:$PASSWORD@$GITEA_URL_WITHOUT_PROTOCOL/tavros/''' + repoName + '''.git \
        && git push -u origin main
    '''
}