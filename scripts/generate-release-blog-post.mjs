import { mkdirSync, writeFileSync } from 'node:fs'
import { fileURLToPath } from 'node:url'
import path from 'node:path'

const version = process.env.NEXT_RELEASE_VERSION
const notes = process.env.NEXT_RELEASE_NOTES

if (!version || !notes) {
  throw new Error('NEXT_RELEASE_VERSION and NEXT_RELEASE_NOTES must be set')
}

const blogDir = path.join(
  path.dirname(fileURLToPath(import.meta.url)),
  '..',
  'website',
  'blog',
)
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
