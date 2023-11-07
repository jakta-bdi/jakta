plugins {
    id("org.danilopianini.gradle-pre-commit-git-hooks") version "1.1.15"
    id("org.gradle.toolchains.foojay-resolver-convention") version "0.7.0"
}

gitHooks {
    preCommit {
        tasks("ktlintCheck")
    }
    commitMsg { conventionalCommits() }
    createHooks()
}

rootProject.name = "jakta"
include(":jakta-state-machine")
include(":jakta-bdi")
include(":jakta-dsl")
include(":jakta-distributed:jakta-distributed-client")
include(":jakta-distributed:jakta-distributed-broker")
