const http = require('http');

const data = JSON.stringify({
  name: "John Doe",
  email: "john@example.com",
  phone: "1234567890",
  date: "2026-04-23",
  timeSlot: "2:30 PM",
  source: "Mumbai",
  destination: "Pune",
  fare: 450
});

const options = {
  hostname: 'localhost',
  port: 3000,
  path: '/api/appointments/book',
  method: 'POST',
  headers: {
    'Content-Type': 'application/json',
    'Content-Length': data.length
  }
};

const req = http.request(options, res => {
  let body = '';
  res.on('data', d => body += d);
  res.on('end', () => console.log('Response:', body));
});

req.on('error', error => console.error(error));
req.write(data);
req.end();
