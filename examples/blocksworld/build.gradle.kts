plugins {
    kotlin("jvm")
    application
}

repositories {
    mavenCentral()
}

dependencies {
    implementation(jakta("core"))
    implementation(jakta("prolog-incarnation"))
    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.kermit)
}

application {
    mainClass.set("MainKt")
}

kotlinJvm {
    compilerOptions {
        freeCompilerArgs.add("-Xcontext-parameters")
    }
}
