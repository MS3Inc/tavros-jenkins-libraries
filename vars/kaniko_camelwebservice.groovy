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
                      - name: git
                        image: atlassian/default-image:4.20230726
                        command:
                        - sleep
                        args:
                        - infinity
                      - name: maven
                        image: maven:3.8.1-jdk-11-slim
                        securityContext:
                          runAsUser: 1000
                        command: ["/bin/sh", "-c"]
                        args:
                        - tail -f /dev/null
                '''
                defaultContainer 'maven'
            }
        }
        stages {
            stage('Test Stage') {
                steps {
                    script {
                        sh 'echo "Running first stage"'
                    }
                }
            }
            stage('Test/Build') {
                environment {
                    NEXUS_CREDS = credentials("${TAVROS_REG_CREDS}")
                    FQDN = "${TAVROS_FQDN}"
                }
                steps {
                    script {
                        sh 'mvn -s .settings.xml clean package'
                    }
                }
            }

        }
    }
}
