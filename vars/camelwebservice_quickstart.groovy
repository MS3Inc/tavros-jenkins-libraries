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
                        image: fedora
                        command:
                        - sleep
                        args:
                        - infinity
                      - name: maven
                        image: maven:3.6.3-jdk-11
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
                    description: 'Required. The organization to create the repository in.'
            )
            string(
                    name: 'API_REPO_NAME',
                    description: 'Required. The name of the repository to create.'
            )
            string(
                    name: 'TAG',
                    description: 'Optional. The tag from the spec repo. If empty, defaults to pulling from main branch.'
            )
            string(
                    name: 'REPO_NAME',
                    description: 'Required. A name of the new repo.'
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
                    name: 'ARTIFACT_ID',
                    description: 'Required. The artifact ID (i.e. name) of the new project.'
            )
            string(
                    name: 'VERSION',
                    defaultValue: '0.0.1',
                    description: 'Required. The version of the project.'
            )
        }
        environment {
            FOLDER_FOR_SPEC = "generated-project"
            GIT_CREDS = credentials("${TAVROS_GIT_CREDS}")
            GIT_HOST = "${TAVROS_GIT_HOST}"
        }
        stages {
            stage('Checkout Spec Repo') {
                steps  {
                    script {
                        def branchName = "${TAG}" ? "refs/tags/${TAG}" : "*/main"
                        dir("spec-files") {
                            checkout([
                                    $class: 'GitSCM',
                                    branches: [[name: branchName]],
                                    userRemoteConfigs: [[
                                            credentialsId: "${TAVROS_GIT_CREDS}",
                                            url: "https://${GIT_HOST}/${ORG}/${API_REPO_NAME}.git"
                                    ]]
                            ])
                        }
                    }
                }
            }
            stage('Generate Project with OpenAPI Archetype') {
                steps {
                    container('maven') {
                        script {
                            dir("spec-files") {
                                utils.shResource "maven-archetype-generate.sh"
                            }
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
                steps {
                    container('maven') {
                        script {
                            utils.writeResource "camelwebservice.jenkinsfile", "Jenkinsfile"
                            sh 'mv Jenkinsfile ${ARTIFACT_ID}/Jenkinsfile'
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
                        dir("${ARTIFACT_ID}") {
                            script {
                                sh 'yum install -y -q git'
                                utils.shResource "git-init-push.sh"
                            }
                        }
                    }
                }
            }
        }
    }
}
