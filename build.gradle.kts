import io.gitlab.arturbosch.detekt.Detekt
import io.gitlab.arturbosch.detekt.DetektPlugin
import io.gitlab.arturbosch.detekt.report.ReportMergeTask
import org.danilopianini.gradle.gitsemver.UpdateType
import org.jlleitschuh.gradle.ktlint.tasks.GenerateReportsTask
import org.jlleitschuh.gradle.ktlint.tasks.KtLintCheckTask
import org.jlleitschuh.gradle.ktlint.tasks.KtLintFormatTask

plugins {
    alias(libs.plugins.dokka)
    alias(libs.plugins.gitSemVer)
    alias(libs.plugins.kover)
    alias(libs.plugins.kotlin.qa)
    alias(libs.plugins.npm.publish)
    alias(libs.plugins.multiJvmTesting)
    alias(libs.plugins.publishOnCentral)
    alias(libs.plugins.taskTree)
}

val reportMerge by tasks.registering(ReportMergeTask::class) {
    output = project.layout.buildDirectory.file("reports/merge.sarif")
}

subprojects {
    project.version = rootProject.version
}

allprojects {

    group = "it.unibo.${rootProject.name}"

    repositories {
        mavenCentral()
    }

    with(rootProject.libs.plugins) {
        apply(plugin = dokka.id)
        apply(plugin = gitSemVer.id)
        apply(plugin = kotlin.qa.id)
        apply(plugin = kover.id)
        apply(plugin = publishOnCentral.id)
        apply(plugin = taskTree.id)
    }

    gitSemVer {
        versionPrefix.set("v")
        excludeLightweightTags()
        commitNameBasedUpdateStrategy {
            UpdateType.NONE
        }
    }

    signing {
        if (System.getenv("CI") == "true") {
            val signingKey: String? by project
            val signingPassword: String? by project
            useInMemoryPgpKeys(signingKey, signingPassword)
        }
    }

    //TODO enable at some point?

//    publishOnCentral {
//        repoOwner = "JaKtA"
//        projectLongName = "jakta"
//        projectDescription = "BDI agents in Kotlin"
//        licenseName = "Apache License 2.0"
//        licenseUrl = "https://opensource.org/license/Apache-2.0/"
//        publishing {
//            publications {
//                withType<MavenPublication>().configureEach {
//                    pom {
//                        developers {
//                            developer {
//                                name = "Martina Baiardi"
//                                email = "m.baiardi@unibo.it"
//                                url = "https://github.com/anitvam"
//                            }
//                            developer {
//                                name = "Samuele Burattini"
//                                email = "samuele.burattini@unibo.it"
//                                url = "https://github.com/samubura"
//                            }
//                            developer {
//                                name = "Giovanni Ciatto"
//                                email = "giovanni.ciatto@unibo.it"
//                                url = "https://github.com/gciatto"
//                            }
//                            developer {
//                                name = "Danilo Pianini"
//                                email = "danilo.pianini@unibo.it"
//                                url = "https://danilopianini.org"
//                            }
//                        }
//                    }
//                }
//            }
//        }
//    }

    //TODO copy past from collektive not sure if needed
    plugins.withType<DetektPlugin> {
        val detektTasks = tasks.withType<Detekt>()
            .matching { task ->
                task.name.let { it.endsWith("Main") || it.endsWith("Test") } &&
                    !task.name.contains("Baseline")
            }
        val check by tasks.getting
        val detektAll by tasks.registering {
            group = "verification"
            check.dependsOn(this)
            dependsOn(detektTasks)
        }
    }

    // Enforce the use of the Kotlin version in all subprojects
    configurations.matching { it.name != "detekt" }.all {
        resolutionStrategy.eachDependency {
            if (requested.group == "org.jetbrains.kotlin") {
                useVersion(rootProject.libs.versions.kotlin.get())
            }
        }
    }

    tasks.withType<SourceTask>().matching { it is VerificationTask }.configureEach {
        finalizedBy(reportMerge)
    }

    tasks.withType<GenerateReportsTask>().configureEach { finalizedBy(reportMerge) }
    reportMerge {
        input.from(tasks.withType<Detekt>().map { it.sarifReportFile })
        input.from(tasks.withType<GenerateReportsTask>().flatMap { it.reportsOutputDirectory.asFileTree.files })
    }



}


dependencies {
    listOf(
        "jakta-api",
        "jakta-dsl"
    ).forEach{
        kover(project(it))
    }
}


tasks {
    // Prevent publishing the root project (since is empty)
    withType<AbstractPublishToMaven>().configureEach { enabled = false }
    withType<GenerateModuleMetadata>().configureEach { enabled = false }

    fun <T : Task> T.dependsOnIncludedBuilds() = dependsOn(gradle.includedBuilds.map { it.task(":$name") })
    fun <T : Task> TaskProvider<T>.dependsOnIncludedBuilds() = configure { dependsOnIncludedBuilds() }
    fun <T : Task> TaskCollection<T>.dependsOnIncludedBuilds() = configureEach { dependsOnIncludedBuilds() }
    withType<KtLintFormatTask>().dependsOnIncludedBuilds()
    withType<KtLintCheckTask>().dependsOnIncludedBuilds()
    withType<Detekt>().dependsOnIncludedBuilds()
    build.dependsOnIncludedBuilds()
    check.dependsOnIncludedBuilds()
}


// OLD CONFIGURATION FOR RELEASE ON MAVEN CENTRAL PLUGIN
//publishOnCentral {
//    repoOwner.set("jakta-bdi")
//    projectLongName.set("JaKtA")
//    projectDescription.set("A Kotlin internal DSL for the definition of BDI agents")
//    scmConnection.set("scm:git:https://github.com/$repoOwner/${rootProject.name}")
//    projectUrl.set("https://github.com/$repoOwner/${rootProject.name}")
//}
//
//publishing.publications.withType<MavenPublication>().configureEach {
//    pom {
//        developers {
//            developer {
//                id.set("anitvam")
//                name.set("Martina Baiardi")
//                email.set("m.baiardi@unibo.it")
//            }
//        }
//    }
//}

