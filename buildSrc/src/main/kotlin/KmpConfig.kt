/*
 * Copyright (c) 2025, Danilo Pianini, Nicolas Farabegoli, Elisa Tronetti,
 * and all authors listed in the `build.gradle.kts` and the generated `pom.xml` file.
 *
 * This file is part of Collektive, and is distributed under the terms of the Apache License 2.0,
 * as described in the LICENSE file in this project's repository's top directory.
 */

import org.gradle.api.Project
import org.gradle.api.artifacts.ProjectDependency
import org.gradle.api.artifacts.dsl.DependencyHandler
import org.gradle.api.provider.Provider
import org.gradle.api.tasks.testing.logging.TestExceptionFormat
import org.gradle.api.tasks.testing.logging.TestLogEvent
import org.gradle.kotlin.dsl.assign
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
import org.jetbrains.kotlin.gradle.tasks.KotlinTest

val Provider<PluginDependency>.id: String get() = get().pluginId

inline fun <reified ProjectType : KotlinProjectExtension> Project.kotlin(configuration: ProjectType.() -> Unit) =
    extensions.getByType<ProjectType>().configuration()

fun Project.kotlinJvm(configuration: KotlinJvmProjectExtension.() -> Unit) = kotlin(configuration)

fun Project.kotlinMultiplatform(configuration: KotlinMultiplatformExtension.() -> Unit) = kotlin(configuration)

fun KotlinDependencyHandler.jakta(project: String): ProjectDependency = project(":jakta-$project")

fun DependencyHandler.jakta(project: String) = project(":jakta-$project")

@OptIn(ExperimentalKotlinGradlePluginApi::class)
fun Project.configureKotlinMultiplatform() {
    with(extensions.getByType<KotlinMultiplatformExtension>()) {
        jvm {
            compilerOptions {
                jvmTarget = JvmTarget.JVM_1_8
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
        js(IR) {
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
        val nativeSetup: KotlinNativeTarget.() -> Unit = {
            binaries {
                sharedLib()
                staticLib()
            }
        }
        applyDefaultHierarchyTemplate()
        linuxX64(nativeSetup)
        linuxArm64(nativeSetup)

        mingwX64(nativeSetup)

        macosX64(nativeSetup)
        macosArm64(nativeSetup)
        iosArm64(nativeSetup)
        iosX64(nativeSetup)
        iosSimulatorArm64(nativeSetup)
        watchosArm64(nativeSetup)
        watchosX64(nativeSetup)
        watchosSimulatorArm64(nativeSetup)
        tvosArm64(nativeSetup)
        tvosX64(nativeSetup)
        tvosSimulatorArm64(nativeSetup)

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
}
