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
	/*const query = client.query('INSERT INTO users (name, email, nif, hash_pin) VALUES ($1, $2, $3, $4) RETURNING *', [user.name,user.email,user.nif,user.hash_pin], function(error, result){
		if(error != null){
			callback(null);
			return;
		}
		delete result.rows[0].hash_pin;
		client.end();
		callback(result.rows[0]);
	});*/


	var query_1 = 'INSERT INTO users (name, email, nif, hash_pin) VALUES ($1, $2, $3, $4) RETURNING *';
	var query_1_params = [user.name,user.email,user.nif,user.hash_pin];
  	var query_2 = 'INSERT INTO creditcards(number, expiration, cvv, user_id) VALUES($1, $2, $3, $4)';
  	var query_2_params = [user.credit_card_number, user.credit_card_expiration, user.credit_card_cvv, null];

	client.query('BEGIN', function(err, result) {
		
		if(err) return rollback(err, client, callback);
		client.query(query_1, query_1_params, function(err, result_user) {

			if(err) return rollback(err, client, callback);

			//delete result_user.rows[0].hash_pin;
			query_2_params[3] = result_user.rows[0].id; //set user uuid for reference in credit card object.

			client.query(query_2, query_2_params, function(err, result_credit_card) {

				if(err) return rollback(err, client, callback);
				//disconnect after successful commit
				client.query('COMMIT', client.end.bind(client));
				callback(result_user.rows[0]);
			});
		});
	});
}

function rollback(err, client, callback) {
	console.log(err);
	//terminating a client connection will
	//automatically rollback any uncommitted transactions
	//so while it's not technically mandatory to call
	//ROLLBACK it is cleaner and more correct
	client.query('ROLLBACK', function() {
		client.end();
		callback(null);
	});
};


function checkLoginByEmail(user, callback){

	var client = openClient();

	client.connect();
	const query = client.query('SELECT * FROM users WHERE email = $1', [user.email], function(error, result){
		if(error){
			callback(null);
			return;
		}
		if(result.rowCount > 0 && bcrypt.compareSync(user.pin, result.rows[0].hash_pin)) {
			delete result.rows[0].hash_pin;

			client.query('SELECT id, number, expiration FROM creditcards WHERE user_id = $1', 
				[result.rows[0].id], 
				function (error1, result1) {
					if(error1) {
						callback(null);
						return;
					}
					result.rows[0].creditcards = result1.rows;
					callback(result.rows[0]);
				});
		} else { // wrong password/id
			callback(null);
		}
	});
}

function checkLoginByID(user, callback){

	var client = openClient();

	client.connect();
	const query = client.query('SELECT * FROM users WHERE id = $1', [user.id], function(error, result){
		if(error){
			callback(null);
			return;
		}
		if(result.rowCount > 0 && bcrypt.compareSync(user.pin, result.rows[0].hash_pin)) {
			delete result.rows[0].hash_pin;

			client.query('SELECT id, number, expiration FROM creditcards WHERE user_id = $1', 
				[result.rows[0].id], 
				function (error1, result1) {
					if(error1) {
						callback(null);
						return;
					}
					result.rows[0].creditcards = result1.rows;
					callback(result.rows[0]);
				});
		} else { // wrong password/id
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

exports.getMenuVersion = function(callback){
	var client = openClient();
	client.connect();
	const results = [];
	const query = client.query('SELECT * FROM globals WHERE key = $1;', ['menu_version'], function(error, result){
		client.end();
		if(error != null)
			callback(null)
		else callback(result.rows[0].value);	
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
		[order.user.id], 
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

			client.query('INSERT INTO order_items (product_id,order_id,quantity,unit_price) VALUES ($1,$2,$3,'+
				'(SELECT price FROM products WHERE id = $1)'+
				') RETURNING *',
				[product_id, resultingOrder.order.id, order.cart[prod_id]],
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


/**
* get previous orders from user
*/
function getPreviousOrders(user,offset,limit,callback) {
	var client = openClient();
	client.connect();
	var results = {};
	const query = client.query(
		'SELECT orders.id AS order_id, products.id AS product_id, products.name AS product_name, '+
		'order_items.quantity AS quantity, '+
		'order_items.unit_price AS unit_price, orders.order_timestamp AS timestamp '+
		'FROM (SELECT * FROM orders ORDER BY order_timestamp DESC '+
		'LIMIT $3 OFFSET $2 ) AS orders, order_items, products '+
		'WHERE orders.user_id = $1 '+
		'AND order_items.order_id = orders.id '+
		'AND order_items.product_id = products.id;',
		[user.id,offset,limit],
		function(error, result){
			if(error != null){
				console.log(error);
				callback(null);
				return;
			}
		}
	);
    // Stream results back one row at a time
    query.on('row', (row) => {
    	var o_id = row.order_id;
    	if(!results[o_id]) {
    		results[o_id] = {};
    		results[o_id].order_id = o_id;
    		results[o_id].timestamp = row.timestamp;
    		results[o_id].products = [];
    	}
    	var p = {};
    	p.id = row.product_id;
    	p.name = row.product_name;
    	p.quantity = row.quantity;
    	p.price = row.unit_price;
		results[o_id].products.push(p);
    });
    // After all data is returned, close connection and return results
    query.on('end', () => {
      callback(results);
    });
}


exports.insertUser = insertUser;
exports.getMenu = getMenu;
exports.checkLoginByEmail = checkLoginByEmail;
exports.checkLoginByID = checkLoginByID;
exports.insertOrder = insertOrder;
exports.getPreviousOrders = getPreviousOrders;