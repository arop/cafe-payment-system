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


const POPCORN_ID = 12;
const COFFEE_ID = 13;


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

function rollback(err, client) {
	console.log(err);
	//terminating a client connection will
	//automatically rollback any uncommitted transactions
	//so while it's not technically mandatory to call
	//ROLLBACK it is cleaner and more correct
	client.query('ROLLBACK', function() {
		client.end();
	});
};

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
		
		if(err){
			callback(null);
			return rollback(err, client);	
		} 
		client.query(query_1, query_1_params, function(err, result_user) {

			if(err){
				callback(null);
				return rollback(err, client);	
			} 

			var user_result = result_user.rows[0];

			delete user_result.hash_pin;
			query_2_params[3] = user_result.id; //set user uuid for reference in credit card object.

			client.query(query_2, query_2_params, function(err, result_credit_card) {

				if(err){
					callback(null);
					return rollback(err, client);	
				} 

				delete result_credit_card.rows[0].cvv;
				user_result.creditcards = [result_credit_card.rows[0]];

				client.query(query_3, [result_credit_card.rows[0].id, user_result.id], 
					function (err, result_add_cc) {

					if(err){
						callback(null);
						return rollback(err, client);	
					} 

					user_result.primary_credit_card = result_add_cc.rows[0].primary_credit_card;

					//disconnect after successful commit
					client.query('COMMIT', client.end.bind(client));
					callback(user_result);
				});

			});
		});
	});
}



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

	client.query('BEGIN', function(err, result) {
		client.query('SELECT * from blacklist WHERE user_id = $1;', [order.user.id],
			function(error, result){
				if(error){
					callback({'error' : 'Error checking blacklist!'});
					return rollback(err, client);
				}

				if(result.rowCount > 0){
					callback({'blacklist' : true});
					rollback(err, client);
					return;	
				}

				client.query('INSERT INTO orders (user_id, credit_card, order_timestamp) VALUES '+
					'($1, (SELECT primary_credit_card FROM users WHERE id = $1), round(date_part( \'epoch\', now())*1000)) RETURNING *', 
					[order.user.id], 
					function(error, result){
						if(error){
							callback({'error' : 'Error inserting order in db!'});
							return rollback(err, client);
						}
						
						resultingOrder.order = result.rows[0];
						resultingOrder.order.order_items = [];
						resultingOrder.order.total_price = 0;			

						insertOrder_insertProducts(client, order, resultingOrder, callback);
					}
				);
		});
		
	});
		
}

function insertOrder_insertProducts(client, order, resultingOrder, callback){
	var numberOfProducts = Object.keys(order.cart).length;
		
	order.number_of_popcorns = 0;
	order.number_of_coffees = 0;

	var abort_mission = false;

	for(var prod_id in order.cart) {
	    if (!order.cart.hasOwnProperty(prod_id)) continue;

	    var product_id = parseInt(prod_id);

	    if(product_id == POPCORN_ID) order.number_of_popcorns++;
	    else if(product_id == COFFEE_ID) order.number_of_coffees++;

		client.query('INSERT INTO order_items (product_id,order_id,quantity,unit_price) VALUES ($1,$2,$3,'+
			'(SELECT price FROM products WHERE id = $1)'+
			') RETURNING *',
			[product_id, resultingOrder.order.id, order.cart[prod_id]],
			function(error, result){
				if(error){
					callback({'error' : 'Error inserting order item in db!'});
					abort_mission = true;
					return rollback(err, client);
				}
				resultingOrder.order.order_items.push(result.rows[0]);
				resultingOrder.order.total_price += result.rows[0].unit_price * result.rows[0].quantity;
			}
		).on('row', (row) => {
			client.query('SELECT name FROM products WHERE id = $1;', [row.product_id], function(error, result2){
				row.name = result2.rows[0].name;
			});
		}).on('end',() => {
			if(abort_mission)
				return;
			//only call callback when all queries finish
			numberOfProducts--;
			if(numberOfProducts <= 0) {
				insertOrder_checkCreditCard(client, order, resultingOrder, callback);
			}
		});
	}
}

function insertOrder_checkCreditCard(client, order, resultingOrder, callback){
	client.query('SELECT name, number, expiration FROM users, creditcards WHERE users.id = $1 AND creditcards.id = users.primary_credit_card;',
		[resultingOrder.order.user_id], function(error, result){
		if(error){
			callback({'error' : 'Error checking credit card in db!'});
			return rollback(err, client);
		}

		resultingOrder.order.user_name = result.rows[0].name;
		resultingOrder.order.credit_card = result.rows[0].number;

		var expiration_parts = result.rows[0].expiration.split('/');
		var month = parseInt(expiration_parts[0]);
		var year = parseInt("20"+expiration_parts[1]);
		var currentTime = new Date()
		var currentMonth = currentTime.getMonth() + 1;
		var currentYear = currentTime.getFullYear()
		var expiredCard = true;
		if(year > currentYear || (year == currentYear && month >= currentMonth))
			expiredCard = false;

		if(expiredCard){
			callback({'blacklist' : true});
			rollback("blacklist", client);
			insertBlacklistedUser(resultingOrder.order.user_id);
			return;
		}

		insertOrder_checkVouchersValidity(client, order, resultingOrder, callback);
		
	});
}

function insertOrder_checkVouchersValidity(client, order, resultingOrder, callback){
	
	var totalNumberOfVouchers = order.vouchers.length;

	if(totalNumberOfVouchers == 0){
		insertOrder_handleValidatedVouchers(client, order, resultingOrder, callback);
		return;
	}

	var pem = rsau.readFile('./keys/public.pem');
	var pubKey = rsa.KEYUTIL.getKey(pem);

	var numberOfVouchersChecked = 0;
	var validated_vouchers = [];

	var abort_mission = false;

	for(var id = 0; id < order.vouchers.length; id++){

		if(abort_mission)
			return;

		//function allows to store "for loop" counter without it changing during assyncronous calls.
		(function(local_id){
			client.query("SELECT * FROM vouchers WHERE serial_id = $1",
					[order.vouchers[local_id].serial_id], function(error, result){
				if(error){
					callback({'error' : 'Error checking voucher validity in db!'});
					return rollback(err, client);
				}

				if(result.rowCount > 0 && result.rows[0].order_id == null){
					// valid voucher


					//check signature
					var signatureHexString = toHexString(order.vouchers[local_id].signature);
					var sig = new rsa.Signature({alg: 'SHA1withRSA'});
					sig.init(pubKey);
					sig.updateString(order.vouchers[local_id].serial_id+"");
					var signVerifyResult = sig.verify(signatureHexString);
					console.warn("sign result: " + signVerifyResult);
					
					if(signVerifyResult){ // valid signature
						// order_id in voucher updated later. see "insertOrder_handleValidatedVouchers"
						validated_vouchers.push(order.vouchers[local_id]);
					}
					else{
						console.warn("INVALID VOUCHER SIGNATURE!! BLACKLIST THIS GUY!!");
						callback({'blacklist' : true});
						rollback("blacklist", client);
						insertBlacklistedUser(resultingOrder.order.user_id);
						//resultingOrder.blacklist = true;
						abort_mission = true;
						return;
					}					
				}
				else {
					// invalid voucher
					console.warn("Ignored voucher " + order.vouchers[local_id].serial_id + " because it was invalid or already used.");
				}

				numberOfVouchersChecked++;

				if(numberOfVouchersChecked == totalNumberOfVouchers){
					order.vouchers = validated_vouchers;
					insertOrder_handleValidatedVouchers(client, order, resultingOrder, callback);
				}
			});
		})(id);
	}
}

function insertOrder_handleValidatedVouchers(client, order, resultingOrder, callback){

	order.number_popcorn_vouchers = 0;
	order.number_coffee_vouchers = 0;
	order.number_discount_vouchers = 0;

	client.query('SELECT * from products WHERE id = $1 OR id = $2', [POPCORN_ID, COFFEE_ID], function(error, result){
		if(error){
			callback({'error' : 'Error getting popcorn/coffee prices from db!'});
			return rollback(err, client);
		}

		var coffee_price = -1, popcorn_price = -1;

		for(var k in result.rows){
			if(result.rows[k].id == POPCORN_ID)
				popcorn_price = result.rows[k].price;
			else if(result.rows[k].id == COFFEE_ID)
				coffee_price = result.rows[k].price;
		}
		/*console.warn('Coffee price: ' + coffee_price);
		console.warn('Popcorn price: ' + popcorn_price);*/

		var validated_vouchers = [];
		for(var i = 0; i < order.vouchers.length; i++){

			if(order.number_discount_vouchers + order.number_popcorn_vouchers + order.number_coffee_vouchers == 3){
				console.warn("Ignored voucher " + order.vouchers[i].serial_id + " because limit was reached.");
				//order.vouchers.splice(i, 1); //delete voucher. limit reached.
				continue;
			}

			switch(order.vouchers[i].type){
				case 'd': {
					if(order.number_discount_vouchers == 1){
						console.warn("Ignored voucher " + order.vouchers[i].serial_id + " because discount limit was reached.");
						order.vouchers.splice(i, 1); //delete voucher. only 1 discount voucher allowed.
					}
					else{
						order.number_discount_vouchers++; 
						validated_vouchers.push(order.vouchers[i]);
						resultingOrder.order.total_price = resultingOrder.order.total_price * 0.95;
						voucherUpdate(order.vouchers[i], resultingOrder.order.id); // no need to block/wait for the query
					}
					break;
				}
				case 'c': {
					if(order.number_of_coffees == order.number_coffee_vouchers){
						console.warn("Ignored voucher " + order.vouchers[i].serial_id + " because no coffees enough.");
						//order.vouchers.splice(id, 1); //delete voucher. only 1 discount voucher allowed.
						break;
					}
					order.number_coffee_vouchers++; 
					validated_vouchers.push(order.vouchers[i]);
					resultingOrder.order.total_price -= coffee_price;
					voucherUpdate(order.vouchers[i], resultingOrder.order.id); // no need to block/wait for the query
					break;
				}
				case 'p': {
					if(order.number_of_popcorns == order.number_popcorn_vouchers){
						console.warn("Ignored voucher " + order.vouchers[i].serial_id + " because no popcorns enough.");
						//order.vouchers.splice(id, 1); //delete voucher. only 1 discount voucher allowed.
						break;
					}
					order.number_popcorn_vouchers++; 
					validated_vouchers.push(order.vouchers[i]);
					resultingOrder.order.total_price -= popcorn_price;
					voucherUpdate(order.vouchers[i], resultingOrder.order.id); // no need to block/wait for the query
					break;
				}
				default: {}//order.vouchers.splice(id, 1); //delete voucher. invalid type. never gonna happen (?)
			}
		}

		resultingOrder.order.vouchers = validated_vouchers;
		resultingOrder.order.total_price = Math.floor(resultingOrder.order.total_price * 100.0)/100.0;
		console.warn("Order inserted. Will send response to terminal.");
		console.warn("RESULTING ORDER:");
		console.warn(resultingOrder);
		//commit transaction
		client.query('COMMIT', client.end.bind(client));
		callback(resultingOrder);

		insertOrder_updateOrderTotal(order, resultingOrder);
		insertOrder_handleOrderTotals(order, resultingOrder);
	});

}

function insertOrder_handleOrderTotals(order, resultingOrder){

	if(resultingOrder.order.total_price >= 20.0){
		issueOfferVoucher(order.user.id);
	}

	var client = openClient();

	client.connect();
	client.query('SELECT updateordertotals($1, $2)', 
		[resultingOrder.order.total_price, order.user.id], function(error, result3){
			if(error != null){
				// n vamos lidar com este erro e ignorar totalmente que não funcionou.
				// de qq forma, é uma operação simples, por isso n deve dar erro. nunca.
				return;
			}

			client.query('SELECT * from users where id = $1',
				[order.user.id], function(error, result4){
					if(error != null){
						// n vamos lidar com este erro e ignorar totalmente que não funcionou.
						// de qq forma, é uma operação simples, por isso n deve dar erro. nunca.
						return;
					}

					//console.log(result4);
					var total_orders_value = result4.rows[0].total_order_value;
					var total_vouchers_issued = result4.rows[0].discount_vouchers_issued;
					var vouchers_to_issue = Math.floor(total_orders_value / 100 ) - total_vouchers_issued;
					
					//console.log("value: " + total_orders_value + " # vouchers: " + total_vouchers_issued);
					//console.log("to issue: "+vouchers_to_issue);

					if(vouchers_to_issue > 0){
						for(var i = 0; i < vouchers_to_issue; i++){
							issueDiscountVoucher(order.user.id);
						}
					}
					client.end();
				}
			);

		}
	);

}

function insertOrder_updateOrderTotal(order, resultingOrder){
	var client = openClient();
	client.connect();
	client.query('UPDATE orders SET total_price = $1 WHERE id = $2', 
		[resultingOrder.order.total_price, resultingOrder.order.id], function(error, result3){
			if(error != null){
				console.warn(error);
				// n vamos lidar com este erro e ignorar totalmente que não funcionou.
				// de qq forma, é uma operação simples, por isso n deve dar erro. nunca.
				return;
			}
			client.end();
		}
	);
}

function voucherUpdate(voucher, order_id){
	var client = openClient();
	client.connect();
	const query = client.query('UPDATE vouchers SET order_id = $1 WHERE serial_id = $2;',
		[order_id, voucher.serial_id], function(error,result) {
			client.end();
		});
}


function issueOfferVoucher(user_id){

	var rand1 = Math.floor(Math.random() * 999 + 1); // 1 to 1000
	var rand2 = Math.floor(Math.random() * 499  + 1); // 1 to 500
	var voucher_serial = rand1 + rand2;

	var voucher_type;
	if( Math.floor(Math.random() * 2) == 1 ) 
		voucher_type = 'p'; // popcorn
	else voucher_type = 'c'; // coffee

	var pem = rsau.readFile('./keys/private.pem');
	var prvKey = rsa.KEYUTIL.getKey(pem);

	var sig = new rsa.Signature({alg: 'SHA1withRSA'});
	sig.init(prvKey);
	sig.updateString(voucher_serial+"");
	var cryp_signature = sig.sign();

	/*console.warn("###############################");
	console.warn(cryp_signature);
	console.warn("###############################");*/

	var client = openClient();
	client.connect();
	const query = client.query(
		"INSERT INTO vouchers (serial_id, type, signature, user_id, order_id) VALUES ($1, $2, decode($3, 'hex'), $4, null)",
		[voucher_serial, voucher_type, cryp_signature, user_id],
		function(error, result){
			if(error != null){
				console.log(error);
				client.end();
				return;
			}
			client.end();
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
	sig.updateString(voucher_serial+"");
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

	console.warn("###############################");
	console.warn(cryp_signature);
	console.warn("###############################");

	var client = openClient();
	client.connect();
	const query = client.query(
		"INSERT INTO vouchers (serial_id, type, signature, user_id, order_id) VALUES ($1, $2, decode($3, 'hex'), $4, null)",
		[voucher_serial, voucher_type, cryp_signature, user_id],
		function(error, result){
			if(error != null){
				console.log(error);
				client.end();
				return;
			}
			client.end();
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
		'order_items.quantity AS quantity, creditcards.number AS credit_card, orders.total_price, '+
		'order_items.unit_price AS unit_price, orders.order_timestamp AS timestamp '+
		'FROM (SELECT * FROM orders WHERE user_id = $1 ORDER BY order_timestamp DESC '+
		'LIMIT $3 OFFSET $2 ) AS orders, order_items, products, creditcards '+
		'WHERE order_items.order_id = orders.id '+
		'AND order_items.product_id = products.id '+
		'AND creditcards.id = orders.credit_card;',
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
    		results[o_id].credit_card = row.credit_card;
    		results[o_id].total_price = row.total_price;
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
		var number_orders_processed = 0;
		var total_number_orders = Object.keys(results).length;
		if(total_number_orders == 0){
			callback(results);
			client.end();
			return;
		}
    	for(var id in results){
    		(function(order_id){
    			client.query('SELECT * FROM vouchers WHERE order_id = $1;',
    				[id], function(error, result){
    					if(error)
    						results[id].vouchers = [];
    					else {
    						results[id].vouchers = result.rows;
    					}

    					if(++number_orders_processed == total_number_orders){
    						callback(results);
    						client.end();
    					}
    			});
    		})(id);
    	}
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
		'AND order_id is null;',
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


////////////////////////////////
///////// BLACKLIST ////////////
////////////////////////////////

function insertBlacklistedUser(user) {
	var client = openClient();
	client.connect();
	const query = client.query('INSERT INTO blacklist (user_id) '+
		'VALUES ($1) RETURNING *;',
		[user], function(error,result) {
			if(error != null) {
				console.log(error);
				return;
			}

			console.log("USER BLACKLISTED: " + user);
		});
}

function insertBlacklistedUsers(user_ids, callback) {
	var client = openClient();
	client.connect();
	
	var inserted_ids = [];
	var completed_queries = 0;

	for(var i = 0; i < user_ids.length; i++){
		client.query('INSERT INTO blacklist (user_id) '+
			'VALUES ($1) RETURNING *;',
			[user_ids[i]], function(error,result) {
				completed_queries++;
				if(!error) {
					completed_queries.push(result.rows[0].user_id);
					console.log("USER BLACKLISTED: " + result.rows[0].user_id);
				}

				if(completed_queries == user_ids.length){
					callback(inserted_ids);
					client.end();
				}
				
		});	
	}	
}

function getBlacklist(callback) {
	var client = openClient();
	client.connect();
	const query = client.query('SELECT * FROM blacklist;',
		function(error,result) {
			if(error != null) {
				callback(null);
				return;
			}

			callback(result.rows);
		});	
}


// TODO TODO TODO
function isUserBlacklisted(user, callback) {
	var client = openClient();
	client.connect();
	const query = client.query('SELECT * FROM blacklist WHERE user_id = $1;',
		function(error,result) {
			if(error != null) {
				callback(null);
			}

			if(result.rows.length > 0)
				callback(true);
			else callback(false);
		});	
}


////////////////////////////////

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

exports.insertBlacklistedUser = insertBlacklistedUser;
exports.insertBlacklistedUsers = insertBlacklistedUsers;
exports.getBlacklist = getBlacklist;



function toHexString(byteArray) {
  return byteArray.map(function(byte) {
    return ('0' + (byte & 0xFF).toString(16)).slice(-2);
  }).join('')
}