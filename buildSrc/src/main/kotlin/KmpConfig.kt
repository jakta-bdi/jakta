/*
 * Copyright (c) 2025, Danilo Pianini, Nicolas Farabegoli, Elisa Tronetti,
 * and all authors listed in the `build.gradle.kts` and the generated `pom.xml` file.
 *
 * This file is part of Collektive, and is distributed under the terms of the Apache License 2.0,
 * as described in the LICENSE file in this project's repository's top directory.
 */

import org.gradle.api.Project
import org.gradle.api.artifacts.ProjectDependency
import org.gradle.api.artifacts.VersionCatalogsExtension
import org.gradle.api.artifacts.dsl.DependencyHandler
import org.gradle.api.provider.Provider
import org.gradle.api.tasks.testing.logging.TestExceptionFormat
import org.gradle.api.tasks.testing.logging.TestLogEvent
import org.gradle.kotlin.dsl.assign
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.getByType
import org.gradle.kotlin.dsl.project
import org.gradle.plugin.use.PluginDependency
import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.dsl.KotlinJvmProjectExtension
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension
import org.jetbrains.kotlin.gradle.dsl.KotlinProjectExtension
import org.jetbrains.kotlin.gradle.plugin.KotlinDependencyHandler
import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget
import kotlin.time.Duration.Companion.minutes
import org.gradle.kotlin.dsl.withType
import org.jetbrains.kotlin.gradle.targets.js.dsl.KotlinJsTargetDsl
import org.jetbrains.kotlin.gradle.tasks.KotlinTest
import dev.petuska.npm.publish.extension.NpmPublishExtension

val Provider<PluginDependency>.id: String get() = get().pluginId

inline fun <reified ProjectType : KotlinProjectExtension> Project.kotlin(configuration: ProjectType.() -> Unit) =
    extensions.getByType<ProjectType>().configuration()

fun Project.kotlinJvm(configuration: KotlinJvmProjectExtension.() -> Unit) = kotlin(configuration)

fun Project.kotlinMultiplatform(configuration: KotlinMultiplatformExtension.() -> Unit) = kotlin(configuration)

fun KotlinDependencyHandler.jakta(project: String): ProjectDependency = project(":jakta-$project")

fun DependencyHandler.jakta(project: String) = project(":jakta-$project")

@OptIn(ExperimentalKotlinGradlePluginApi::class)
fun Project.configureKotlinMultiplatform(includeNative: Boolean = true, targetJvm: JvmTarget = JvmTarget.JVM_1_8) {
    with(extensions.getByType<KotlinMultiplatformExtension>()) {
        jvm {
            compilerOptions {
                jvmTarget = targetJvm
            }
            testRuns.getByName("test").executionTask.configure {
                useJUnitPlatform()
                filter {
                    isFailOnNoMatchingTests = false
                }
                testLogging {
                    showExceptions = true
                    events = setOf(
                        TestLogEvent.FAILED,
                        TestLogEvent.PASSED,
                    )
                    exceptionFormat = TestExceptionFormat.FULL
                }
            }
        }
        val mochaTimeout = 60.minutes.inWholeMilliseconds.toString()
        js {
            browser {
                testTask {
                    useMocha {
                        timeout = mochaTimeout
                    }
                }
            }
            nodejs {
                testTask {
                    useMocha {
                        timeout = mochaTimeout
                    }
                }
            }
            binaries.library()
        }
        applyDefaultHierarchyTemplate()
        if (includeNative) {
            val nativeSetup: KotlinNativeTarget.() -> Unit = {
                binaries {
                    sharedLib()
                    staticLib()
                }
            }
            linuxX64(nativeSetup)
            linuxArm64(nativeSetup)

            mingwX64(nativeSetup)

            macosArm64(nativeSetup)
            iosArm64(nativeSetup)
            iosX64(nativeSetup)

//        iosSimulatorArm64(nativeSetup)
//        watchosArm64(nativeSetup)
//        watchosSimulatorArm64(nativeSetup)
//        tvosArm64(nativeSetup)
//        tvosSimulatorArm64(nativeSetup)
        }

        targets.all {
            compilations.all {
                compileTaskProvider.configure {
                    compilerOptions {
                        allWarningsAsErrors = false
                        freeCompilerArgs.add("-Xexpect-actual-classes")
                    }
                }
            }
        }

    }
    configureNpmPublishing()
}

private fun Project.configureNpmPublishing() {
    val libs = extensions.getByType<VersionCatalogsExtension>().named("libs")
    pluginManager.apply(libs.findPlugin("npm-publish").get().id)
    extensions.configure<NpmPublishExtension> {
        organization.set("jakta")
        readme.set(rootProject.layout.projectDirectory.file("README.md"))
        // No authToken: publishing relies on Trusted Publishing (GitHub Actions OIDC).
        // IMPORTANT: this workflow only runs as a workflow_call from dispatcher.yml, and
        // GitHub's OIDC token identifies the *caller* workflow -- so each package's Trusted
        // Publisher on npmjs.com must be configured with workflow filename
        // ".github/workflows/dispatcher.yml", NOT "build-and-deploy.yml", or the OIDC identity
        // check silently fails and npm falls back to demanding a token (ENEEDAUTH).
        registries {
            npmjs { }
        }
        packages.all {
            files.from(rootProject.layout.projectDirectory.file("LICENSE"))
            packageJson {
                license.set("Apache-2.0")
            }
        }
    }
}
