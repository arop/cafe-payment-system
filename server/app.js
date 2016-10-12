
var express = require('express');

var app = express();

app.get('/', function (req, res) {
  res.send('Hello World!');
});


app.listen(process.env.PORT || 5000);
console.log('Server running in port ' + (process.env.PORT || 5000));



///////////////////////////////////
/////////    CUSTOMERS    /////////
///////////////////////////////////
//customer registration
app.post('/register', function(req, res) {
	//TODO
});


//customer validation
app.post('/login', function(req, res) {
	//TODO
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