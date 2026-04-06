const { execSync } = require('child_process');

function runJava(params) {
    const command = `java -cp "bin;lib/*" main.ApiHandler ${params}`;
    console.log('Command:', command);
    const out = execSync(command, { encoding: 'utf-8' });
    console.log("JAVA OUTPUT:", out.trim());
}

const bookingId = "123";
const name = "John";
const email = "e";
const phone = "1";
const date = "2026-04-23";
const timeSlot = "2:30 PM";
const source = "Mumbai";
const destination = "Pune";
const fare = 450;

runJava(`appt-book "${bookingId}" "${name}" "${email}" "${phone}" ${date} "${timeSlot}" "${source || ''}" "${destination || ''}" ${fare || 0}`);
