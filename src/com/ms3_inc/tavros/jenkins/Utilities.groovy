package com.ms3_inc.tavros.jenkins

class Utilities {
    private final workflow

    Utilities(workflow) {
        this.workflow = workflow
    }

    def printEnv() {
        workflow.sh 'env'
    }

    def shResource(String name) {
        workflow.sh(workflow.libraryResource(name))
    }

    def writeResource(String name, String dest) {
        workflow.writeFile(file: dest, text: workflow.libraryResource(name))
    }

    def createRepo(String provider) {
        switch (provider) {
            case "gitea":
                shResource"gitea-create-repo.sh"
                break
            default:
                throw new IllegalArgumentException("Unsupported provider: $provider")
        }
    }
}
