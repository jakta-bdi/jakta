import { mkdirSync, writeFileSync, readFileSync } from 'node:fs'
import { fileURLToPath } from 'node:url'
import path from 'node:path'

const version = process.env.NEXT_RELEASE_VERSION

if (!version) {
  throw new Error('NEXT_RELEASE_VERSION must be set')
}

// Only feature (minor) and breaking (major) releases get a post; patch releases are listed in
// CHANGELOG.md and on GitHub Releases. An unset type (e.g. a manual run) still generates the post.
const releaseType = process.env.NEXT_RELEASE_TYPE
if (releaseType && !['major', 'minor', 'premajor', 'preminor'].includes(releaseType)) {
  console.log(`Skipping release post for ${releaseType} release ${version}`)
  process.exit(0)
}

const rootDir = path.join(path.dirname(fileURLToPath(import.meta.url)), '..')

// Read notes from CHANGELOG.md instead of threading them through a shell
// command: @semantic-release/changelog's "prepare" step runs before this
// script (see release.config.js plugin order) and has already written this
// version's section, and markdown release notes can contain characters
// (quotes, backticks) that aren't safe to pass through a templated shell command.
const changelog = readFileSync(path.join(rootDir, 'CHANGELOG.md'), 'utf8')
const sectionStart = changelog.indexOf(`## [${version}]`)
if (sectionStart === -1) {
  throw new Error(`Could not find a CHANGELOG.md section for version ${version}`)
}
const notesStart = changelog.indexOf('\n', sectionStart) + 1
const notesEnd = changelog.indexOf('\n## [', notesStart)
const notes = changelog.slice(notesStart, notesEnd === -1 ? undefined : notesEnd).trim()

// Release notes are a separate blog instance on the website (/releases), see website/docusaurus.config.ts.
const releasesDir = path.join(rootDir, 'website', 'releases')
mkdirSync(releasesDir, { recursive: true })

const date = new Date().toISOString().slice(0, 10)
const filePath = path.join(releasesDir, `${date}-release-${version}.md`)

// A full timestamp keeps several releases published on the same day in the right order.
const content = `---
slug: ${version}
title: JaKtA ${version}
date: ${new Date().toISOString()}
---

${notes}
`

writeFileSync(filePath, content)
console.log(`Generated release blog post at ${filePath}`)
