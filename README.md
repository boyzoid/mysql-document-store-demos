# MySQL Document Store Demos

This repo contains MySQL Document Store demos for different languages supported by the X-Plugin.

Each demo is designed to stand on its own and provides a simple API to show how to use the X DevAPI to interact with MySQL Document Store.

The individual demos do not interact with each other.

Included in this repo are:

* [Node.Js using Express](node/express)
* [Java using Micronaut](java/micronaut/)

To set up the demo database, you will need to have [MySQL Shell](https://dev.mysql.com/doc/mysql-shell/8.0/en/) installed.

* Use MySQL Shell to connect to your database server.
* Run the command `util.loadDump("{absolute path to project}/data/scores_dump}")`

Be sure to follow any instructions in the README for each demo.
