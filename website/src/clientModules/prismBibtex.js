import {Prism} from 'prism-react-renderer';

// Prism ships no BibTeX grammar: this minimal one colours the entry type, the field names and the values,
// so the citation on the home page uses the site's code colours.
Prism.languages.bibtex = {
  keyword: /@\w+/,
  function: /\b[\w-]+(?=\s*=)/,
  // a value in braces after '=', with one level of nested braces such as {BDI}
  string: {pattern: /(=\s*)\{(?:[^{}]|\{[^{}]*\})*\}/, lookbehind: true, greedy: true},
  punctuation: /[{}=,]/,
};
