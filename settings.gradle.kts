plugins {
    id("org.danilopianini.gradle-pre-commit-git-hooks") version "1.0.19"
}

gitHooks {
    preCommit {
        tasks("ktlintCheck")
    }
    commitMsg { conventionalCommits() }
    createHooks()
}

rootProject.name = "ise-2022-project-baiardi"

include(":core")
