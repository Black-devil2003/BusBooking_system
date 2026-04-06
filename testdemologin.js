const http = require('http');

const dataLogin = JSON.stringify({
  email: "user@example.com",
  password: "password"
});

const req = http.request({
  hostname: 'localhost',
  port: 3000,
  path: '/api/login',
  method: 'POST',
  headers: {
    'Content-Type': 'application/json',
    'Content-Length': Buffer.byteLength(dataLogin)
  }
}, res => {
  let body = '';
  res.on('data', d => body += d);
  res.on('end', () => console.log('Login Response:', body));
});
req.write(dataLogin);
req.end();
