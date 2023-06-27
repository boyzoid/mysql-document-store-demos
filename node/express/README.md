# Node.js Demo with MySQL Document Store

A demo for using Node.js to access a MySQL Document Store

## Setup

This setup assumes you already have [Node.js](https://nodejs.org/en/download/) and have access to a MySQL database.

* In the project root directory, copy the `.env_template` file to `.env` and fill in the values for the port Node will listen on and the database information.
* From a command prompt, run `npm install` in the project root directory.
* From a command prompt, run `node src` in the project root directory.
* In a browser window, or at tool such as Postman, make a `GET` request to [http://localhost:3001/](http://localhost:3001/)

  * You should see the following result: `{"message":"Node Demo main endpoint"}`
