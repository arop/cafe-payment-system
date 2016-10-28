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
  'user_id UUID not null REFERENCES users(id),'+
  'order_timestamp timestamp with time zone)');
query.on('end', () => { createOrderItemTable(); });


function createOrderItemTable() {
  const query1 = client.query(
    'CREATE TABLE order_items(id SERIAL PRIMARY KEY,'+
    'product_id integer not null REFERENCES products(id),'+
    'order_id integer not null REFERENCES orders(id),'+
    'quantity smallint not null,'+
    'unit_price real not null)');
  query1.on('end', () => { client.end(); });
}