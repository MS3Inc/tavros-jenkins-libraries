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
                        image: atlassian/default-image:5.20250519
                        command:
                        - sleep
                        args:
                        - infinity
                      - name: maven
                        image: maven:3.9.16-eclipse-temurin-21
                        securityContext:
                          runAsUser: 1000
                        command: ["/bin/sh", "-c"]
                        args:
                        - tail -f /dev/null
                      # Replaces gcr.io/kaniko-project/executor. Kaniko was
                      # ARCHIVED in June 2025 - no development, no security
                      # updates - which is disqualifying for a platform heading
                      # into accreditation. buildah is actively maintained,
                      # daemonless, and needs no privileged container.
                      # STORAGE_DRIVER=vfs avoids /dev/fuse and extra
                      # capabilities; slower than overlay, but unprivileged.
                      # Registry auth is the same secret kaniko used, mounted the
                      # same way and read via REGISTRY_AUTH_FILE.
                      - name: buildah
                        image: quay.io/buildah/stable:v1.43.2
                        command: ["/bin/sh", "-c"]
                        args:
                        - tail -f /dev/null
                        env:
                        - name: STORAGE_DRIVER
                          value: vfs
                        - name: BUILDAH_ISOLATION
                          value: chroot
                        - name: REGISTRY_AUTH_FILE
                          value: /home/build/.docker/config.json
                        volumeMounts:
                        - name: registry-auth
                          mountPath: /home/build/.docker
                      # Scan and signing tooling. Caches point at the shared
                      # workspace rather than each image's default under /root,
                      # so they work whatever UID the pod runs them as.
                      - name: trivy
                        image: aquasec/trivy:0.74.0
                        command: ["/bin/sh", "-c"]
                        args:
                        - tail -f /dev/null
                        env:
                        - name: TRIVY_CACHE_DIR
                          value: /home/jenkins/agent/.cache/trivy
                      - name: syft
                        image: anchore/syft:v1.51.1
                        command: ["/bin/sh", "-c"]
                        args:
                        - tail -f /dev/null
                        env:
                        - name: SYFT_CACHE_DIR
                          value: /home/jenkins/agent/.cache/syft
                      - name: cosign
                        image: ghcr.io/sigstore/cosign:v3.1.3
                        command: ["/bin/sh", "-c"]
                        args:
                        - tail -f /dev/null
                      volumes:
                      - name: registry-auth
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
            IMAGE = "${TAVROS_REG_HOST}/${NAME}:${VERSION}"
        }
        stages {
            stage('Test/Build') {
                steps {
                    script {
                        utils.shResource "maven-verify.sh"
                    }
                }
                post {
                    always {
                        junit allowEmptyResults: true, testResults: 'target/surefire-reports/*.xml'
                    }
                }
            }

            stage('Static Analysis') {
                when {
                    expression { return env.TAVROS_SONARQUBE_SERVER?.trim() }
                }
                steps {
                    withSonarQubeEnv("${TAVROS_SONARQUBE_SERVER}") {
                        script {
                            utils.shResource "sonar-analysis.sh"
                        }
                    }
                }
            }

            stage('Dependency Vulnerability Scan') {
                steps {
                    container('trivy') {
                        script {
                            utils.shResource "scan-dependencies.sh"
                        }
                    }
                }
                post {
                    always {
                        archiveArtifacts artifacts: 'target/security/dependency-scan.*',
                                         allowEmptyArchive: true, fingerprint: true
                    }
                }
            }
            stage('Build Image') {
                steps {
                    container('buildah') {
                        script {
                            utils.shResource "image-build.sh"
                        }
                    }
                }
            }

            stage('Image Vulnerability Scan') {
                steps {
                    container('trivy') {
                        script {
                            utils.shResource "scan-image.sh"
                        }
                    }
                }
                post {
                    always {
                        archiveArtifacts artifacts: 'target/security/image-scan.*',
                                         allowEmptyArchive: true, fingerprint: true
                    }
                }
            }

            stage('Generate SBOM') {
                steps {
                    container('syft') {
                        script {
                            utils.shResource "sbom-generate.sh"
                        }
                    }
                }
                post {
                    always {
                        archiveArtifacts artifacts: 'target/security/sbom.*',
                                         allowEmptyArchive: true, fingerprint: true
                    }
                }
            }

            stage('Push Image') {
                steps {
                    container('buildah') {
                        script {
                            utils.shResource "image-push.sh"
                        }
                    }
                }
            }

            stage('Sign Image') {
                when {
                    expression { return env.TAVROS_COSIGN_KEY_CREDS?.trim() }
                }
                steps {
                    container('cosign') {
                        withCredentials([
                            file(credentialsId: "${TAVROS_COSIGN_KEY_CREDS}", variable: 'COSIGN_KEY'),
                            string(credentialsId: "${TAVROS_COSIGN_PASSWORD_CREDS}", variable: 'COSIGN_PASSWORD')
                        ]) {
                            script {
                                utils.shResource "image-sign.sh"
                            }
                        }
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
