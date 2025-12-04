import de.aaschmid.gradle.plugins.cpd.Cpd

apply(plugin = rootProject.libs.plugins.kotlin.multiplatform.id)

configureKotlinMultiplatform()

kotlinMultiplatform {
    sourceSets {
        commonMain.dependencies {
            implementation(jakta("api"))
            implementation(libs.kermit)
            implementation(libs.kotlinx.coroutines.core)
        }
        commonTest.dependencies {
            implementation(kotlin("test"))
            implementation(libs.kotlinx.coroutines.test)
        }
    }
}

tasks.withType(Cpd::class).configureEach {
    exclude("**PlanBuilder**")
    // TODO decide what to do with this, the PlanBuilder is problematic as there is duplication
//    minimumTokenCount = 100
    reports {
        text.required.set(true)
    }
}
