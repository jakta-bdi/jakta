plugins {
    kotlin("multiplatform")
    alias(libs.plugins.compose.multiplatform)
    alias(libs.plugins.kotlin.compose)
}

repositories {
    mavenCentral()
    google()
}

kotlinMultiplatform {
    jvm("desktop")
    js {
        browser()
        binaries.executable()
    }

    sourceSets {
        commonMain.dependencies {
            implementation(jakta("core"))
            implementation(jakta("dsl"))
            implementation(jakta("inspector"))

            implementation(libs.kotlinx.coroutines.core)
            implementation(libs.kermit)
        }
        named("desktopMain").dependencies {
            implementation(compose.desktop.currentOs)
        }
    }
}

compose.desktop {
    application {
        mainClass = "MainKt"
    }
}
