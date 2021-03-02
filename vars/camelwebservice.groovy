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
                '''
                defaultContainer 'builder'
            }
        }
        stages {
            stage('Example') {
                steps {
                    script {
                        sh 'echo This is a sample pipeline'
                    }
                }
            }
        }
    }
}
