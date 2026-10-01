plugins {
    kotlin("jvm")
    application
}

repositories {
    mavenCentral()
}

dependencies {
    implementation(project(":alchemist-jakta-incarnation"))
    implementation(project(":jakta-situated"))
    implementation(jakta("core"))
    implementation(jakta("dsl"))

    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.kermit)
    runtimeOnly(libs.alchemist.swingui)
}

application {
    mainClass.set("it.unibo.alchemist.Alchemist")
}

// Headless: ./gradlew :examples:alchemist-gossip:run
tasks.named<JavaExec>("run") {
    args("run", "gossip.yml", "--verbosity", "error")
}

tasks.register<JavaExec>("runGui") {
    group = ApplicationPlugin.APPLICATION_GROUP
    description = "Runs the gossip simulation in the Alchemist Swing GUI."
    classpath = sourceSets.main.get().runtimeClasspath
    mainClass.set(application.mainClass)
    // Alchemist overrides are YAML strings, not files
    args("run", "gossip.yml", "--override", file("gui.yml").readText(), "--verbosity", "error")
}

tasks.register<JavaExec>("runInMemory") {
    group = ApplicationPlugin.APPLICATION_GROUP
    description = "Runs the same gossip agents without Alchemist, with the default runner."
    classpath = sourceSets.main.get().runtimeClasspath
    mainClass.set("InMemoryGossipKt")
}
