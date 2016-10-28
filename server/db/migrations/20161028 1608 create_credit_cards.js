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
  'CREATE TABLE creditcards('+
    'id SERIAL PRIMARY KEY, '+
    'number VARCHAR(16) not null, '+
    'expiration VARCHAR(5) not null, '+
    'cvv VARCHAR(3) not null, '+
    'user_id UUID not null REFERENCES users(id))');
query.on('end', () => { client.end(); });