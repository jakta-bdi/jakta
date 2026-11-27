# JaKtA website

The JaKtA website, built with [Docusaurus](https://docusaurus.io/) and published at https://jakta-bdi.github.io.

```bash
npm ci
npm start      # dev server at http://localhost:3000
npm run build  # static build, fails on broken links
```

## Publications

The [Publications](https://jakta-bdi.github.io/publications) page and the paper blog posts are both generated from
`static/jakta-references.bib`. To add a paper:

1. add its BibTeX entry to `static/jakta-references.bib` (from [DBLP](https://dblp.org) when it is there);
2. run `node scripts/generate-publication-blog-posts.mjs` from this folder: it writes a post in `blog/` for each
   entry that has none, with the abstract from Semantic Scholar when available
   (set `SEMANTIC_SCHOLAR_API_KEY` to avoid its rate limits);
3. review the generated post and commit it with the bib change.

The script never touches existing posts: a post is linked to its entry by a `jakta-publication-bib-key` comment,
so it can be edited freely. Posts are dated January 1st of the publication year unless the entry has a month.

## Releases

Release notes under `releases/` are written on each minor or major release by `scripts/generate-release-blog-post.mjs`
at the repository root (see `release.config.js`).
