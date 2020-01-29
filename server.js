
// Built-in Node.js modules
let path = require('path');
let bodyParser = require('body-parser'); // For parsing params in requests

// NPM modules
let express = require('express');
let sqlite3 = require('sqlite3');
let cors = require('cors');

//let public_dir = path.join(__dirname, 'public'); probably will need this from part 2
let db_filename = path.join(__dirname, 'db.db');

let app = express();
app.use(bodyParser.urlencoded({extended: true})); // allows us to get PUT request body
app.use(cors());

// let port = 8040; //parseInt(process.argv[2]);
const PORT = process.env.PORT || 5000;

// open stpaul_crime.sqlite3 database for reading and writing
let db = new sqlite3.Database(db_filename, sqlite3.OPEN_READWRITE, err => {
    if (err) {
        console.log("Error opening db");
    } else {
        console.log("Now connected to db");
    }
});

app.get('/', (req, res) => {
    respondWithData(res, 'text/plain', 'App online')
});

app.get('/items', (req, res) => {
    let sqlQuery = 'SELECT * FROM items';
    queryDatabase(sqlQuery)
    .then(rows => {
        respondWithData(res, 'application/json', JSON.stringify(rows));
    }).catch(err => {
        respondWithError(res);
    });
});

// Helper function for query database using provided SQL query
function queryDatabase(sqlQuery){
    // Creates and returns promise that will resolve when rows are received
    return new Promise( (resolve, reject) => {
        db.all(sqlQuery, (err, rows) => {
            if (err) {
                console.log(err);
                reject();
            } else {
                resolve(rows);
            }
        });
    });
}

function respondWithData(res, contentType, response){
    res.writeHead(200, {'Content-Type': contentType});
    res.write(response);
    res.end();
}

function respondWithError(res){
    res.writeHead(500, {'Content-Type': 'text/plain'});
    res.write('Error while querying database');
    res.end();
}

app.put("/new-item", (req, res) => {
    let title = req.body.title;
    try {
        db.run("INSERT INTO items (title) VALUES (?)", [title]);
        res.writeHead(200);
        res.write('item added to db');
        console.log('added new item');
    } catch {
        res.writeHead(500);
        console.log('error adding item');
        res.write('Error while inserting into db');
    }
    res.end();
});

app.delete("/delete-item", (req, res) => {
    let title = req.body.id;
    try {
        db.run("DELETE FROM items WHERE id = (?)", [id]);
        res.writeHead(200);
        res.write('item deleted from db');
        console.log('deleted item');
    } catch {
        res.writeHead(500);
        console.log('error deleting item');
        res.write('Error while deleting item from db');
    }
    res.end();
});

console.log('Listening on port ' + PORT);
let server = app.listen(PORT);
