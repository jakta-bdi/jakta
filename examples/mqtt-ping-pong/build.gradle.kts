plugins {
    kotlin("jvm")
    kotlin("plugin.serialization")
    application
}

repositories {
    mavenCentral()
}

java {
    // the containers run on eclipse-temurin:17-jre
    toolchain.languageVersion.set(JavaLanguageVersion.of(17))
}

dependencies {
    implementation(jakta("core"))
    implementation(jakta("prolog-incarnation"))
    implementation(jakta("distributed"))

    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.kermit)
}

application {
    mainClass.set("MainKt")
}

// the launcher builds the Docker image from the installed distribution
tasks.named("run") { dependsOn("installDist") }
