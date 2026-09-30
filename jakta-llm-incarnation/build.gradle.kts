import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlin.serialization)
}

apply(plugin = rootProject.libs.plugins.kotlin.multiplatform.id)

// Koog publishes no Linux/Windows/macOS native targets, and its JVM artifacts are built for Java 17.
configureKotlinMultiplatform(includeNative = false, targetJvm = JvmTarget.JVM_17)

kotlinMultiplatform {
    sourceSets {
        commonMain.dependencies {
            api(jakta("api"))
            api(jakta("dsl"))
            implementation(jakta("core"))
            implementation(libs.kotlinx.coroutines.core)
            implementation(libs.kermit)

            api(libs.koog.prompt.executor.model)
        }
        commonTest.dependencies {
            implementation(kotlin("test"))
            implementation(libs.kotlinx.coroutines.test)
            implementation(libs.koog.agents.test)
        }
    }
}
