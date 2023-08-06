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
                        env:
                        - name: DOCKER_HOST 
                          value: tcp://localhost:2376
                        command: ["/bin/sh", "-c"]
                        args:
                        - tail -f /dev/null
                      - name: dind-daemon 
                        image: docker:24.0.2-dind-alpine3.18
                        resources: 
                        requests: 
                          cpu: 20m 
                          memory: 512Mi 
                        securityContext: 
                          privileged: true 
                        volumeMounts: 
                        - name: graph-storage 
                          mountPath: /var/lib/docker  
                      volumes: 
                      - name: graph-storage 
                        emptyDir: {}
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
            stage('Test/Build/Push Camel API') {
                environment {
                    NEXUS_CREDS = credentials("${TAVROS_REG_CREDS}")
                    FQDN = "${TAVROS_FQDN}"
                }
                steps {
                    script {
                        sh 'echo "Running deploy script"'
                        utils.shResource "maven-deploy.sh"
                        sh 'echo "After running deploy script"'
                    }
                }
            }
        }
    }
}
