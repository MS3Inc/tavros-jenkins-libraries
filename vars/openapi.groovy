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
                      - name: swagger-validator
                        image: swaggerapi/swagger-validator-v2
                '''
                defaultContainer 'builder'
            }
        }
        stages {
            stage('Validate OpenAPI Document') {
                steps {
                    script {
                        utils.shResource "validate-openapi.sh"
                    }
                }
            }
        }
    }

}
