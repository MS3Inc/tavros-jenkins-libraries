# Tavros Jenkins Library

This is a Jenkins library to be used with Jenkins in Tavros.

Jenkins in Tavros will utilize a specific version of this repo, so major changes will need to be tagged.

## Entry points

Five global variables are exposed from `vars/`. Two are **build pipelines** that run on every commit
to a project, two are **quickstarts** run once by hand to create a project, and one builds shared
Java libraries.

| Entry point | Kind | Purpose |
| --- | --- | --- |
| `openapi()` | build | Validates an OpenAPI document on every commit to a spec repository |
| `openapi_quickstart()` | quickstart | Creates a spec repository seeded with a sample document and a Jenkinsfile |
| `camelwebservice()` | build | Builds, tests, scans, signs and publishes a Camel integration, then updates its Helm release |
| `camelwebservice_quickstart()` | quickstart | Creates a Camel project from the Tavros archetype against an existing spec repository |
| `javadependency()` | build | Builds a shared Java library and publishes it to Nexus |

## `camelwebservice()` stages

| Stage | Container | Notes |
| --- | --- | --- |
| Test/Build | maven | `maven-verify.sh`; results published to Jenkins |
| Static Analysis | maven | SonarQube. **Skipped** unless `TAVROS_SONARQUBE_SERVER` is set |
| Dependency Vulnerability Scan | trivy | Reports HIGH/CRITICAL; archived. Does not gate |
| Build Image | buildah | Rootless build from the project Dockerfile; exports a tarball |
| Image Vulnerability Scan | trivy | Scans the tarball **before** the image is pushed |
| Generate SBOM | syft | CycloneDX + SPDX, archived as build artifacts |
| Push Image | buildah | Publishes to the Tavros registry; records the digest |
| Sign Image | cosign | Signs by digest. **Skipped** unless `TAVROS_COSIGN_KEY_CREDS` is set |
| Update Helm Release | git | Commits the HelmRelease change to the platform repo for Flux |

Deployment happens the way it always has: the pipeline commits a HelmRelease change to
`tavros/platform` and Flux reconciles it. Nothing is pushed directly to a cluster. The image stages
publish to the registry only, and `Update Helm Release` runs last so the release is never pointed at
an image that failed to build, scan or sign.

The two scan stages **report rather than gate**. Gating needs an agreed severity threshold and a
suppression process; without those the first unfixable transitive CVE blocks every integration
project on the platform. That is a policy decision.

### Image builder

Image builds use **buildah**, replacing the kaniko executor. The kaniko project was archived in June
2025 and receives no security updates, which is untenable for a platform heading into accreditation.
buildah is daemonless and requires no privileged container; it reads the same
`tavros-artifacts-registry` secret kaniko used, via `REGISTRY_AUTH_FILE`.

## Configuration

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

## Automated tests

`test/*.bats` covers the helm-release shell scripts, run by `.github/workflows/test.yaml`. Bats and
its helpers come from git submodules, so clone with `--recurse-submodules` or run
`git submodule update --init` before running `bats test` locally.

# Acceptance Tests

Until tests can be automated, these are the manual acceptance tests/expectations of what the pipelines should accomplish.

## Quickstart − OpenAPI Project
```
Given a run of the quickstart
When the parameters are passed and name of repo is unique
Then
    Spec repo is created using given name
    Spec repo contains default Pet Store spec and Jenkinsfile with openapi() call
```

## Quickstart − Camel Web Service Project

```
Given a run of the quickstart
When the parameters are passed and name of repo is unique
Then
    API repo is created
    Archetype is run using the api spec (name passed in via parameters)
    Generated project is committed to the repo     
```

## Camel Web Service
```
Given a repo that uses camelwebservice() pipeline
When commit is made to main
Then
    Repo code is pulled
    Code is packaged
        If repo has a dependency in Tavros' Nexus, then that dependency is retrieved
    Image is built
    Image is pushed to registry.$FQDN/$NAME:$VERSION `internal` nexus repo
    Platform repo is pulled
    If helm release or folder for api doesn't exist
        Helm release is created in dev/apis/$NAME-release.yaml
    Helm release is updated with last commit hash as annotation
    Changes to platform repo are committed and pushed
    Flux sees change and creates/updates pod
    
Given two pipelines that are running camelwebservice() at the same time
When updates are rejected in pipeline B because pipeline A finished first
Then pipeline B doesn't fail

Given a repo that uses javadependency() pipeline
When commit is made to main
Then java dependency is built and pushed to `maven-releases` or `maven-snapshots` Nexus repo
```