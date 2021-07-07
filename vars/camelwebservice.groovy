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
        stages {
            stage('Test/Build Camel API') {
                container('maven') {
                    steps {
                        script {
                            utils.shResource "build-api.sh"
                        }
                    }
                }
            }
        }
    }
}
