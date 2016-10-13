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
  'CREATE TABLE users(id UUID PRIMARY KEY DEFAULT gen_random_uuid(),'+
  'name VARCHAR(120) not null, '+
  'email VARCHAR(120) not null unique, '+
  'nif VARCHAR(9) not null, '+
  'hash_pin TEXT not null)');
query.on('end', () => { client.end(); });
