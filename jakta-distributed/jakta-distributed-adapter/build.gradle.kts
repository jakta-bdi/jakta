plugins {
    application
    alias(libs.plugins.kotlin.jvm)
}

repositories {
    mavenCentral()
}

dependencies {

    implementation(project(mapOf("path" to ":jakta-bdi")))
    implementation(project(mapOf("path" to ":jakta-distributed:jakta-distributed-client")))
}
