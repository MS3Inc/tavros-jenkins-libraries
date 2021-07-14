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
                        image: maven:3.8.1-jdk-11-slim
                        securityContext:
                          runAsUser: 1000
                        env:
                        - name: DOCKER_HOST 
                          value: tcp://localhost:2375 
                        command: ["/bin/sh", "-c"]
                        args:
                        - tail -f /dev/null
                      - name: dind-daemon 
                        image: docker:18.06-dind
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
            stage('Test/Build/Push Camel API') {
                environment {
                    REG_CREDS = credentials("${TAVROS_REG_CREDS}")
                    REG_HOST = "${TAVROS_REG_HOST}"
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
