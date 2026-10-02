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
    }

    sourceSets {
        commonMain.dependencies {
            api(libs.bundles.compose)
            api(libs.kermit)
        }
    }
}
