require('dotenv').config();

const pg = require('pg');
const connectionString = process.env.DATABASE_URL;
const bcrypt = require('../lib/bCrypt.js');

/*pg.on('error', function (err) {
  console.log('Database error!', err);
});*/

function openClient(){
	const client = new pg.Client({
	    user: process.env.DATABASE_USER,
	    password: process.env.DATABASE_PASSWORD,
	    database: process.env.DATABASE_SERVER,
	    port: process.env.DATABASE_PORT,
	    host: process.env.DATABASE_HOST,
	    ssl: true
	}); 
	return client;
}

/**
* Register
*/
function insertUser(user, callback){

	var client = openClient();

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

function checkLogin(user, callback){

	var client = openClient();

	client.connect();
	const query = client.query('SELECT * FROM users WHERE email = $1', [user.email], function(error, result){
		if(error != null){
			callback(null);
			return;
		}
		if(result.rowCount > 0 && bcrypt.compareSync(user.pin, result.rows[0].hash_pin)) {
			delete result.rows[0].hash_pin;
			callback(result.rows[0]);
			return;
		} else { // wrong password/email
			callback(null);
		}

	});
}

function getMenu(callback){
	var client = openClient();
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

/**
* insert order
*/

function insertOrder(order,callback) {
	var client = openClient();
	var resultingOrder = {};

	client.connect();
	client.query('INSERT INTO orders (user_id, order_timestamp) VALUES ($1,CURRENT_TIMESTAMP) RETURNING *', 
		[order.user], 
		function(error, result){
			if(error != null){
				callback(null);
				return;
			}		
			else {
				resultingOrder.order = result.rows[0];
				resultingOrder.order_items = [];
			}
		}
	).on('end', () => {
		var numberOfProducts = Object.keys(order.cart).length;
		for(var prod_id in order.cart) {
		    if (!order.cart.hasOwnProperty(prod_id)) continue;

		    var product_id = parseInt(prod_id);

			client.query('INSERT INTO order_items (product_id,order_id,quantity,unit_price) VALUES ($1,$2,$3,$4) RETURNING *', 
				[product_id, resultingOrder.order.id, order.cart[prod_id], 1], //TODO change unit price
				function(error, result){
					if(error != null){
						console.log(error);
						return;
					}
					resultingOrder.order_items.push(result.rows[0]);
				}
			).on('end',() => {
				//only call callback when all queries finish
				numberOfProducts--;
				if(numberOfProducts <= 0) {
					callback(resultingOrder);
				}
			});
		}
	});
}

exports.insertUser = insertUser;
exports.getMenu = getMenu;
exports.checkLogin = checkLogin;
exports.insertOrder = insertOrder;