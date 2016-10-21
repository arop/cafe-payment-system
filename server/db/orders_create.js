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
  'CREATE TABLE orders(id SERIAL PRIMARY KEY,'+
  'user_id UUID not null)');
query.on('end', () => { client.end(); });

client.connect();
const query1 = client.query(
  'CREATE TABLE order_item(id SERIAL PRIMARY KEY,'+
  'product_id int not null,'+
  'quantity smallint not null,'+
  'order_id integer not null)');
query1.on('end', () => { client.end(); });