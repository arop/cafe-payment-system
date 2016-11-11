require('dotenv').config();

const 
  bodyParser = require('body-parser'),
  express = require('express'),
  bcrypt = require('./lib/bCrypt.js'),
  db = require('./db/db.js');

var app = express();

/** bodyParser.urlencoded(options)
 * Parses the text as URL encoded data (which is how browsers tend to send form data from regular forms set to POST)
 * and exposes the resulting object (containing the keys and values) on req.body
 */
app.use(bodyParser.urlencoded({
    extended: true
}));

/**bodyParser.json(options)
 * Parses the text as JSON and exposes the resulting object on req.body.
 */
app.use(bodyParser.json());


app.get('/', function (req, res) {
  res.send('Hello World!');
});


app.listen(process.env.PORT || 5000);
console.log('Server running in port ' + (process.env.PORT || 5000));


///////////////////////////////////
/////////    CUSTOMERS    /////////
///////////////////////////////////
//customer registration
// receives: 
// { "user" : { "name" : "joao norim", "email" : "joaonorim@mail.com" , "nif" : "999999999" } }
app.post('/register', function(req, res) {
	//TODO
	if(!req.body.user){
		res.status(404).send('No user info received!');
		return;
	}

	var user = req.body.user;
	console.log(req.body);
	if(!user.name || !user.email || !user.nif)
		res.status(404).send("Missing parameters!");
	else{
		//user.pin = Math.floor(Math.random() * (9999 - 1000) + 1000);
		//TODO uncomment above and comment bellow for random PIN
		user.pin = 1111;
		user.pin = pinTo4Digits(user.pin,4);
		user.hash_pin = bcrypt.hashSync(user.pin);

		user.credit_card_number = user.credit_card_number.replace(/\s/g, '');
		//console.log(user);
		db.insertUser(user, function(result){
			if(result == null){
				res.send({"error" : "Invalid parameters, or already existing email address!"});
			}
			else{
				result.pin = user.pin;
				console.log(result);
				res.send(result);
			}
		});
	}
});

//customer login
// receives
// { "user" : { email: "xxx", pin : xxxx}}
app.post('/login', function(req, res) {
	if(!req.body.user){
		res.status(404).send('No user info received!');
		return;
	}

	var user = req.body.user;
	if(!user.email || !user.pin)
		res.status(404).send("Missing parameters!");
	else{
		db.checkLoginByEmail(user, function(result){
			if(result == null){
				res.send({"error" : "Invalid email or password!"});
			}
			else{
				result = JSON.parse(JSON.stringify(result));
				console.log(result);
				res.send(result);
			}
		});
	}
});


//get menu
app.get('/menu', function(req, res){
	var version = req.query.version; // $_GET["version"]
	console.log(version);
	db.getMenuVersion(function(actual_version){
		if(version == actual_version){
			res.status(204).send('Menu up-to-date!');
		}else{
			db.getMenu(function(menu){
				res.send({"menu" : menu, "version" : actual_version});
			})
		}
	});
	
});



///////////////////////////////////
/////////    VOUCHERS     /////////
///////////////////////////////////

//get user valid vouchers
app.post('/validvouchers', function(req, res) {
	if(!req.body.user || !req.body.user.id || !req.body.user.pin){
		res.status(404).send('No user info received!');
		return;
	}
	var user = req.body.user;
	db.checkLoginByID(user, function(result) {
		if(result == null) {
			res.send({"error" : "Wrong credentials!"});
		} else {
			
			db.getValidVouchers(user,function(vouchers){
				console.log(vouchers);
				if(vouchers == null) {
					res.send({"error" : "Error getting vouchers!"});
				}
				else res.send({"vouchers" : vouchers});
			});
		}
	});
});

//voucher validation
app.post('/voucher', function(req, res) {
	//TODO
});



///////////////////////////////////
/////////  TRANSACTIONS   /////////
///////////////////////////////////

//new transaction
app.post('/transaction', function(req, res){
	
	if(!req.body.order){
		res.status(404).send('No order info received!');
		return;
	}
	if(!req.body.order.cart){
		res.status(404).send('No cart info received!');
		return;
	}
	if(!req.body.order.user || !req.body.order.pin){
		res.status(404).send('No user info received!');
		return;
	}

	var user = {};
	user.id = req.body.order.user;
	user.pin = req.body.order.pin;
	var cart = req.body.order.cart;
	var order = {};
	order.user = user;
	order.cart = JSON.parse(cart);


	if(!req.body.order.vouchers){
		order.vouchers = [];
	}
	else order.vouchers = req.body.order.vouchers;

	order.vouchers = JSON.parse(order.vouchers);

	if(Object.keys(order.cart).length < 1)
		res.status(404).send("Missing parameters!");
	else {
		db.checkLoginByID(user, function(result) {
			if(result == null) {
				res.send({"error" : "Wrong credentials!"});
			} else {

				try{
					db.insertOrder(order, function(result){
						if(result.blacklist){
							res.send({"blacklist" : true});
						}
						else if(result.error){
							res.send({"error" : result.error});
						}
						else{
							res.send(result);
						}
					});
				}
				catch(e){
					res.send({"error" : "Error registing order!"});
					console.warn(e.message);
					console.warn(e.description);
				}
			}
		});
	}
});

//consult transactions
app.post('/pasttransactions', function(req, res){
	if(!req.body.user || !req.body.user.id || !req.body.user.pin){
		res.status(404).send('No user info received!');
		return;
	}
	var user = req.body.user;
	db.checkLoginByID(user, function(result) {
		if(result == null) {
			res.send({"error" : "Wrong credentials!"});
		} else {
			var offset = 0;
			if(req.body.offset) offset = parseInt(req.body.offset);
			console.warn("offset: " + offset);
			db.getPreviousOrders(user,offset*10,10,function(orders){
				console.log(orders);
				if(orders == null) {
					res.send({"error" : "Error getting orders!"});
				}
				else res.send({"orders" : orders});
			});
		}
	});

});



///////////////////////////
//////// CREDIT CARD //////
///////////////////////////
app.post('/credit_card', function (req, res) {
	if(!req.body.user || !req.body.user.id || !req.body.user.pin){
		res.status(404).send('No user info received!');
		return;
	}
	if(!req.body.credit_card || !req.body.credit_card.number 
		|| !req.body.credit_card.cvv || !req.body.credit_card.exp_date){
		res.status(404).send('No credit card info received!');
		return;
	}

	var user = req.body.user;
	db.checkLoginByID(user, function(result) {
		if(result == null) {
			res.send({"error" : "Wrong credentials!"});
		} else {
			var credit_card = req.body.credit_card;
			credit_card.number = credit_card.number.replace(/\s/g, '');

			db.insertCreditCard(user,credit_card,function(credcard){
				if(credcard == null) {
					res.send({"error" : "Error inserting credit card!"});
				}
				else res.send({"credit_card" : credcard});
			});
		}
	});
});

app.post('/primary_credit_card', function (req, res) {
	if(!req.body.user || !req.body.user.id || !req.body.user.pin){
		res.status(404).send('No user info received!');
		return;
	}
	if(!req.body.credit_card || !req.body.credit_card.id){
		res.status(404).send('No credit card info received!');
		return;
	}
	var user = req.body.user;
	db.checkLoginByID(user, function(result) {
		if(result == null) {
			res.send({"error" : "Wrong credentials!"});
		} else {
			var credit_card = req.body.credit_card;

			db.setPrimaryCreditCard(user,credit_card,function(resultUser){
				if(resultUser == null) {
					res.send({"error" : "Error updating primary credit card!"});
				}
				else res.send({"user" : resultUser});
			});
		}
	});
});


///////////////////////////
//////// BLACKLIST ////////
///////////////////////////
app.get('/blacklist', function (req, res) {
	db.getBlacklist(function(result) {
		if(result == null) {
			res.send({"error" : "Error getting blacklist!"});
		} else {
			res.send({"blacklist" : result});
		}
	});
});

app.post('/blacklist', function (req, res) {
	if(!req.body.blacklist){
		res.status(404).send('No blacklist received!');
		return;
	}
	try{
		db.insertBlacklistedUsers(req.body.blacklist, function(result){
			res.send({"inserted": result});
		});

	}
	catch(e){
		res.send({'error' : 'Problem inserting blacklist!'});
	}
});

///////////////////////////
//////// OTHER ////////////
///////////////////////////
function pinTo4Digits(num, size) {
    var s = "000000000" + num;
    return s.substr(s.length-size);
}

