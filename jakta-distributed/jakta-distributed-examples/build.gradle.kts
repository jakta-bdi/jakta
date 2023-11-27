plugins {
    application
    alias(libs.plugins.kotlin.jvm)
}

repositories {
    mavenCentral()
}

dependencies {
    implementation(libs.logback.classic)
    implementation(libs.kotlinx.coroutines.core)
    implementation(project(mapOf("path" to ":jakta-bdi")))
    implementation(project(mapOf("path" to ":jakta-distributed:jakta-distributed-adapter")))
    implementation(project(mapOf("path" to ":jakta-distributed:jakta-distributed-broker")))
}

tasks.register<JavaExec>("runPonger") {
    mainClass = "it.unibo.jakta.agents.distributed.pingpong.PongerKt"
    classpath = sourceSets.main.get().runtimeClasspath
}

tasks.register<JavaExec>("runPinger") {
    mainClass = "it.unibo.jakta.agents.distributed.pingpong.PingerKt"
    classpath = sourceSets.main.get().runtimeClasspath
}
