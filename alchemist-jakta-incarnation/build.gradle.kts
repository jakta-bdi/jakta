import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.gradle.api.tasks.testing.logging.TestExceptionFormat
import org.gradle.api.tasks.testing.logging.TestLogEvent

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
                api(project(":jakta-base-dsl"))
                api(project(":jakta-api"))
                api(libs.bundles.alchemist.api)
            }
        }
        val test by getting {
            dependencies {
                implementation(kotlin("test"))
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
