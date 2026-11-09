// Webpack loader for .bib files: formats the references in Node at build time and exports
// [{year, citations: [html]}] (newest year first), so citation-js and citeproc never reach the browser.
const {Cite} = require('@citation-js/core');
require('@citation-js/plugin-bibtex');
require('@citation-js/plugin-csl');

module.exports = function bibtexLoader(source) {
  const sections = new Map();
  for (const entry of new Cite(source).get({type: 'json'})) {
    const year = String(entry.issued?.['date-parts']?.[0]?.[0] ?? entry.year ?? 'Unknown');
    let citation = new Cite([entry]).format('bibliography', {format: 'html', template: 'apa', lang: 'en-US'});
    if (entry.URL) {
      citation = citation.replace(/(https?:\/\/[^\s<]+)/g, '<a href="$1" target="_blank" rel="noopener noreferrer">$1</a>');
    }
    sections.set(year, [...(sections.get(year) ?? []), citation]);
  }
  const years = [...sections.keys()].sort((a, b) => b.localeCompare(a, undefined, {numeric: true}));
  return `export default ${JSON.stringify(years.map((year) => ({year, citations: sections.get(year)})))};`;
};
