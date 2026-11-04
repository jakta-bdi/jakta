plugins {
    kotlin("jvm")
    application
}

repositories {
    mavenCentral()
}

// Alchemist needs Java 17
java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(17))
    }
}

dependencies {
    implementation(jakta("core"))
    implementation(jakta("dsl"))
    implementation(jakta("api"))
    implementation(project(":alchemist-jakta-incarnation"))

    implementation(libs.bundles.alchemist.api)
    // Alchemist's graphical viewer, at the same version as Alchemist
    implementation("it.unibo.alchemist:alchemist-swingui:${libs.versions.alchemist.get()}")
    implementation("it.unibo.alchemist:alchemist-euclidean-geometry:${libs.versions.alchemist.get()}")

    // Alchemist logs through Logback: the example sets its level
    implementation("ch.qos.logback:logback-classic:1.5.34")

    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.kermit)
}

application {
    mainClass.set("MainKt")
}
