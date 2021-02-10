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

def createRepoWithPostRequest(String repoName, String repoDesc, Boolean ifPrivate) {
    sh '''
        curl ${GITEA_URL}/api/v1/orgs/tavros/repos -i --fail -k \
        -u $USERNAME:$PASSWORD \
        -H "Accept: application/json" \
        -H "Content-Type: application/json" \
        -d '{ \
        "auto_init": false, \
        "default_branch": "main", \
        "description": "''' + repoDesc + '''", \
        "name": "''' + repoName + '''", \
        "private":  ''' + ifPrivate + ''', \
        "template": false, \
        "trust_model": "default"}'
    '''
}