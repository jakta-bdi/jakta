plugins {
    kotlin("jvm")
    application
}

repositories {
    mavenCentral()
}

dependencies {
    implementation(jakta("core"))
    implementation(jakta("dsl"))
    implementation(jakta("api"))

    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.kermit)
}

application {
    mainClass.set("MainKt")
}
