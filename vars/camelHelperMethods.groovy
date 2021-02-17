#!/usr/bin/env groovy

def gitPushAll(String commitMessage, String org, String repoName) {
    sh '''git config --global user.email "${BUILD_USER_EMAIL}" \
        && git config --global user.name "${BUILD_USER}" \
        && git init \
        && git checkout -b main \
        && git add . \
        && git commit -m "''' + commitMessage + '''" \
        && git remote add origin https://$USERNAME:$PASSWORD@$SOURCE_CONTROL_URL_WITHOUT_PROTOCOL:''' + org + '''/''' + repoName + '''.git \
        && git push -u origin main
    '''
}

def createGiteaRepo(String org, String repoName, String repoDesc, Boolean ifPrivate) {
    sh '''
        curl $SOURCE_CONTROL_URL/api/v1/orgs/''' + org + '''/repos -i --fail \
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
