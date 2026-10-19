const { chromium } = require('playwright');
const path = require('path');
(async () => {
  const b = await chromium.launch();
  const p = await b.newPage({ viewport: { width: 1280, height: 200 }, deviceScaleFactor: 1.5 });
  for (const n of ['current-workflow', 'mockup-thesis-input', 'mockup-results', 'mockup-saved-project']) {
    await p.goto('file://' + path.join(__dirname, n + '.html'));
    await p.screenshot({ path: path.join(__dirname, '..', n + '.png'), fullPage: true });
  }
  await b.close();
})();
