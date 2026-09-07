var publishCmd = `
./gradlew publishAllPublicationsToProjectLocalRepository zipMavenCentralPortalPublication releaseMavenCentralPortalPublication --stacktrace || exit 3
`
var prepareCmd = `
./gradlew dokkaGenerateHtml || true
`

var config = require('semantic-release-preconfigured-conventional-commits');
config.plugins.push(
    [
        "@semantic-release/exec",
        {
            "prepareCmd": prepareCmd,
            "publishCmd": publishCmd,
        }
    ],
    [
        "@semantic-release/github",
        {
            "assets": [
                { "path": "**/build/**/*redist*.jar" }
            ]
        }
    ],
    "@semantic-release/git",
)
module.exports = config
