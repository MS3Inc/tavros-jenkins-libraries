# Tavros Jenkins Library

This is a Jenkins library to be used with Jenkins in Tavros.

Jenkins in Tavros will utilize a specific version of this repo, so major changes will need to be tagged.

# Acceptance Tests

Until tests can be automated, these are the manual acceptance tests/expectations of what the pipelines should accomplish.

## Quickstart − OpenAPI Project

TBD

## Quickstart − Camel Web Service Project

TBD

## Camel Web Service
```
Given a repo that uses camelwebservice() pipeline
When commit is made to main
Then
    Repo code is pulled
    Code is packaged
    Image is built
    Image is pushed to registry.$FQDN/$NAME:$VERSION and exists in internal nexus repo
    Platform repo is pulled
    If helm release or folder for api doesn't exist
        Helm release is created in dev/apis/$NAME-release.yaml
        Validations for helm release are:
            metadata.namespace == dev
            spec.targetNamespace == dev
            metadata.name == $NAME
            spec.values.fullnameOverride == $NAME
            spec.values.image.repository == registry.$FQDN/$NAME
            tag == tag from commit
            spec.values.ingress.hosts[0].host == apps.sandbox.$FQDN
            spec.values.ingress.hosts[0].paths == "/dev/$NAME"
            spec.values.podAnnotations.list-commit == current git commit of api
    Helm release is updated with last commit hash as annotation
    Changes to platform repo are committed and pushed
    Flux sees change and creates/updates pod
    
Given two pipelines that are running camelwebservice() at the same time
When updates are rejected in pipeline B because pipeline A finished first
Then pipeline B doesn't fail
```