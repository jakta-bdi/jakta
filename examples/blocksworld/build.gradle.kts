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
        browser {
            testTask { useMocha { timeout = "60s" } }
        }
        binaries.executable()
    }

    sourceSets {
        commonMain.dependencies {
            implementation(libs.bundles.compose)

            implementation(jakta("core"))
            implementation(jakta("prolog-incarnation"))

            implementation(libs.kotlinx.coroutines.core)
            implementation(libs.kermit)
        }
        commonTest.dependencies {
            implementation(kotlin("test"))
            implementation(libs.kotlinx.coroutines.test)
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
