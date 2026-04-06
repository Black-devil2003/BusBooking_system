const http = require('http');

const dataLogin = JSON.stringify({
  email: "newuser@example.com",
  password: "password123"
});

const req2 = http.request({
  hostname: 'localhost',
  port: 3000,
  path: '/api/login',
  method: 'POST',
  headers: {
    'Content-Type': 'application/json',
    'Content-Length': Buffer.byteLength(dataLogin)
  }
}, res2 => {
  let body2 = '';
  res2.on('data', d => body2 += d);
  res2.on('end', () => console.log('Login Response:', body2));
});
req2.write(dataLogin);
req2.end();
