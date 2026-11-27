// Tables must never overflow the content column. They wrap prose between words and inline code at its punctuation;
// a table that still does not fit first gets `table--compact` (smaller text), then `table--squeeze`, which lets any
// word break (see custom.css).
import ExecutionEnvironment from '@docusaurus/ExecutionEnvironment';

function fitTables() {
  for (const table of document.querySelectorAll('.markdown table')) {
    const container = table.parentElement.getBoundingClientRect().width;
    if (container === 0) continue; // inside a closed <details>: measured when opened
    table.classList.remove('table--compact', 'table--squeeze');
    for (const fallback of ['table--compact', 'table--squeeze']) {
      if (table.getBoundingClientRect().width <= container + 1) break;
      table.classList.add(fallback);
    }
  }
}

// Table code carries zero-width spaces as break points (src/plugins/table-code-breaks.mjs): never copy them.
function copyWithoutBreakPoints(event) {
  const text = window.getSelection()?.toString() ?? '';
  if (!text.includes('\u200B')) return;
  event.clipboardData.setData('text/plain', text.replaceAll('\u200B', ''));
  event.preventDefault();
}

if (ExecutionEnvironment.canUseDOM) {
  window.addEventListener('resize', fitTables);
  document.addEventListener('toggle', fitTables, true);
  document.addEventListener('copy', copyWithoutBreakPoints);
}

export function onRouteDidUpdate() {
  // After the new page is in the DOM and laid out.
  requestAnimationFrame(fitTables);
}
