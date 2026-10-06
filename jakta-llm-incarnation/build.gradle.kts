import org.gradle.api.tasks.testing.logging.TestExceptionFormat
import org.gradle.api.tasks.testing.logging.TestLogEvent
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    `java-library`
    alias(libs.plugins.kotlin.serialization)
}

apply(plugin = rootProject.libs.plugins.kotlin.jvm.id)

// JVM only: plans are selected synchronously, so matching blocks on the LLM calls, which JS cannot do.
// Koog's JVM artifacts are built for Java 17.
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

                api(libs.koog.prompt.executor.model)
            }
        }
        val test by getting {
            dependencies {
                implementation(kotlin("test"))
                implementation(libs.kotlinx.coroutines.test)
                implementation(libs.koog.agents.test)
            }
        }
    }
    compilerOptions {
        jvmTarget.set(targetJvm)
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
