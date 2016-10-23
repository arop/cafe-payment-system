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
query0 = client.query('DROP TABLE IF EXISTS globals');
query0.on('end', () => { 
	query1 = client.query(
	  'CREATE TABLE globals(id SERIAL PRIMARY KEY,'+
	  'key VARCHAR not null, '+
	  'value VARCHAR not null)');
	query1.on('end', () => { 
		query2 = client.query('INSERT INTO globals (key, value) VALUES (\'menu_version\', round(date_part( \'epoch\', now())*1000))');
		query2.on('end', () => { 
			client.end();
		});
	});
});

