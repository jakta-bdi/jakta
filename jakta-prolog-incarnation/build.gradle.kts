import org.gradle.api.tasks.testing.logging.TestExceptionFormat
import org.gradle.api.tasks.testing.logging.TestLogEvent
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    `java-library`
}

apply(plugin = rootProject.libs.plugins.kotlin.jvm.id)

val targetJvm = JvmTarget.JVM_17

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(targetJvm.target.toInt()))
    }
}

kotlinJvm {
    sourceSets {
        val main by getting {
            dependencies {
                api(jakta("api"))
                api(jakta("dsl"))
                implementation(jakta("core"))
                implementation(libs.kotlinx.coroutines.core)
                implementation(libs.kermit)

                api(libs.tuprolog.core)
                api(libs.tuprolog.solve.classic)
                api(libs.tuprolog.parser.core)
                api(libs.tuprolog.dsl.core)
                api(libs.tuprolog.dsl.unify)
                api(libs.tuprolog.dsl.theory)
                api(libs.tuprolog.serialize.core)
            }
        }
        val test by getting {
            dependencies {
                implementation(kotlin("test"))
                implementation(libs.kotlinx.coroutines.test)
            }
        }
    }
    compilerOptions {
        jvmTarget.set(targetJvm)
        freeCompilerArgs.add("-Xcontext-parameters")
    }
    tasks.withType<Test> {
        useJUnitPlatform()
        testLogging {
            showExceptions = true
            events = setOf(TestLogEvent.PASSED, TestLogEvent.SKIPPED, TestLogEvent.FAILED)
            exceptionFormat = TestExceptionFormat.FULL
        }
    }
}
dependencies {
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.10.2")
}

tasks.withType<KotlinCompile>().configureEach {
    compilerOptions {
        allWarningsAsErrors.set(false)
    }
}
