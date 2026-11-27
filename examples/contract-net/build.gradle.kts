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
            implementation("org.jetbrains.compose.components:components-resources:${libs.versions.compose.get()}")
            implementation(libs.kermit)

            implementation(jakta("core"))
            implementation(jakta("prolog-incarnation"))

            implementation(libs.kotlinx.coroutines.core)
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

// the code Compose generates to load the bundled emoji font is not ours to lint
tasks.withType<org.jlleitschuh.gradle.ktlint.tasks.BaseKtLintCheckTask>().configureEach {
    exclude { "generated" in it.file.path }
}
tasks.withType<dev.detekt.gradle.Detekt>().configureEach { exclude { "generated" in it.file.path } }
