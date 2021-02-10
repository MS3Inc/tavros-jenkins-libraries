#!/usr/bin/env groovy

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