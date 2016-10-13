require('dotenv').config();

const pg = require('pg');
const connectionString = process.env.DATABASE_URL;

/*pg.on('error', function (err) {
  console.log('Database error!', err);
});*/

const client = new pg.Client({
    user: process.env.DATABASE_USER,
    password: process.env.DATABASE_PASSWORD,
    database: process.env.DATABASE_SERVER,
    port: process.env.DATABASE_PORT,
    host: process.env.DATABASE_HOST,
    ssl: true
}); 


function insertUser(user, callback){

	client.connect();
	const query = client.query('INSERT INTO users (name, email, nif, hash_pin) VALUES ($1, $2, $3, $4) RETURNING *', [user.name,user.email,user.nif,user.hash_pin], function(error, result){
		if(error != null){
			callback(null);
			return;
		}
		callback(result.rows[0]);
		
		//done();
	});
	//query.on('end', () => { client.end(); });

}


function getMenu(callback){

	client.connect();
	const results = [];
	const query = client.query('SELECT * FROM products ORDER BY id ASC;');
    // Stream results back one row at a time
    query.on('row', (row) => {
      results.push(row);
    });
    // After all data is returned, close connection and return results
    query.on('end', () => {
      callback(results);
    });

}


exports.insertUser = insertUser;
exports.getMenu = getMenu;