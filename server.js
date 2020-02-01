
// Built-in Node.js modules
let path = require('path');
let bodyParser = require('body-parser'); // For parsing params in requests

// NPM modules
let express = require('express');
let enableWs = require('express-ws');
let sqlite3 = require('sqlite3');
let cors = require('cors');

//let public_dir = path.join(__dirname, 'public'); probably will need this from part 2
let db_filename = path.join(__dirname, 'db.db');

let app = express();
app.use(bodyParser.urlencoded({extended: true})); // allows us to get PUT request body
app.use(cors());
let expressWs = enableWs(app);

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

app.all('/', (req, res) => {
    respondWithData(res, 'text/plain', 'App online')
});

// app.get('/items', (req, res) => {
//     let sqlQuery = 'SELECT * FROM items WHERE active = TRUE';
//     queryDatabase(sqlQuery)
//     .then(rows => {
//         respondWithData(res, 'application/json', JSON.stringify(rows));
//         console.log('serving items');
//     }).catch(err => {
//         respondWithError(res);
//         console.log('error serving items');
//     });
// });

app.ws('/items', (ws, req) => {
    ws.on('message', function(msg) {
        if(msg === 'Give me items please.'){
            getItemsFromDatabase()
                .then(rows => {
                    ws.send(JSON.stringify(rows));
                    console.log('new connection');
                }).catch(err => {
                    console.log(err);
                });
        }
    });
});

function broadcast(){
    getItemsFromDatabase()
        .then(rows => {
            rows = JSON.stringify(rows);
            let wss = expressWs.getWss('/items');
            console.log('db updated, serving items');
            wss.clients.forEach(client => {
                client.send(rows);
            });
        }).catch(err => {
            console.log(err);
        });
}

function getItemsFromDatabase(){
    let sqlQuery = 'SELECT * FROM items WHERE active = TRUE';
    return queryDatabase(sqlQuery);
    // .then(rows => {
    //     respondWithData(res, 'application/json', JSON.stringify(rows));
    //     console.log('serving items');
    // }).catch(err => {
    //     respondWithError(res);
    //     console.log('error serving items');
    // });
}

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

// function respondWithData(res, contentType, response){
//     res.writeHead(200, {'Content-Type': contentType});
//     res.write(response);
//     res.end();
// }
//
// function respondWithError(res){
//     res.writeHead(500, {'Content-Type': 'text/plain'});
//     res.write('Error while querying database');
//     res.end();
// }

app.put("/items", (req, res) => {
    let title = req.body.title;
    let color = req.body.color;
    try {
        db.run("INSERT INTO items (title, active, color) VALUES (?, ?, ?)", [title, true, color]);
        res.writeHead(200);
        res.write('item added to db');
        console.log('added new item');
    } catch {
        res.writeHead(500);
        console.log('error adding item');
        res.write('Error while inserting into db');
    }
    res.end();
    broadcast();
});

app.delete("/items", (req, res) => {
    let id = req.body.id;
    try {
        db.run("UPDATE items SET active = 0 WHERE id = (?)", [id]);
        res.writeHead(200);
        res.write('item deleted from db');
        console.log('deleted item');
    } catch {
        res.writeHead(500);
        console.log('error deleting item');
        res.write('Error while deleting item from db');
    }
    res.end();
    broadcast();
});

console.log('Listening on port ' + PORT);
let server = app.listen(PORT);