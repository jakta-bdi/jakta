plugins {
    kotlin("multiplatform")
    alias(libs.plugins.compose.multiplatform)
    alias(libs.plugins.kotlin.compose)
}

repositories {
    mavenCentral()
    google()
}

// Compose Multiplatform has no Linux/Windows native targets, and tuProlog no wasm: JVM (desktop) and JS (browser)
kotlinMultiplatform {
    jvm()
    js {
        browser()
        nodejs()
        binaries.library()
    }

    sourceSets {
        commonMain.dependencies {
            api(jakta("api"))
            implementation(jakta("core"))
            implementation(libs.kotlinx.coroutines.core)
            api(libs.bundles.compose)
        }
        commonTest.dependencies {
            implementation(jakta("dsl"))
            implementation(kotlin("test"))
            implementation(libs.kotlinx.coroutines.test)
        }
    }
}
