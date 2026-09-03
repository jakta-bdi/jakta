import de.aaschmid.gradle.plugins.cpd.Cpd
import dev.detekt.gradle.Detekt
import dev.detekt.gradle.report.ReportMergeTask
import dev.detekt.gradle.plugin.DetektPlugin
import org.danilopianini.gradle.gitsemver.UpdateType
import org.jlleitschuh.gradle.ktlint.tasks.GenerateReportsTask
import org.jlleitschuh.gradle.ktlint.tasks.KtLintFormatTask
import org.jlleitschuh.gradle.ktlint.tasks.KtLintCheckTask

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

val reportMerge = tasks.register("reportMerge", ReportMergeTask::class.java) {
    description = "Merges all Detekt reports into a single report"
    output = project.layout.buildDirectory.file("reports/merge.sarif")
}

fun Project.isExampleModule(): Boolean =
    path == ":examples" || path.startsWith(":examples:")


allprojects {

    group = "it.unibo.${rootProject.name}"

    repositories {
        mavenCentral()
    }

    with(rootProject.libs.plugins) {
        pluginManager.apply(dokka.id)
        pluginManager.apply(gitSemVer.id)
        pluginManager.apply(kover.id)
        pluginManager.apply(kotlin.qa.id)
        pluginManager.apply(taskTree.id)
        pluginManager.apply(publishOnCentral.id)
    }

    gitSemVer {
        excludeLightweightTags()
        commitNameBasedUpdateStrategy {
            UpdateType.NONE
        }
    }

    signing {
        if (System.getenv("CI") == "true") {
            val signingKey: String? = project.findProperty("signingKey")?.toString()
            val signingPassword: String? = project.findProperty("signingPassword")?.toString()
            useInMemoryPgpKeys(signingKey, signingPassword)
        }
    }

    publishOnCentral {
        repoOwner = "JaKtA"
        projectLongName = "jakta"
        projectDescription = "BDI agents in Kotlin"
        licenseName = "Apache License 2.0"
        licenseUrl = "https://opensource.org/license/Apache-2.0/"
        publishing {
            publications {
                withType<MavenPublication>().configureEach {
                    pom {
                        developers {
                            developer {
                                name = "Martina Baiardi"
                                email = "m.baiardi@unibo.it"
                                url = "https://github.com/anitvam"
                            }
                            developer {
                                name = "Samuele Burattini"
                                email = "samuele.burattini@unibo.it"
                                url = "https://github.com/samubura"
                            }
                            developer {
                                name = "Danilo Pianini"
                                email = "danilo.pianini@unibo.it"
                                url = "https://danilopianini.org"
                            }
                            developer {
                                name = "Giovanni Ciatto"
                                email = "giovanni.ciatto@unibo.it"
                                url = "https://github.com/gciatto"
                            }

                        }
                    }
                }
            }
        }
    }


    plugins.withType<DetektPlugin> {
        val detektTasks = tasks.withType<Detekt>()
            .matching { task ->
                task.name.let { it.endsWith("Main") || it.endsWith("Test") || it.startsWith("Test") } &&
                    !task.name.contains("Baseline")
            }
        val check = tasks.getByName("check")
        val detektAll = tasks.register("detektAll", Detekt::class.java) {
            description = "Runs all detekt tasks"
            group = "verification"
            dependsOn(detektTasks)
        }
        check.dependsOn(detektAll)
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
        input.from(tasks.withType<Detekt>().map { it.reports.checkstyle.outputLocation })
        input.from(tasks.withType<GenerateReportsTask>().flatMap { it.reportsOutputDirectory.asFileTree.files })
    }

    tasks.withType<Cpd> {
        reports {
            text.required.set(true)
            xml.required.set(true)
        }
    }

}


subprojects {
    project.version = rootProject.version

    // Exclude example projects from publishing
    if (isExampleModule()) {
        tasks.matching {
            it.name.startsWith("publish") || it.name.startsWith("upload")
        }.configureEach {
            enabled = false
        }
    }
}

dependencies {
    listOf(
        "jakta-api",
        "jakta-dsl",
        "jakta-core",
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



