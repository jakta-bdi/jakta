plugins {
    id("org.danilopianini.gradle-pre-commit-git-hooks") version "1.1.1"
}

gitHooks {
    preCommit {
        tasks("ktlintCheck")
    }
    commitMsg { conventionalCommits() }
    createHooks()
}

rootProject.name = "ise-2022-project-baiardi"

include(":utils")
include(":agent-fsm")
include(":agent-bdi")
include("examples")
include("agent-dsl")
