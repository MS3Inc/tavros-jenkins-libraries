# Tavros Jenkins Library

A Jenkins [shared library](https://www.jenkins.io/doc/book/pipeline/shared-libraries/) defining how
Tavros integration projects are scaffolded and built.

Jenkins in Tavros consumes a specific **tag** of this repository, so changes must be tagged to take
effect. See [Consuming a new version](#consuming-a-new-version).

## Entry points

Four global variables are exposed from `vars/`. Two are **build pipelines** that run on every commit
to a project; two are **quickstarts** that run once, by hand, to create a project.

| Entry point | Kind | Purpose |
| --- | --- | --- |
| `openapi()` | build | Validates an OpenAPI document on every commit to a spec repository |
| `openapi_quickstart()` | quickstart | Creates a new spec repository, seeded with a sample document and a Jenkinsfile |
| `camelwebservice()` | build | Builds, tests, scans, signs and publishes a Camel integration project |
| `camelwebservice_quickstart()` | quickstart | Creates a new Camel project from the Tavros archetype, from an existing spec repository |

The quickstarts are parameterised Jenkins jobs a developer runs to bootstrap a repository. They
create the Gitea repo, lay down initial content, write a `Jenkinsfile` that calls the corresponding
build pipeline, and push the first commit. From then on the build pipeline runs on its own.

## `camelwebservice()` stages

| Stage | Container | Notes |
| --- | --- | --- |
| Build & Unit Test | maven | `mvn clean verify`; results published to Jenkins |
| Static Analysis | maven | SonarQube. **Skipped** unless `TAVROS_SONARQUBE_SERVER` is set |
| Dependency Vulnerability Scan | trivy | Reports HIGH/CRITICAL; archived. Does not gate |
| Publish Artifact | maven | `mvn deploy -DskipTests` to Nexus |
| Build Image | buildah | Rootless build from the archetype Dockerfile; exports a tarball |
| Image Vulnerability Scan | trivy | Scans the tarball **before** the image is pushed |
| Generate SBOM | syft | CycloneDX + SPDX, archived as build artifacts |
| Push Image | buildah | Publishes to the Tavros registry; records the digest |
| Sign Image | cosign | Signs by digest. **Skipped** unless `TAVROS_COSIGN_KEY_CREDS` is set |

**There is no deployment stage, deliberately.** Flux reconciles the platform from git. A push from
Jenkins would place changes into the cluster that never passed through the repository, which is the
auditability the GitOps model was chosen to provide. "Publish Artifact" and "Push Image" publish to
Nexus and the registry; neither deploys anything.

The two scan stages **report rather than gate**. Turning them into gates needs an agreed severity
threshold and a suppression process — without those, the first unfixable transitive CVE blocks every
integration project on the platform. That is a policy decision, not a pipeline one.

## Configuration

Global environment expected on the Jenkins controller:

| Variable | Required | Purpose |
| --- | --- | --- |
| `TAVROS_GIT_HOST` | yes | Gitea host |
| `TAVROS_GIT_CREDS` | yes | Gitea credentials id |
| `TAVROS_GIT_PROVIDER` | yes | Currently only `gitea` |
| `TAVROS_REG_HOST` | yes | Container registry host |
| `TAVROS_REG_CREDS` | yes | Registry credentials id |
| `TAVROS_SONARQUBE_SERVER` | no | SonarQube server name in Jenkins config. Unset ⇒ analysis skipped |
| `TAVROS_COSIGN_KEY_CREDS` | no | Credentials id (file) of the cosign private key. Unset ⇒ signing skipped |
| `TAVROS_COSIGN_PASSWORD_CREDS` | no | Credentials id (string) of the cosign key password |

SonarQube and cosign are optional on purpose. ADR-0015 specifies SonarQube as an intended platform
component but no Ansible role provisions it yet, so a hard dependency would break every build until
that role exists.

## Consuming a new version

Jenkins pins this library by tag. After tagging, update the Tavros `jenkins` role so the
`tavros-library` global shared library points at the new tag, then re-run the provision playbook (or
change it in **Manage Jenkins → System → Global Pipeline Libraries** for a quick check).

Existing projects pick the change up on their next build — their `Jenkinsfile` is just
`@Library("tavros-library") _` followed by the entry-point call, so the pipeline definition lives
here, not in the project.
