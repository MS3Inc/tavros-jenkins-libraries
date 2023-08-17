#!/usr/bin/env groovy
import com.ms3_inc.tavros.jenkins.Utilities

def call() {
    def utils = new Utilities(this)
    pipeline {
        agent {
            kubernetes {
                yaml '''
                    apiVersion: v1
                    kind: Pod
                    spec:
                      containers:
                      - name: builder
                        image: atlassian/default-image:4.20230726
                        command:
                        - sleep
                        args:
                        - infinity
                      - name: maven
                        image: maven:3.6.3-jdk-11
                        securityContext:
                          runAsUser: 1000
                        command:
                        - sleep
                        args:
                        - infinity
                '''
                defaultContainer 'builder'
            }
        }
        parameters {
            string(
                name: 'ORG',
                defaultValue: 'tavros',
                description: 'Required. The organization to create the repository in.'
            )
            string(
                name: 'API_REPO_NAME',
                description: 'Required. The name of the spec repository to pull from.'
            )
            string(
                name: 'TAG',
                description: 'Optional. The tag from the spec repo. If empty, defaults to pulling from main branch.'
            )
            string(
                name: 'REPO_NAME',
                description: 'Required. A name of the new API implementation repo.'
            )
            string(
                name: 'REPO_DESC',
                description: 'Optional. A description of the new repository.'
            )
            booleanParam(
                name: 'IS_PRIVATE',
                description: 'Whether the repository is to private or not.',
                defaultValue: 'false',
            )
            string(
                name: 'GROUP_ID',
                description: 'Required. The group ID of the new project.'
            )
            string(
                name: 'VERSION',
                defaultValue: '0.1.0-SNAPSHOT',
                description: 'Required. The version of the project.'
            )
        }
        environment {
            GIT_CREDS = credentials("${TAVROS_GIT_CREDS}")
            GIT_HOST = "${TAVROS_GIT_HOST}"
        }
        stages {
            stage('Checkout Spec Repo') {
                steps  {
                    container('git') {
                        dir("openapi") {
                            checkout([
                                    $class: 'GitSCM',
                                    branches: [[name: "${TAG}" ? "refs/tags/${TAG}" : "*/main"]],
                                    userRemoteConfigs: [[
                                                                credentialsId: "${TAVROS_GIT_CREDS}",
                                                                url: "https://${GIT_HOST}/${ORG}/${API_REPO_NAME}.git"
                                                        ]]
                            ])
                        }
                    }
                }
            }
            stage('Create Camel Web Service Repository') {
                steps {
                    script {
                        utils.createRepo("${TAVROS_GIT_PROVIDER}")
                    }
                }
            }
            stage('Setup Project') {
                environment {
                    ARCHETYPE_VERSION = "0.2.7"
                }
                steps {
                    container('maven') {
                        script {
                            dir("repo") {
                                utils.shResource "maven-archetype-generate.sh"
                            }
                        }
                    }
                    dir("repo/${REPO_NAME}") {
                        script {
                            utils.writeResource "camelwebservice.jenkinsfile", "Jenkinsfile"
                        }
                    }
                }
            }
            stage('Push New Project') {
                environment {
                    COMMIT_MSG = "[tavros-quickstart] Initial commit"
                }
                steps {
                    wrap([$class: 'BuildUser']) {
                        dir("repo/${REPO_NAME}") {
                            sh 'git config --global --add safe.directory "$WORKSPACE/repo/${REPO_NAME}"'
                            script {
                                utils.shResource "git-init-push.sh"
                            }
                        }
                    }
                }
            }
        }
    }
}
