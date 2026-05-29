apply(plugin = rootProject.libs.plugins.kotlin.multiplatform.id)

configureKotlinMultiplatform()

kotlinMultiplatform {
    sourceSets {
        commonMain.dependencies {
            api(jakta("api"))
            api(jakta("dsl"))
            implementation(jakta("core"))
            implementation(libs.kermit)
            implementation(libs.kotlinx.coroutines.core)
        }
        commonTest.dependencies {
            implementation(kotlin("test"))
            implementation(libs.kotlinx.coroutines.test)
        }
        all {
            languageSettings.enableLanguageFeature("ContextParameters")
        }
    }
}
