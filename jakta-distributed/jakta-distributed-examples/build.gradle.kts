import org.gradle.configurationcache.extensions.capitalized
import java.nio.charset.Charset

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
    implementation(project(mapOf("path" to ":jakta-dsl")))
}

tasks.register<JavaExec>("runPonger") {
    mainClass = "it.unibo.jakta.agents.distributed.pingpong.PongerKt"
    classpath = sourceSets.main.get().runtimeClasspath
}

tasks.register<JavaExec>("runPinger") {
    mainClass = "it.unibo.jakta.agents.distributed.pingpong.PingerKt"
    classpath = sourceSets.main.get().runtimeClasspath
}

fun mainFiles() = project
    .projectDir
    .listFiles { f: File -> f.name == "src" }
    ?.firstOrNull()
    ?.walk()
    ?.filter { it.isFile && it.readText(Charset.defaultCharset()).contains("fun main") }
    // ?.map { it.nameWithoutExtension }
    ?.toList()
    ?: emptyList()

fun fromPathToClasspath(file: File) = file
    .path
    .replaceBefore(fromDotToSeparator(project.group.toString()), "")
    .dropLast(3)
    .fromSeparatorToDot()

fun fromDotToSeparator(string: String) = string.replace('.', File.separatorChar)
fun String.fromSeparatorToDot() = this.replace(File.separatorChar, '.')

mainFiles().forEach {
    task<JavaExec>("jakta${it.nameWithoutExtension.capitalized()}") {
        group = "JaKtA examples"
        sourceSets.main { classpath = runtimeClasspath }
        mainClass.set(fromPathToClasspath(it))
        standardInput = System.`in`
    }
}
