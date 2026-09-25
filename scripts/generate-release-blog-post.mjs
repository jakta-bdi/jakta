import { mkdirSync, writeFileSync, readFileSync } from 'node:fs'
import { fileURLToPath } from 'node:url'
import path from 'node:path'

const version = process.env.NEXT_RELEASE_VERSION

if (!version) {
  throw new Error('NEXT_RELEASE_VERSION must be set')
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

const blogDir = path.join(rootDir, 'website', 'blog')
mkdirSync(blogDir, { recursive: true })

const date = new Date().toISOString().slice(0, 10)
const filePath = path.join(blogDir, `${date}-release-${version}.md`)

const content = `---
slug: release-${version}
title: JaKtA ${version} released
authors: [samubura]
tags: [jakta, release]
---

JaKtA ${version} is out.

<!-- truncate -->

${notes}
`

writeFileSync(filePath, content)
console.log(`Generated release blog post at ${filePath}`)
