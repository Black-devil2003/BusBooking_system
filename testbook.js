const { execSync } = require('child_process');

try {
    const out = execSync(`java -cp "bin;lib/*" main.ApiHandler appt-book "TEST-ID" "Tester" "t@t.com" "123456" "2026-04-10" "10:00 AM" "Mumbai" "Pune" 500.5`, { encoding: 'utf-8' });
    console.log("JAVA OUTPUT:", out.trim());
} catch(e) {
    console.log("ERROR:", e.output ? e.output.toString() : e);
}
