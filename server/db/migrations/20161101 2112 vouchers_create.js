require('dotenv').config();

const pg = require('pg');

const client = new pg.Client({
    user: process.env.DATABASE_USER,
    password: process.env.DATABASE_PASSWORD,
    database: process.env.DATABASE_SERVER,
    port: process.env.DATABASE_PORT,
    host: process.env.DATABASE_HOST,
    ssl: true
}); 


client.connect();
const query = client.query(
  'CREATE TABLE vouchers('+
    'serial_id integer PRIMARY KEY, '+
    'type CHAR(1) not null, '+ 
    'signature CHAR(46) not null, '+
    'user_id UUID not null REFERENCES users(id))');
query.on('end', () => { client.end(); });