import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    `kotlin-dsl`
}

val javaVersion = project.rootDir.resolve(".java-version").readText().trim().substringBefore('.').toInt()

kotlin {
    compilerOptions {
        jvmTarget = JvmTarget.fromTarget(javaVersion.toString())
    }
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(javaVersion))
    }
}

repositories {
    gradlePluginPortal()
    mavenCentral()
}

with(extensions.getByType<VersionCatalogsExtension>().named("libs")) {
    dependencies {
        implementation(kotlin("reflect"))
        implementation(findLibrary("kotlin-gradle-plugin").get())
    }
}

//TODO this feels like a hack but it should work
configurations.all {
    resolutionStrategy.force("org.jetbrains.kotlinx:kotlinx-coroutines-core-jvm:1.11.0")
}


