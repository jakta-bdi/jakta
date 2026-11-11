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
    }

    tasks.withType(Cpd::class).configureEach {
        reports {
            text.required.set(true)
        }
    }
}
