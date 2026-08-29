#!/usr/bin/env groovy
import com.ms3_inc.tavros.jenkins.Utilities

/**
 * Build pipeline for a Camel web service generated from the Tavros archetypes.
 *
 * Produces a tested, scanned, signed container image plus an SBOM, and publishes
 * the Maven artifact to Nexus.
 *
 * It deliberately does NOT deploy to a cluster. Flux reconciles the platform from
 * git; a push from Jenkins would put changes into the cluster that never passed
 * through the repository, which is exactly the auditability the GitOps model was
 * chosen to provide.
 *
 * Optional configuration, read from Jenkins global environment:
 *   TAVROS_SONARQUBE_SERVER  name of the SonarQube server in Jenkins config.
 *                            If unset, the analysis stage is skipped rather than
 *                            failing - no platform role provisions SonarQube yet
 *                            (ADR-0015 specifies it as intended, not present).
 *   TAVROS_COSIGN_KEY_CREDS  credentials id of the cosign private key.
 *                            If unset, signing is skipped with a warning.
 */
def call(Map args = [:]) {
    def utils = new Utilities(this)
    pipeline {
        agent {
            kubernetes {
                yaml '''
                    apiVersion: v1
                    kind: Pod
                    spec:
                      # No pod-wide securityContext. runAsUser is set per
                      # container, following the original pipeline: the scanner
                      # and signing images run as root by default, and forcing a
                      # UID on them stops them starting. What matters for the
                      # accreditation posture is that no container is privileged,
                      # and none is.
                      containers:
                      - name: maven
                        image: maven:3.9.16-eclipse-temurin-21
                        securityContext:
                          runAsUser: 1000
                        command: ["/bin/sh", "-c"]
                        args: ["tail -f /dev/null"]
                        resources:
                          requests:
                            cpu: 500m
                            memory: 1Gi
                      # Rootless, daemonless image builds. Replaces the previous
                      # docker:18.06-dind sidecar, which required privileged: true.
                      # STORAGE_DRIVER=vfs avoids needing /dev/fuse or extra
                      # capabilities; it is slower than overlay but needs no
                      # elevated privileges, which is the point.
                      - name: buildah
                        image: quay.io/buildah/stable:v1.43.2
                        command: ["/bin/sh", "-c"]
                        args: ["tail -f /dev/null"]
                        env:
                        - name: STORAGE_DRIVER
                          value: vfs
                        - name: BUILDAH_ISOLATION
                          value: chroot
                        resources:
                          requests:
                            cpu: 500m
                            memory: 1Gi
                      # Scanner and signing tooling. Caches are pointed at the
                      # shared workspace rather than each image's default under
                      # /root, so these keep working whatever UID the pod ends up
                      # running them as.
                      - name: trivy
                        image: aquasec/trivy:0.74.0
                        command: ["/bin/sh", "-c"]
                        args: ["tail -f /dev/null"]
                        env:
                        - name: TRIVY_CACHE_DIR
                          value: /home/jenkins/agent/.cache/trivy
                      - name: syft
                        image: anchore/syft:v1.51.1
                        command: ["/bin/sh", "-c"]
                        args: ["tail -f /dev/null"]
                        env:
                        - name: SYFT_CACHE_DIR
                          value: /home/jenkins/agent/.cache/syft
                      - name: cosign
                        image: ghcr.io/sigstore/cosign:v3.1.3
                        command: ["/bin/sh", "-c"]
                        args: ["tail -f /dev/null"]
                '''
                defaultContainer 'maven'
            }
        }

        options {
            timestamps()
            buildDiscarder(logRotator(numToKeepStr: '30'))
        }

        environment {
            REG_CREDS = credentials("${TAVROS_REG_CREDS}")
            REG_HOST  = "${TAVROS_REG_HOST}"
            IMAGE     = "${TAVROS_REG_HOST}/${JOB_BASE_NAME}:${GIT_COMMIT ?: BUILD_NUMBER}"
        }

        stages {
            stage('Build & Unit Test') {
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

            stage('Publish Artifact') {
                steps {
                    script {
                        utils.shResource "maven-deploy.sh"
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
        }
    }
}
