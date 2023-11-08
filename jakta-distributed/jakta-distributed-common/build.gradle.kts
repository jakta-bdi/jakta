plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.serialization)
}

repositories {
    mavenCentral()
}

dependencies {
    implementation(project(mapOf("path" to ":jakta-bdi")))
    implementation(libs.ktor.serialization.kotlinx.json)
}
