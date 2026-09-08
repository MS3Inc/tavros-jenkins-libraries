#!/usr/bin/env groovy
import com.ms3_inc.tavros.jenkins.Utilities

def call(Map args = [:]) {
    def utils = new Utilities(this)
    pipeline {
        agent {
            kubernetes {
                yaml '''
                    apiVersion: v1
                    kind: Pod
                    spec:
                      containers:
                      - name: maven
                        image: maven:3.9.16-eclipse-temurin-21
                        securityContext:
                          runAsUser: 1000
                        command:
                        - sleep
                        args:
                        - infinity
                '''
                defaultContainer 'maven'
            }
        }
        stages {
            stage('Test/Build/Push') {
                environment {
                    REG_CREDS = credentials("${TAVROS_REG_CREDS}")
                }
                steps {
                    script {
                        utils.shResource "maven-deploy.sh"
                    }
                }
            }
        }
    }
}