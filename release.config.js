var publishCmd = `
./gradlew publishAllPublicationsToProjectLocalRepository zipMavenCentralPortalPublication releaseMavenCentralPortalPublication publish --stacktrace || exit 3
`
var prepareCmd = `
NEXT_RELEASE_VERSION=\${nextRelease.version} node scripts/generate-release-blog-post.mjs
(cd website && node scripts/generate-publication-blog-posts.mjs) || true
`

var config = require('semantic-release-preconfigured-conventional-commits');
// Documentation (website, Dokka) is deployed on its own: do not cut a release for it
var analyzer = config.plugins.find(p => p[0] === "@semantic-release/commit-analyzer")[1];
analyzer.releaseRules = analyzer.releaseRules.filter(rule => rule.type !== "docs");
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
