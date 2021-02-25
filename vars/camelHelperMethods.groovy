#!/usr/bin/env groovy

def gitPushAll(String commitMessage, String org, String repoName) {
    sh '''git config --global user.email "${BUILD_USER_EMAIL}" \
        && git config --global user.name "${BUILD_USER}" \
        && git init \
        && git checkout -b main \
        && git add . \
        && git commit -m "''' + commitMessage + '''" \
        && git remote add origin https://$USERNAME:$PASSWORD@$TAVROS_SCM_HOST/''' + org + '''/''' + repoName + '''.git \
        && git push -u origin main
    '''
}

def createRepo(String scmProvider, String org, String repoName, String repoDesc, Boolean ifPrivate) {
    if (scmProvider == 'gitea') {
        createGiteaRepo(org, repoName, repoDesc, ifPrivate)
    }
}

def createGiteaRepo(String org, String repoName, String repoDesc, Boolean ifPrivate) {
    sh '''
        curl https://$TAVROS_SCM_HOST/api/v1/orgs/''' + org + '''/repos -i --fail \
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
