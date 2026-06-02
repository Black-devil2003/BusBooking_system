const fs = require('fs');
const path = require('path');

const projectRoot = path.join(__dirname, '..', '..');
const binDir = path.join(projectRoot, 'bin');
const srcDir = path.join(projectRoot, 'src');

function removeClassFiles(dir) {
    if (!fs.existsSync(dir)) return;

    for (const entry of fs.readdirSync(dir, { withFileTypes: true })) {
        const fullPath = path.join(dir, entry.name);

        if (entry.isDirectory()) {
            removeClassFiles(fullPath);
        } else if (entry.isFile() && entry.name.endsWith('.class')) {
            fs.rmSync(fullPath, { force: true });
        }
    }
}

fs.rmSync(binDir, { recursive: true, force: true });
removeClassFiles(srcDir);
fs.mkdirSync(binDir, { recursive: true });

console.log('Cleaned compiled Java files.');
