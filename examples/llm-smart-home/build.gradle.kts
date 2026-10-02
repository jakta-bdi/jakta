plugins {
    kotlin("jvm")
    application
}

repositories {
    mavenCentral()
}

dependencies {
    implementation(jakta("core"))
    implementation(jakta("llm-incarnation"))

    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.kermit)
    // The HTTP client the Koog LLM clients find at runtime on the JVM
    runtimeOnly(libs.koog.http.client.ktor)

    testImplementation(kotlin("test"))
}

application {
    mainClass.set("MainKt")
}

tasks.test {
    useJUnitPlatform()
    // The test calls a real LLM when a provider is configured, so it must never be considered up to date
    outputs.upToDateWhen { false }
}
