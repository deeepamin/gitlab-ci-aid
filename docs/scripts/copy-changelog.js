const fs = require('fs');
const path = require('path');

const sourcePath = path.resolve(__dirname, '../../CHANGELOG.md');
const targetPath = path.resolve(__dirname, '../docs/changelog.md');

const frontmatter = `---\nid: changelog\ntitle: 📝 Changelog\nhide_title: true\nhide_table_of_contents: false\n---\n\n`;

const changelog = fs.readFileSync(sourcePath, 'utf-8');
fs.writeFileSync(targetPath, frontmatter + changelog, 'utf-8');

console.log('✅ Synced CHANGELOG.md to Docusaurus docs');
