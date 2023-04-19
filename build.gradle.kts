group = "it.unibo.jakta"

@Suppress("DSL_SCOPE_VIOLATION")
plugins {
    alias(libs.plugins.gitSemVer)
    alias(libs.plugins.publishOnCentral)
}

val scmDeveloperConnection = "git:git@github.com:jakta-bdi/jakta"
val repoUrl = "https://github.com/jakta-bdi/jakta"
val Provider<PluginDependency>.id: String get() = get().pluginId

allprojects {
    group = rootProject.group

    with(rootProject.libs.plugins) {
        apply(plugin = publishOnCentral.id)
        apply(plugin = gitSemVer.id)
    }
    repositories {
        mavenCentral()
    }

    gitSemVer {
        versionPrefix.set("v")
        excludeLightweightTags()
        assignGitSemanticVersion()
    }

    signing {
        val signingKey: String? by project
        val signingPassword: String? by project
        useInMemoryPgpKeys(signingKey, signingPassword)
    }

    publishOnCentral {
        projectLongName.set("JaKtA")
        projectDescription.set("A Kotlin internal DSL for the definition of BDI agents")
        projectUrl.set(repoUrl)
        scmConnection.set(scmDeveloperConnection)
        publishing {
            publications {
                withType<MavenPublication>().configureEach {
                    pom {
                        scm {
                            connection.set(scmDeveloperConnection)
                            developerConnection.set(scmDeveloperConnection)
                            url.set(repoUrl)
                        }
                        developers {
                            developer {
                                name.set("Martina Baiardi")
                                email.set("m.baiardi@unibo.it")
                            }
                        }
                    }
                }
            }
        }
    }

}

