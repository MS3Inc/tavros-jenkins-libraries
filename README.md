# Tavros Jenkins Library

This is a Jenkins library to be used with Jenkins in Tavros.

Jenkins in Tavros will utilize a specific version of this repo, so major changes will need to be tagged.

# Acceptance Tests

## Quickstart − OpenAPI Project

TBD

## Quickstart − Camel Web Service Project

TBD

## Camel Web Service
```
Given a repo that uses camelwebservice() pipeline
When commit is made to main
Then
    Platform repo code is pulled
    Code is packaged
    Image is built
    Image is pushed to registry.$FQDN/$NAME:$VERSION and exists in internal nexus repo
    If helm release doesn't exist
        Helm release is created in test/$NAME/release.yaml
    Helm release is updated with last commit hash as annotation
    Changes to platform repo are committed and pushed
    Flux sees change and updates pod
```