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
                        image: atlassian/default-image:5.20250519
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
                name: 'REPO_NAME',
                description: 'Required. The name of the repository to create.'
            )
            string(
                name: 'REPO_DESC',
                defaultValue: '',
                description: 'Optional. A description of the repository.'
            )
            booleanParam(
                name: 'IS_PRIVATE',
                description: 'Whether the repository is to private or not.',
                defaultValue: 'false',
            )
        }
        environment {
            GIT_HOST = "${TAVROS_GIT_HOST}"
            GIT_CREDS = credentials("${TAVROS_GIT_CREDS}")
        }
        stages {
            stage('Create Repository') {
                steps  {
                    script {
                        utils.createRepo("${TAVROS_GIT_PROVIDER}")
                    }
                }
            }
            stage('Setup Project') {
                steps {
                    dir("repo") {
                        script {
                            // Vendored in this library rather than curled from
                            // raw.githubusercontent.com at pipeline runtime. A
                            // third-party URL is an outage and a supply-chain
                            // exposure the pipeline cannot see coming.
                            utils.writeResource "openapi-petstore-sample.yaml", "openapi.yaml"
                            utils.writeResource "openapi.jenkinsfile", "Jenkinsfile"
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
                        dir("repo") {
                            sh 'git config --global --add safe.directory "$WORKSPACE/repo"'
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
