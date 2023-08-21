setup() {
    load 'test_helper/bats-support/load'
    load 'test_helper/bats-assert/load'
    DIR="$( cd "$( dirname "$BATS_TEST_FILENAME" )" >/dev/null 2>&1 && pwd )"
    CURRENT_PATH="$DIR/resources"
}

@test "when editing helm release then new values are correct" {

    helmrelease=`cat $CURRENT_PATH/helmrelease.yaml`
    expected_helmrelease=`cat $CURRENT_PATH/expected-results/expected-helmrelease.yaml`
    [ "$helmrelease" == "$expected_helmrelease" ]
}