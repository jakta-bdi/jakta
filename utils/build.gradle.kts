dependencies {
    implementation(libs.kotlin.stdlib)
    testImplementation(libs.bundles.kotlin.testing)
}

tasks {
    withType<AbstractPublishToMaven>().configureEach {
        enabled = false
    }
}
