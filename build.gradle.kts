group = "it.unibo.jakta"

@Suppress("DSL_SCOPE_VIOLATION")
plugins {
    alias(libs.plugins.gitSemVer)
    alias(libs.plugins.publishOnCentral)
}

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
        projectUrl.set("https://github.com/jakta-bdi/jakta")
        scmConnection.set("git:git@github.com:jakta-bdi/jakta")
        publishing {
            publications {
                withType<MavenPublication>().configureEach {
                    pom {
                        scm {
                            connection.set("git:git@github.com:jakta-bdi/jakta")
                            developerConnection.set("git:git@github.com:jakta-bdi/jakta")
                            url.set("https://github.com/jakta-bdi/jakta")
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

