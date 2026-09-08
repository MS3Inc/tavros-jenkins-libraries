# Tavros Jenkins Library

This is a Jenkins library to be used with Jenkins in Tavros.

Jenkins in Tavros will utilize a specific version of this repo, so major changes will need to be tagged.

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