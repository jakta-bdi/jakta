apply(plugin = rootProject.libs.plugins.kotlin.multiplatform.id)

configureKotlinMultiplatform(includeNative = false)

kotlinMultiplatform {
    sourceSets {
        commonMain.dependencies {
            api(jakta("api"))
            api(jakta("dsl"))
            implementation(jakta("core"))
            implementation(libs.kotlinx.coroutines.core)
            implementation(libs.kermit)

            api(libs.tesserakt.rdf)
            api(libs.tesserakt.sparql)
            api(libs.tesserakt.serialization.turtle)
        }
        commonTest.dependencies {
            implementation(kotlin("test"))
            implementation(libs.kotlinx.coroutines.test)
        }
    }
}
