require('dotenv').config()

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
		db.insertUser(user, function(result){
			if(result == null){
				res.send({"error" : "Invalid parameters, or already existing email address!"});
			}
			else{
				result.hash_pin = undefined;
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
		user.hash_pin = bcrypt.hashSync(user.pin);
		db.checkLogin(user, function(result){
			if(result == null){
				res.send({"error" : "Invalid email or password!"});
			}
			else{
				res.send(result);
			}
		});
	}
});


//get menu
app.get('/menu', function(req, res){
	db.getMenu(function(menu){
		console.log(menu);
		res.send({"menu" : menu});
	})
});



///////////////////////////////////
/////////    VOUCHERS     /////////
///////////////////////////////////

//voucher emition
app.get('/voucher', function(req, res) {
	//TODO
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
	//TODO
});

//consult transactions
app.get('/transaction', function(req, res){
	//TODO
});



///////////////////////////
//////// OTHER ////////////
///////////////////////////
function pinTo4Digits(num, size) {
    var s = "000000000" + num;
    return s.substr(s.length-size);
}
