const express = require('express');
const { exec } = require('child_process');
const cors = require('cors');

const app = express();
app.use(cors());
app.use(express.json());

// API endpoint to get routes
app.get('/api/routes', (req, res) => {
    exec('java -cp "bin;lib/*" BusTicketApp --api --get-routes', (error, stdout, stderr) => {
        if (error) {
            console.error('Java app error:', error);
            res.json({ routes: [] });
            return;
        }
        try {
            const routes = JSON.parse(stdout.trim());
            res.json({ routes });
        } catch (e) {
            res.json({ routes: [] });
        }
    });
});

// API endpoint to get buses
app.get('/api/buses', (req, res) => {
    const routeId = req.query.routeId;
    exec(`java -cp "bin;lib/*" BusTicketApp --api --get-buses ${routeId}`, (error, stdout, stderr) => {
        if (error) {
            res.json({ buses: [] });
            return;
        }
        try {
            const buses = JSON.parse(stdout.trim());
            res.json({ buses });
        } catch (e) {
            res.json({ buses: [] });
        }
    });
});

app.listen(3000, () => {
    console.log('🚀 Server running on http://localhost:3000');
    console.log('📱 Web app available at http://localhost:3000');
});