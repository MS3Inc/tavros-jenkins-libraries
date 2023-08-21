setup() {
    load 'test_helper/bats-support/load'
    load 'test_helper/bats-assert/load'
    DIR="$( cd "$( dirname "$BATS_TEST_FILENAME" )" >/dev/null 2>&1 && pwd )"
    CURRENT_PATH="$DIR/resources"
    PROJECT_RESOURCES_PATH="$CURRENT_PATH/../../resources"
}

@test "when editing helm release then new values are correct" {
    cp $PROJECT_RESOURCES_PATH/release.yaml $CURRENT_PATH/helmrelease.yaml

    RELEASE_PATH="$CURRENT_PATH/helmrelease.yaml"
    GIT_COMMIT='bcc1cx3cwd44f2269da9ab008be7831196cce7b9'
    NAME="api-repo"
    VERSION="0.1.5-SNAPSHOT"
    NAMESPACE="dev"
    TAVROS_HOST="tavros.com"
    export GIT_COMMIT
    export RELEASE_PATH
    export NAME
    export VERSION
    export NAMESPACE
    export TAVROS_HOST

    run "$PROJECT_RESOURCES_PATH/helm-release-update.sh"

    helmrelease=`cat $CURRENT_PATH/helmrelease.yaml`
    expected_helmrelease=`cat $CURRENT_PATH/expected-results/expected-helmrelease.yaml`
    [ "$helmrelease" == "$expected_helmrelease" ]
}