plugins {
    id("org.danilopianini.gradle-pre-commit-git-hooks") version "1.1.7"
    id("org.gradle.toolchains.foojay-resolver-convention") version "0.4.0"
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
include(":jakta-examples")
include(":jakta-dsl")
