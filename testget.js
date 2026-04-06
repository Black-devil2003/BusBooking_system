const { execSync } = require('child_process');
try {
    const out = execSync(`java -cp "bin;lib/*" main.ApiHandler appt-get-my "123456" ""`, { encoding: 'utf-8' });
    console.log("JAVA OUTPUT:", out.trim());
} catch(e) {
    console.log("ERROR:", e);
}
