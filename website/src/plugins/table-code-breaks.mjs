// Rehype plugin: inside table cells, lets inline code wrap after `.`, `(`, `,`, `<`, `>` and `:`, so that long
// identifiers break at their punctuation instead of mid-word. It inserts zero-width spaces: an element such as <wbr>
// would turn the inline code into a code block. src/clientModules/fitTables.js strips them from copied text.
const BREAK_AFTER = /([.(,<>:])(?=.)/g;

function walk(node, inCell, inCode) {
  if (node.type === 'text' && inCell && inCode) {
    node.value = node.value.replace(BREAK_AFTER, '$1​');
    return;
  }
  if (!node.children) return;
  const isElement = node.type === 'element';
  const cell = inCell || (isElement && (node.tagName === 'td' || node.tagName === 'th'));
  const code = inCode || (isElement && node.tagName === 'code');
  for (const child of node.children) walk(child, cell, code);
}

export default function tableCodeBreaks() {
  return (tree) => walk(tree, false, false);
}
