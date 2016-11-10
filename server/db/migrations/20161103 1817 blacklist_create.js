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
  'CREATE TABLE blacklist(id SERIAL PRIMARY KEY, '+
  'user_id UUID not null REFERENCES users(id) UNIQUE)');
query.on('end', () => { client.end(); });
