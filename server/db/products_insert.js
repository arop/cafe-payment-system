require('dotenv').config();

var pgp = require('pg-promise')();
var cn = {
    user: process.env.DATABASE_USER,
    password: process.env.DATABASE_PASSWORD,
    database: process.env.DATABASE_SERVER,
    port: process.env.DATABASE_PORT,
    host: process.env.DATABASE_HOST,
    ssl: true
};

var db = pgp(cn);


///////////////////////////////////////////////////////////////////////////////////////////
//SRC: http://stackoverflow.com/questions/34990186/how-do-i-properly-insert-multiple-rows-into-pg-with-node-postgres
// Concatenates an array of objects or arrays of values, according to the template,
// to use with insert queries. Can be used either as a class type or as a function.
//
// template = formatting template string
// data = array of either objects or arrays of values
function Inserts(template, data) {
    if (!(this instanceof Inserts)) {
        return new Inserts(template, data);
    }
    this._rawDBType = true;
    this.formatDBType = function () {
        return data.map(d=>'(' + pgp.as.format(template, d) + ')').join(',');
    };
}
///////////////////////////////////////////////////////////////////////////////////////////


//client.connect();

var products = [['Popcorn', 4.90], ['Coffee', 1.00], ['Coke', 1.50], ['Waffle', 3.00], 
	['Toast', 2.50], ['Water', 1.00], ['Orange Juice', 2.00], ['Croissant', 2.00],
	['Tea', 2.00], ['Chips', 2.00], ['Muffin', 2.00], ['Chocolate', 1.30]];
db.none('INSERT INTO products(name, price) VALUES $1', Inserts('$1, $2', products))
    .then(data=> {
        // OK, all records have been inserted
        console.log('All products inserted!!');
    })
    .catch(error=> {
        // Error, no records inserted
        console.log('ERROR inserting products!!');
    });