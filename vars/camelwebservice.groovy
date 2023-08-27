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
                      - name: kaniko
                        image: gcr.io/kaniko-project/executor:v1.13.0-debug
                        command:
                        - sleep
                        args:
                        - 9999999
                        volumeMounts:
                        - name: kaniko-secret
                          mountPath: /kaniko/.docker
                      volumes:
                      - name: kaniko-secret
                        secret:
                            secretName: tavros-artifacts-registry
                            items:
                            - key: .dockerconfigjson
                              path: config.json
                '''
                defaultContainer 'maven'
            }
        }
        environment {
            VERSION = """${sh(
                    returnStdout: true,
                    script: 'mvn help:evaluate -Dexpression=project.version -q -DforceStdout'
            )}"""
            NAME = """${sh(
                    returnStdout: true,
                    script: 'mvn help:evaluate -Dexpression=project.artifactId -q -DforceStdout'
            )}"""
            REG_CREDS = credentials("${TAVROS_REG_CREDS}")
        }
        stages {
            stage('Test/Build') {
                steps {
                    script {
                        utils.shResource "maven-verify.sh"
                    }
                }
            }
            stage('Push with Kaniko') {
                steps {
                    container('kaniko') {
                        sh '''
                        echo "Running kaniko cmd"
                        /kaniko/executor -f `pwd`/Dockerfile -c `pwd` --destination="${TAVROS_REG_HOST}/${NAME}:${VERSION}"
                        '''
                    }
                }
            }
            stage('Update Helm Release') {
                environment {
                    GIT_CREDS = credentials("${TAVROS_GIT_CREDS}")
                    GIT_HOST = "${TAVROS_GIT_HOST}"
                    NAMESPACE = "dev"
                    RELEASE_PATH = "${NAMESPACE}/apis/${NAME}-release.yaml"
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

                            script {
                                if (env.BUILD_USER_EMAIL == null) {
                                    env.BUILD_USER_EMAIL = ""
                                    env.BUILD_USER = "Jenkins"
                                }

                                try {
                                    utils.shResource "check-if-helm-release-exists.sh"
                                } catch (err) {
                                    echo "Helm release doesn't exist. Creating file: ${RELEASE_PATH}"
                                    utils.writeResource "release.yaml", "${RELEASE_PATH}"
                                }

                                utils.shResource "helm-release-update.sh"
                                utils.shResource "helm-release-git-setup.sh"
                                utils.shResource "helm-release-commit.sh"
                                timeout(2) {
                                    waitUntil {
                                        try {
                                            utils.shResource "helm-release-push.sh"
                                            return true
                                        } catch (error) {
                                            return false
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
