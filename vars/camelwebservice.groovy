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
                        image: fedora
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
            stage('Update Helm Release') {
                environment {
                    GIT_CREDS = credentials("${TAVROS_GIT_CREDS}")
                    GIT_HOST = "${TAVROS_GIT_HOST}"
                    PROJECT_VERSION = sh(returnStdout: true, script: "mvn org.apache.maven.plugins:maven-help-plugin:3.2.0:evaluate -Dexpression=project.version -q -DforceStdout").trim()
                    COMMIT_MSG = "Update version"
                    ENV = "${env.DEFAULT_ENV == null ? 'dev' : env.DEFAULT_ENV}"
                    MVN_SETTINGS = credentials("mvn-settings")
                }
                steps {
                    container('git') {
                        dir("tavros-platform") {
                            checkout([
                                    $class           : 'GitSCM',
                                    branches         : [[name: '*/main']],
                                    extensions       : [[$class: 'LocalBranch', localBranch: "**"]],
                                    userRemoteConfigs: [[
                                                                credentialsId: "${TAVROS_GIT_CREDS}",
                                                                url          : "https://${TAVROS_GIT_HOST}/tavros/platform.git"
                                                        ]]
                            ])
                        }

                        script {
                            if (env.BUILD_USER_EMAIL == null) {
                                env.BUILD_USER_EMAIL = ""
                                env.BUILD_USER = "Jenkins"
                            }

                            sh 'yum install -y -q git'
                            utils.shResource "update-helm-release.sh"
                        }
                    }
                }
            }
        }
    }
}