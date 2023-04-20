dependencies {
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
