apply(plugin = rootProject.libs.plugins.kotlin.multiplatform.id)

configureKotlinMultiplatform()

kotlinMultiplatform {
    sourceSets {
        commonMain.dependencies {
            api(jakta("api"))
        }
        commonTest.dependencies {
            implementation(kotlin("test"))
        }
    }
}
