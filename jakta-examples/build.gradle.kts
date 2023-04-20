dependencies {
    implementation(libs.kotlin.stdlib)
    testImplementation(libs.bundles.kotlin.testing)
    api(project(":jakta-dsl"))
}

tasks {
    withType<AbstractPublishToMaven>().configureEach {
        enabled = false
    }
}

tasks.cpdKotlinCheck {
    onlyIf {
        project.hasProperty("runCpd")
    }
}
