import de.aaschmid.gradle.plugins.cpd.Cpd
import org.gradle.kotlin.dsl.withType

apply(plugin = rootProject.libs.plugins.kotlin.multiplatform.id)

configureKotlinMultiplatform()

kotlinMultiplatform {
    sourceSets {
        commonMain.dependencies {
            implementation(libs.kermit)
            implementation(libs.kotlinx.coroutines.core)
        }
        commonTest.dependencies {
            implementation(kotlin("test"))
            implementation(libs.kotlinx.coroutines.test)
        }

        jvmMain.dependencies {
            implementation(libs.kotlin.reflect)
        }
    }

    tasks.withType(Cpd::class).configureEach {
        exclude("**PlanImpl**")
        // TODO decide what to do with this, the PlanBuilder is problematic as there is duplication
//    minimumTokenCount = 100
        reports {
            text.required.set(true)
        }
    }
}
