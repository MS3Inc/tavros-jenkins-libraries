#!/usr/bin/env groovy

def call(Map args = [:]) {
    pipeline {
        agent {
            kubernetes {
                containerTemplate {
                    name 'jnlp'
                    image 'jenkins/inbound-agent:3131.vf2b_b_798b_ce99-2'
                }
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
                          value: tcp://localhost:2375 
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
            stage('Test') {
                steps {
                    sh 'echo "Running pipeline"'
                }
            }
        }
    }
}
