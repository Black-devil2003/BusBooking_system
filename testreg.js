const http = require('http');

const dataReg = JSON.stringify({
  name: "New User",
  email: "newuser@example.com",
  password: "password123"
});

const req = http.request({
  hostname: 'localhost',
  port: 3000,
  path: '/api/register',
  method: 'POST',
  headers: {
    'Content-Type': 'application/json',
    'Content-Length': Buffer.byteLength(dataReg)
  }
}, res => {
  let body = '';
  res.on('data', d => body += d);
  res.on('end', () => {
      console.log('Register Response:', body);
      // Wait for 1 second, then login
      setTimeout(() => {
          const req2 = http.request({
              hostname: 'localhost',
              port: 3000,
              path: '/api/login',
              method: 'POST',
              headers: {
                'Content-Type': 'application/json',
                'Content-Length': Buffer.byteLength(dataReg)
              }
            }, res2 => {
              let body2 = '';
              res2.on('data', d => body2 += d);
              res2.on('end', () => console.log('Login Response:', body2));
            });
            req2.write(JSON.stringify({email: "newuser@example.com", password: "password123"}));
            req2.end();
      }, 1000);
  });
});

req.on('error', error => console.error(error));
req.write(dataReg);
req.end();
