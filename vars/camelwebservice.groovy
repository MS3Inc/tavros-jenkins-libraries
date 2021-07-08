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
                      - name: docker
                        image: docker:dind
                        securityContext:
                          privileged: true
                        volumeMounts: 
                        - name: graph-storage 
                          mountPath: /var/lib/docker  
                        command: ["tail", "-f", "/dev/null"]
                      volumes: 
                      - name: graph-storage 
                        emptyDir: {}
                '''
                defaultContainer 'builder'
            }
        }
        stages {
            stage('Test/Package Camel API') {
                steps {
                    container('maven') {
                        script {
                            utils.shResource "test-api.sh"
                        }
                    }
                }
            }
            stage('Build/Push Camel API') {
                environment {
                    REG_CREDS = credentials("${TAVROS_REG_CREDS}")
                    REG_HOST = "${TAVROS_REG_HOST}"
                }
                steps {
                    container('docker') {
                        script {
                            utils.shResource "build-api.sh"
                        }
                    }
                }
            }
        }
    }
}
