plugins {
    kotlin("jvm")
    application
}

repositories {
    mavenCentral()
    google()
}

// settings.gradle.kts includes this module only when WEBOTS_HOME is set
val webotsLibs = "${System.getenv("WEBOTS_HOME")}/lib/controller"

dependencies {
    implementation(project(":examples:blocksworld"))
    implementation(jakta("core"))
    implementation(jakta("prolog-incarnation"))
    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.kermit)

    // the Webots Java API is not on Maven: it ships with Webots
    implementation(files("$webotsLibs/java/Controller.jar"))

    testImplementation(kotlin("test"))
}

application {
    mainClass.set("MainKt")
    applicationDefaultJvmArgs = listOf("-Djava.library.path=$webotsLibs/java", "--enable-native-access=ALL-UNNAMED")
}

// ponytail: Linux only; macOS needs DYLD_LIBRARY_PATH and $WEBOTS_HOME/Contents/lib/controller, Windows needs PATH
tasks.named<JavaExec>("run") {
    // libJavaController loads libController and libCppController from here
    environment("LD_LIBRARY_PATH", listOfNotNull(webotsLibs, System.getenv("LD_LIBRARY_PATH")).joinToString(":"))
}

tasks.test {
    useJUnitPlatform()
}
