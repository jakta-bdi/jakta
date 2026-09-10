var publishCmd = `
./gradlew publishAllPublicationsToProjectLocalRepository zipMavenCentralPortalPublication releaseMavenCentralPortalPublication publish --stacktrace || exit 3
`
var prepareCmd = `
node scripts/generate-release-blog-post.mjs
(cd website && node scripts/generate-publication-blog-posts.mjs) || true
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
    [
        "@semantic-release/git",
        {
            "assets": [
                "CHANGELOG.md",
                "package.json",
                "package-lock.json",
                "website/blog/*.md",
            ]
        }
    ],
)

module.exports = config
