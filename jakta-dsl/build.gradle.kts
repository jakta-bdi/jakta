dependencies {
    implementation(libs.kotlin.stdlib)
    testImplementation(libs.bundles.kotlin.testing)
    implementation(project(":utils"))
    api(project(":jakta-bdi"))
    api(libs.tuprolog.dsl.theory)
    api(libs.tuprolog.dsl.core)
}
