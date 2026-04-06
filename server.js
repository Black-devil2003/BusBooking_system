const express = require('express');
const { exec } = require('child_process');
const cors = require('cors');
const path = require('path');

const app = express();
app.use(cors());
app.use(express.json());

// Serve static files from web directory
app.use(express.static(path.join(__dirname, 'web')));

// Helper: Run Java ApiHandler
function runJava(params) {
    return new Promise((resolve, reject) => {
        const command = `java -cp "bin;lib/*" main.ApiHandler ${params}`;
        console.log('Running Java:', command);
        exec(command, (error, stdout, stderr) => {
            if (error) {
                console.error('Java Execution Error:', error);
                reject(error);
                return;
            }
            try {
                const output = stdout.trim();
                // Filter out non-JSON lines (like loading messages)
                const lines = output.split('\n');
                const lastLine = lines[lines.length - 1];
                resolve(JSON.parse(lastLine));
            } catch (e) {
                console.error('JSON Parse Error:', e, 'Raw output:', stdout);
                reject(new Error('Invalid response from backend'));
            }
        });
    });
}

// Helper: generate a short unique booking ID
function genBookingId() {
    const chars = 'ABCDEFGHJKLMNPQRSTUVWXYZ23456789';
    let id = 'APT-';
    for (let i = 0; i < 6; i++) id += chars[Math.floor(Math.random() * chars.length)];
    return id;
}

// ---------------------------------------------------------
// BUS & ROUTE API (Original Java-backed)
// ---------------------------------------------------------

app.get('/api/routes', async (req, res) => {
    try {
        const routes = await runJava('get-routes');
        res.json({ routes });
    } catch (e) { res.json({ routes: [] }); }
});

app.get('/api/buses', async (req, res) => {
    try {
        const buses = await runJava(`get-buses ${req.query.routeId}`);
        res.json({ buses });
    } catch (e) { res.json({ buses: [] }); }
});

app.get('/api/seats', async (req, res) => {
    try {
        const seats = await runJava(`get-seats ${req.query.busId}`);
        res.json({ seats });
    } catch (e) { res.json({ seats: [] }); }
});

app.post('/api/book', async (req, res) => {
    const { busId, seatNumber, passengerName, phone, email } = req.body;
    const date = new Date().toISOString().split('T')[0];
    try {
        const result = await runJava(`book-ticket ${busId} ${seatNumber} "${passengerName}" "${phone}" "${email}" ${date}`);
        res.json(result);
    } catch (e) { res.json({ success: false, message: e.message }); }
});

// ---------------------------------------------------------
// AUTHENTICATION API (MIGRATED TO JAVA/MYSQL)
// ---------------------------------------------------------

app.post('/api/register', async (req, res) => {
    const { name, email, password } = req.body;
    try {
        const result = await runJava(`register "${name}" "${email}" "${password}"`);
        if (result.success) res.json({ success: true, message: 'Account created successfully!' });
        else res.status(400).json({ success: false, message: 'Registration failed' });
    } catch (e) { res.status(500).json({ success: false, message: e.message }); }
});

app.post('/api/login', async (req, res) => {
    const { email, password } = req.body;
    try {
        const result = await runJava(`login "${email}" "${password}"`);
        if (result.success) {
            const token = Buffer.from(`${email}:${Date.now()}`).toString('base64');
            res.json({ success: true, token, user: result.user });
        } else {
            res.status(401).json({ success: false, message: 'Invalid credentials' });
        }
    } catch (e) { res.status(500).json({ success: false, message: e.message }); }
});

// ---------------------------------------------------------
// ROUTE MANAGEMENT API (MIGRATED TO JAVA/MYSQL)
// ---------------------------------------------------------

app.get('/api/manage/routes', async (req, res) => {
    try {
        const routes = await runJava('get-routes');
        res.json({ success: true, routes });
    } catch (e) { res.status(500).json({ success: false, message: e.message }); }
});

app.post('/api/manage/routes', async (req, res) => {
    const { source, destination, fare, durationMinutes } = req.body;
    try {
        const result = await runJava(`manage-add-route "${source}" "${destination}" ${fare} ${durationMinutes}`);
        res.json(result);
    } catch (e) { res.status(500).json({ success: false, message: e.message }); }
});

app.put('/api/manage/routes/:id', async (req, res) => {
    const { source, destination, fare, durationMinutes } = req.body;
    try {
        const result = await runJava(`manage-update-route ${req.params.id} "${source}" "${destination}" ${fare} ${durationMinutes}`);
        res.json(result);
    } catch (e) { res.status(500).json({ success: false, message: e.message }); }
});

app.delete('/api/manage/routes/:id', async (req, res) => {
    try {
        const result = await runJava(`manage-delete-route ${req.params.id}`);
        res.json(result);
    } catch (e) { res.status(500).json({ success: false, message: e.message }); }
});

app.get('/api/manage/routes/stats', async (req, res) => {
    try {
        const stats = await runJava('manage-stats');
        res.json({ success: true, stats });
    } catch (e) { res.json({ success: false, stats: {} }); }
});

// ---------------------------------------------------------
// APPOINTMENTS API (MIGRATED TO JAVA/MYSQL)
// ---------------------------------------------------------

app.post('/api/appointments/book', async (req, res) => {
    const { name, email, phone, date, timeSlot, source, destination, fare } = req.body;
    const bookingId = genBookingId();
    try {
        const result = await runJava(`appt-book "${bookingId}" "${name}" "${email}" "${phone}" ${date} "${timeSlot}" "${source || ''}" "${destination || ''}" ${fare || 0}`);
        res.json(result);
    } catch (e) { res.status(500).json({ success: false, message: e.message }); }
});

app.get('/api/appointments/all', async (req, res) => {
    try {
        const appointments = await runJava('appt-get-all');
        res.json({ appointments });
    } catch (e) { res.json({ appointments: [] }); }
});

app.get('/api/appointments/taken-slots', async (req, res) => {
    try {
        const takenSlots = await runJava(`appt-get-taken-slots ${req.query.date}`);
        res.json({ takenSlots });
    } catch (e) { res.json({ takenSlots: [] }); }
});

app.get('/api/my-bookings', async (req, res) => {
    const { phone, email } = req.query;
    try {
        // Fallback to empty if both missing
        if (!phone && !email) return res.json({ success: true, bookings: [] });
        const bookings = await runJava(`appt-get-my "${phone || ''}" "${email || ''}"`);
        res.json({ success: true, bookings });
    } catch (e) { res.json({ success: false, bookings: [] }); }
});

// SPA catch-all
app.use((req, res, next) => {
    if (req.method === 'GET' && !req.path.startsWith('/api')) {
        const webPath = (req.path === '/routes-manager' || req.path === '/routes-manager.html') 
            ? 'routes-manager.html' : 'index.html';
        res.sendFile(path.join(__dirname, 'web', webPath));
    } else {
        next();
    }
});

const PORT = process.env.PORT || 3000;
app.listen(PORT, () => {
    console.log(`🚀 Server running on http://localhost:${PORT}`);
    console.log(`✅ All data now in MySQL.`);
});