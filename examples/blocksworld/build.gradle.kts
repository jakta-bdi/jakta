plugins {
    kotlin("jvm")
    id("org.jetbrains.compose") version "1.11.1"
    id("org.jetbrains.kotlin.plugin.compose") version "2.4.0"
}

repositories {
    mavenCentral()
    google()
}

dependencies {
    implementation(compose.desktop.currentOs)

    implementation(jakta("core"))
    implementation(jakta("prolog-incarnation"))

    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.kermit)
}

compose.desktop {
    application {
        mainClass = "MainKt"
    }
}
