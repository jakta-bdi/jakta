@Suppress("DSL_SCOPE_VIOLATION")
plugins {
    `java-gradle-plugin`
    alias(libs.plugins.dokka)
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.multiJvmTesting)
    alias(libs.plugins.taskTree)
    id("jacoco-report-aggregation")
}

multiJvm {
    jvmVersionForCompilation.set(11)
}

repositories {
    mavenCentral()
}

dependencies {
    implementation(libs.kotlin.stdlib)
    testImplementation(libs.bundles.kotlin.testing)
    api(project(":jakta-dsl"))
}

kotlin {
    target {
        compilations.all {
            kotlinOptions {
                allWarningsAsErrors = true
                freeCompilerArgs = listOf("-opt-in=kotlin.RequiresOptIn", "-Xcontext-receivers")
            }
        }
    }
}

tasks {
    test {
        useJUnitPlatform()
        testLogging {
            showStandardStreams = true
            showCauses = true
            showStackTraces = true
            events(*org.gradle.api.tasks.testing.logging.TestLogEvent.values())
            exceptionFormat = org.gradle.api.tasks.testing.logging.TestExceptionFormat.FULL
        }
    }
    withType<AbstractPublishToMaven>().configureEach {
        enabled = false
    }
}
