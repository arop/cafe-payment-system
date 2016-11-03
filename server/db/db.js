require('dotenv').config();

const pg = require('pg');
const connectionString = process.env.DATABASE_URL;
const 
	bcrypt = require('../lib/bCrypt.js'),
	rsa = require('jsrsasign'),
	rsau = require('jsrsasign-util');

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

	var query_1 = 'INSERT INTO users (name, email, nif, hash_pin) VALUES ($1, $2, $3, $4) RETURNING *';
	var query_1_params = [user.name,user.email,user.nif,user.hash_pin];
  	var query_2 = 'INSERT INTO creditcards(number, expiration, cvv, user_id) VALUES($1, $2, $3, $4) RETURNING *';
  	var query_2_params = [user.credit_card_number, user.credit_card_expiration, user.credit_card_cvv, null];
  	var query_3 = 'UPDATE users SET primary_credit_card = $1 WHERE id = $2 RETURNING *';

	client.query('BEGIN', function(err, result) {
		
		if(err) return rollback(err, client, callback);
		client.query(query_1, query_1_params, function(err, result_user) {

			if(err) return rollback(err, client, callback);

			var user_result = result_user.rows[0];

			delete user_result.hash_pin;
			query_2_params[3] = user_result.id; //set user uuid for reference in credit card object.

			client.query(query_2, query_2_params, function(err, result_credit_card) {

				if(err) return rollback(err, client, callback);

				delete result_credit_card.rows[0].cvv;
				user_result.creditcards = [result_credit_card.rows[0]];

				client.query(query_3, [result_credit_card.rows[0].id, user_result.id], 
					function (err, result_add_cc) {

					if(err) return rollback(err, client, callback);

					user_result.primary_credit_card = result_add_cc.rows[0].primary_credit_card;

					//disconnect after successful commit
					client.query('COMMIT', client.end.bind(client));
					callback(user_result);
				});

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
	client.query('INSERT INTO orders (user_id, credit_card, order_timestamp) VALUES '+
		'($1, (SELECT primary_credit_card FROM users WHERE id = $1), round(date_part( \'epoch\', now())*1000)) RETURNING *', 
		[order.user.id], 
		function(error, result){
			if(error != null){
				callback(null);
				return;
			}		
			else {
				resultingOrder.order = result.rows[0];
				resultingOrder.order.order_items = [];
				resultingOrder.order.total_price = 0;
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
					resultingOrder.order.order_items.push(result.rows[0]);
					resultingOrder.order.total_price += result.rows[0].unit_price * result.rows[0].quantity;
				}
			).on('row', (row) => {
				client.query('SELECT name FROM products WHERE id = $1;', [row.product_id], function(error, result2){
					row.name = result2.rows[0].name;
				});
			}).on('end',() => {
				//only call callback when all queries finish
				numberOfProducts--;
				if(numberOfProducts <= 0) {

					client.query('SELECT name, number FROM users, creditcards WHERE users.id = $1 AND creditcards.id = users.primary_credit_card;',
						[resultingOrder.order.user_id], function(error, result){
						if(error != null)
							callback(null)

						resultingOrder.order.user_name = result.rows[0].name;
						resultingOrder.order.credit_card = result.rows[0].number;
						callback(resultingOrder);

						if(resultingOrder.order.total_price >= 20.0){
							issueOfferVoucher(order.user.id);
						}


						client.query('SELECT updateordertotals($1, $2)', 
							[resultingOrder.order.total_price, order.user.id], function(error, result3){
								if(error != null){
									// n vamos lidar com este erro e ignorar totalmente que n? funcionou.
									// de qq forma, ?uma opera?o simples, por isso n deve dar erro. nunca.
									return;
								}

								client.query('SELECT * from users where id = $1',
									[order.user.id], function(error, result4){
										if(error != null){
											// n vamos lidar com este erro e ignorar totalmente que n? funcionou.
											// de qq forma, ?uma opera?o simples, por isso n deve dar erro. nunca.
											return;
										}

										console.log(result4);
										var total_orders_value = result4.rows[0].total_order_value;
										var total_vouchers_issued = result4.rows[0].discount_vouchers_issued;
										var vouchers_to_issue = Math.floor(total_orders_value / 100 ) - total_vouchers_issued;
										
										console.log("value: " + total_orders_value + " # vouchers: " + total_vouchers_issued);
										console.log("to issue: "+vouchers_to_issue);

										if(vouchers_to_issue > 0){
											for(var i = 0; i < vouchers_to_issue; i++){
												issueDiscountVoucher(order.user.id);
											}
										}
									}
								);

							}
						);
					});
				}
			});
		}
	});
}


function issueOfferVoucher(user_id){

	var rand1 = Math.floor(Math.random() * 999 + 1); // 1 to 1000
	var rand2 = Math.floor(Math.random() * 499  + 1); // 1 to 500
	var voucher_serial = rand1 + rand2;

	var voucher_type;
	if( Math.floor(Math.random() + 1) == 1 ) 
		voucher_type = 'p'; // popcorn
	else voucher_type = 'c'; // coffee

	var pem = rsau.readFile('./keys/private.pem');
	var prvKey = rsa.KEYUTIL.getKey(pem);

	var sig = new rsa.Signature({alg: 'SHA1withRSA'});
	sig.init(prvKey);
	sig.updateString(voucher_serial);
	var cryp_signature = sig.sign();

	var client = openClient();
	client.connect();
	const query = client.query(
		"INSERT INTO vouchers (serial_id, type, signature, user_id, order_id) VALUES ($1, $2, decode($3, 'hex'), $4, null)",
		[voucher_serial, voucher_type, cryp_signature, user_id],
		function(error, result){
			if(error != null){
				console.log(error);
				return;
			}
		}
	);
}

function issueDiscountVoucher(user_id){

	var rand1 = Math.floor(Math.random() * 999 + 1); // 1 to 1000
	var rand2 = Math.floor(Math.random() * 499  + 1); // 1 to 500
	var voucher_serial = rand1 + rand2;

	var voucher_type = 'd';

	var pem = rsau.readFile('./keys/private.pem');
	var prvKey = rsa.KEYUTIL.getKey(pem);

	var sig = new rsa.Signature({alg: 'SHA1withRSA'});
	sig.init(prvKey);
	sig.updateString(voucher_serial);
	var cryp_signature = sig.sign();

	/*var cryp_signature_encoded = ""
	for(var i = 0; i < cryp_signature.length; i++){
		var first = ('0x'+cryp_signature.charAt(i)) << 0;
		var first_shifted = ('0x'+cryp_signature.charAt(i)) << 4;
		var second = ('0x'+cryp_signature.charAt(++i)) << 0;
		var new_char = first_shifted+second;
		
		var new_char_string = String.fromCharCode(new_char); 

		cryp_signature_encoded += new_char_string;

		console.log('        First char code: '+first);
		console.log('First char code shifted: '+first_shifted);
		console.log('       Second char code: ' + second);
		console.log('          New char code: ' + new_char);
		console.log('        New char string: '+new_char_string);
		console.log('--------------------------------------------------');

	}
	console.log(cryp_signature_encoded);
	console.log(cryp_signature_encoded.length);*/


	var client = openClient();
	client.connect();
	const query = client.query(
		"INSERT INTO vouchers (serial_id, type, signature, user_id, order_id) VALUES ($1, $2, decode($3, 'hex'), $4, null)",
		[voucher_serial, voucher_type, cryp_signature, user_id],
		function(error, result){
			if(error != null){
				console.log(error);
				return;
			}
		}
	);

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

/**
* get valid vouchers of user
*/
function getValidVouchers(user,callback) {
	var client = openClient();
	client.connect();
	
	const query = client.query(
		'SELECT * '+
		'FROM vouchers '+
		'WHERE user_id = $1 '+
		'AND order_id = null;',
		[user.id],
		function(error, result){
			if(error != null){
				console.log(error);
				callback(null);
				return;
			}

			callback(result.rows);
			client.end();
		}
	);
}


/**
* insert credit card
*/
function insertCreditCard(user, credit_card, callback) {
	var client = openClient();
	client.connect();
	const query = client.query('INSERT INTO creditcards (number,expiration,cvv,user_id) '+
		'VALUES ($1,$2,$3,$4) RETURNING *;',
		[credit_card.number,credit_card.exp_date,credit_card.cvv,user.id], function(error,result) {
			if(error != null) {
				callback(null);
				return;
			}

			delete result.rows[0].cvv;
			callback(result.rows[0]);
		});
}

/**
* change user's primary card
*/
function setPrimaryCreditCard(user, credit_card, callback) {
	var client = openClient();
	client.connect();
	const query = client.query('UPDATE users SET primary_credit_card = $1 WHERE id = $2 '+
		'RETURNING *;',
		[credit_card.id, user.id], function(error,result) {
			if(error != null) {
				callback(null);
				return;
			}

			callback(result.rows[0]);
		});
}


exports.insertUser = insertUser;
exports.getMenu = getMenu;
exports.checkLoginByEmail = checkLoginByEmail;
exports.checkLoginByID = checkLoginByID;
exports.insertOrder = insertOrder;
exports.getPreviousOrders = getPreviousOrders;

exports.issueDiscountVoucher = issueDiscountVoucher;
exports.getPreviousOrders = getPreviousOrders;
exports.setPrimaryCreditCard = setPrimaryCreditCard;
exports.insertCreditCard = insertCreditCard;

exports.getValidVouchers = getValidVouchers;