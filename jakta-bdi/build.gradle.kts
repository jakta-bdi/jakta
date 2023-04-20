dependencies {
    implementation(libs.kotlin.stdlib)
    testImplementation(libs.bundles.kotlin.testing)
    implementation(project(":utils"))
    api(project(":jakta-state-machine"))
    api(libs.tuprolog.core)
    api(libs.tuprolog.theory)
    api(libs.tuprolog.solve.classic)
    api(libs.tuprolog.solve.classic)
    api(libs.tuprolog.parser.core)
}
