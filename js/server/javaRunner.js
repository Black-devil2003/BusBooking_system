const { execFile } = require('child_process');
const path = require('path');

const projectRoot = path.join(__dirname, '..', '..');

function runJava(args) {
    return new Promise((resolve, reject) => {
        const javaArgs = ['-cp', `bin${path.delimiter}lib/*`, 'main.ApiHandler', ...args.map(String)];

        execFile('java', javaArgs, { cwd: projectRoot }, (error, stdout, stderr) => {
            if (error) {
                if (stderr) console.error(stderr);
                reject(error);
                return;
            }

            try {
                const output = stdout.trim();
                const lines = output.split('\n');
                resolve(JSON.parse(lines[lines.length - 1]));
            } catch (parseError) {
                console.error('Invalid Java response:', stdout);
                reject(parseError);
            }
        });
    });
}

module.exports = { runJava };
