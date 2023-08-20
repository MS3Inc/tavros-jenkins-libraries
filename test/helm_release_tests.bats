setup() {
    load 'test_helper/bats-support/load'
    load 'test_helper/bats-assert/load'
    DIR="$( cd "$( dirname "$BATS_TEST_FILENAME" )" >/dev/null 2>&1 && pwd )"
    PATH="$DIR/../src:$PATH"
}

@test "when editing helm release then new values are correct" {
    helmrelease=`cat resources/helmrelease.yaml`
    expected_helmrelease=`cat resources/expected-results/expected-helmrelease.yaml`
    [ "$helmrelease" == "$expected_helmrelease" ]
}