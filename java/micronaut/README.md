# Micronaut & Java Demo with MySQL Document Store

A demo for using Micronaut to access a MySQL Document Store

## Setup

This setup assumes you already have access to a MySQL database.

* Copy the file `src/main/resources/application.yml.template` to `src/main/resources/application.yml`
  * Change the values to match your database information
* Run the command ` ./gradlew run` in the main directory
* In a browser window, or at tool such as Postman, make a `GET` request to [http://localhost:8080/](http://localhost:8080/)

  * You should see the following result: `{"message":"Micronaut/Java Demo main endpoint"}`