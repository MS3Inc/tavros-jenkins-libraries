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
                name: 'ORG_NAME',
                description: 'Required. The organization to create.'
            )
            string(
                name: 'ORG_DESC',
                description: 'Optional. A description of the new org.'
            )
            string(
                name: 'ORG_LOC',
                description: 'Optional. Location of the new org.'
            )
            booleanParam(
                name: 'REPO_ADMIN_ACCESS_CONTROL',
                description: 'Required. Ability for Repo Admin to change team access',
                defaultValue: 'true'
            )
            string(
                name: 'ORG_USER',
                description: 'Required. User Who Owns Org.'
            )
            string(
                name: 'ORG_VIS',
                description: 'Required. Visiblity level of Org',
                defaultValue: 'public'
            )
            string(
                name: 'ORG_WEBSITE',
                description: 'Optional. Website of Org.'
            )
        }
        environment {
            GIT_CREDS = credentials("${TAVROS_GIT_CREDS}")
            GIT_HOST = "${TAVROS_GIT_HOST}"
        }
        stages {
            stage('Create Gitea Org') {
                steps {
                    script {
                        utils.createOrg("${TAVROS_GIT_PROVIDER}")
                    }
                }
            }
        }
    }
}
