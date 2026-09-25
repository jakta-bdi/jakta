import de.aaschmid.gradle.plugins.cpd.Cpd

apply(plugin = rootProject.libs.plugins.kotlin.multiplatform.id)

// 2p-kt looks up its solver implementations at runtime in a way that fails under ES modules.
configureKotlinMultiplatform(includeNative = false, esModules = false)

kotlinMultiplatform {
    sourceSets {
        commonMain.dependencies {
            api(jakta("api"))
            api(jakta("dsl"))
            implementation(jakta("core"))
            implementation(libs.kotlinx.coroutines.core)
            implementation(libs.kermit)

            api(libs.tuprolog.core)
            api(libs.tuprolog.solve.classic)
            api(libs.tuprolog.parser.core)
            api(libs.tuprolog.dsl.core)
            api(libs.tuprolog.dsl.unify)
            api(libs.tuprolog.dsl.theory)
            api(libs.tuprolog.serialize.core)
        }
        commonTest.dependencies {
            implementation(kotlin("test"))
            implementation(libs.kotlinx.coroutines.test)
        }
    }
}

tasks.withType<Cpd> {
    // TODO should I not exclude CPD for tests?
    exclude("Test**")
}
