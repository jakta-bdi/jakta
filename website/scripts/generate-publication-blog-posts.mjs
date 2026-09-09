// Generates a blog post for every publication in static/jakta-references.bib
// that doesn't already have one. The bib file is manually curated (this script
// never writes to it) -- this only fills in the announcement blogposts, using
// Semantic Scholar purely to look up an abstract for each already-known paper
// (by DOI, falling back to a title search), never to discover new papers.
//
// Dedup / "don't override manual edits": each generated post embeds an HTML
// comment `<!-- jakta-publication-bib-key: <key> -->`. A post is considered
// "already handled" once that marker exists anywhere in website/blog/*.md,
// regardless of what a human has since edited in the rest of the file.

import { readFileSync, writeFileSync, readdirSync } from 'node:fs'
import path from 'node:path'
import { fileURLToPath } from 'node:url'
import * as citationJs from '@citation-js/core'
import '@citation-js/plugin-bibtex'
import yaml from 'js-yaml'

const Cite = citationJs.Cite || citationJs.default.Cite

const scriptDir = path.dirname(fileURLToPath(import.meta.url))
const websiteDir = path.join(scriptDir, '..')
const bibPath = path.join(websiteDir, 'static', 'jakta-references.bib')
const blogDir = path.join(websiteDir, 'blog')
const authorsPath = path.join(blogDir, 'authors.yml')

const sleep = (ms) => new Promise((resolve) => setTimeout(resolve, ms))

function loadCoveredBibKeys() {
  const covered = new Set()
  for (const file of readdirSync(blogDir)) {
    if (!file.endsWith('.md')) continue
    const content = readFileSync(path.join(blogDir, file), 'utf8')
    const match = content.match(/<!--\s*jakta-publication-bib-key:\s*(\S+)\s*-->/)
    if (match) covered.add(match[1])
  }
  return covered
}

function loadAuthorNameToKey() {
  const authors = yaml.load(readFileSync(authorsPath, 'utf8'))
  const map = new Map()
  for (const [key, value] of Object.entries(authors)) {
    if (value?.name) map.set(value.name.toLowerCase(), key)
  }
  return map
}

function slugifyKey(key) {
  return key
    .toLowerCase()
    .replace(/[^a-z0-9]+/g, '-')
    .replace(/^-+|-+$/g, '')
}

function fullName(author) {
  return [author.given, author.family].filter(Boolean).join(' ')
}

// BibTeX escapes characters like `_` for LaTeX (e.g. "10.1007/foo-4\_4"). These
// fields are used verbatim in URLs and API lookups here, not compiled by LaTeX,
// so the backslash must be stripped rather than treated as part of the value.
function stripLatexEscapes(value) {
  return value?.replace(/\\([&%$#_{}~^])/g, '$1') ?? value
}

// Returns { ok: true, data } on a successful (possibly abstract-less) response,
// or { ok: false } if every retry was rate-limited/unreachable -- callers must
// not treat the latter as "confirmed no abstract exists".
async function tryFetch(url, headers) {
  const retryDelaysMs = [0, 4000, 10000, 20000]
  for (const delay of retryDelaysMs) {
    if (delay) await sleep(delay)
    try {
      const res = await fetch(url, { headers })
      if (res.status === 429) continue
      if (!res.ok) return { ok: false }
      return { ok: true, data: await res.json() }
    } catch {
      continue
    }
  }
  return { ok: false }
}

async function fetchAbstract(entry) {
  const headers = process.env.SEMANTIC_SCHOLAR_API_KEY
    ? { 'x-api-key': process.env.SEMANTIC_SCHOLAR_API_KEY }
    : {}
  const warnUnverified = () =>
    console.warn(
      `  could not reach Semantic Scholar for "${entry.title}" (rate-limited/unreachable after retries) -- abstract left out`,
    )

  const doi = stripLatexEscapes(entry.DOI)
  if (doi) {
    const result = await tryFetch(
      `https://api.semanticscholar.org/graph/v1/paper/DOI:${encodeURIComponent(doi)}?fields=abstract`,
      headers,
    )
    if (!result.ok) {
      warnUnverified()
      return null
    }
    // A successful response with no abstract is a confirmed absence (e.g. the
    // publisher didn't license the abstract to Semantic Scholar) -- not a failure.
    return result.data?.abstract ?? null
  }

  const result = await tryFetch(
    `https://api.semanticscholar.org/graph/v1/paper/search?query=${encodeURIComponent(entry.title)}&fields=title,abstract&limit=1`,
    headers,
  )
  if (!result.ok) {
    warnUnverified()
    return null
  }
  const hit = result.data?.data?.[0]
  if (hit && hit.title?.toLowerCase().trim() === entry.title?.toLowerCase().trim()) {
    return hit.abstract ?? null
  }
  return null
}

function buildPost({ entry, authorNames, blogAuthorKeys, year, pubDate, venue, url, abstract }) {
  const slug = `paper-${slugifyKey(entry.id)}`
  const frontmatterLines = [
    '---',
    `slug: ${slug}`,
    `title: ${JSON.stringify(`New Publication: ${entry.title}`)}`,
    `date: ${pubDate}`,
  ]
  if (blogAuthorKeys.length > 0) {
    frontmatterLines.push(`authors: [${blogAuthorKeys.join(', ')}]`)
  }
  frontmatterLines.push('tags: [jakta, paper]', '---')

  const detailLines = [
    `**Authors**: ${authorNames.join(', ')}`,
    venue ? `**Venue**: ${venue}${year ? ` (${year})` : ''}` : year ? `**Year**: ${year}` : null,
    url ? `**Link**: [${url}](${url})` : null,
  ].filter(Boolean)

  const body = [
    `A new paper related to JaKtA has been published${venue ? ` at ${venue}` : ''}${year ? ` (${year})` : ''}.`,
    '',
    '<!-- truncate -->',
    '',
    `<!-- jakta-publication-bib-key: ${entry.id} -->`,
    '',
    detailLines.join('\n'),
  ]

  if (abstract) {
    body.push('', '### Abstract', '', abstract)
  }

  return `${frontmatterLines.join('\n')}\n\n${body.join('\n')}\n`
}

async function main() {
  const bibText = readFileSync(bibPath, 'utf8')
  const entries = new Cite(bibText).get({ type: 'json' })
  const covered = loadCoveredBibKeys()
  const authorNameToKey = loadAuthorNameToKey()

  const missing = entries.filter((entry) => !covered.has(entry.id))
  console.log(`${entries.length} publications total, ${missing.length} missing a blog post.`)

  const today = new Date().toISOString().slice(0, 10)

  for (const entry of missing) {
    try {
      const authorNames = (entry.author ?? []).map(fullName)
      const blogAuthorKeys = authorNames
        .map((name) => authorNameToKey.get(name.toLowerCase()))
        .filter(Boolean)
      const year = entry.issued?.['date-parts']?.[0]?.[0] ?? null
      // DBLP bib entries only ever carry a year, never month/day, so the
      // publication date is pinned to Jan 1st of that year -- still far more
      // accurate than dating the post the day the script happened to run.
      const pubDate = year ? `${year}-01-01` : today
      const venue = entry['container-title'] ?? ''
      const doi = stripLatexEscapes(entry.DOI)
      // citation-js percent-encodes the raw `url` field's stray LaTeX escapes
      // (e.g. "\_" becomes the literal text "%5C_") before we see it, so a DOI
      // link is always rebuilt from the (cleaned) DOI rather than trusted as-is.
      const url = doi ? `https://doi.org/${doi}` : stripLatexEscapes(entry.URL) ?? ''

      console.log(`Generating post for ${entry.id}: "${entry.title}"`)
      const abstract = await fetchAbstract(entry)
      await sleep(3000) // stay well under Semantic Scholar's unauthenticated rate limit

      const post = buildPost({ entry, authorNames, blogAuthorKeys, year, pubDate, venue, url, abstract })
      const filePath = path.join(blogDir, `${pubDate}-paper-${slugifyKey(entry.id)}.md`)
      writeFileSync(filePath, post)
      console.log(`  wrote ${path.relative(websiteDir, filePath)}`)
    } catch (e) {
      console.warn(`  skipping ${entry.id}: ${e.message}`)
    }
  }
}

main().catch((e) => {
  console.warn(`generate-publication-blog-posts failed, skipping: ${e.message}`)
})
