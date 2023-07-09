# Hungry Groot Food Tracking Application [ DEMO STARTER APP ]

[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](https://github.com/dhvakr/Atm-Machine-Code/blob/main/LICENSE)
[![GitHub issues](https://img.shields.io/github/issues/dhvakr/Atm-Machine-Code)](https://github.com/dhvakr/HungryGroot/issues)
![JDK](https://img.shields.io/badge/JDK-%3E%3D%20v17-blue)
[![No Maintenance Intended](http://unmaintained.tech/badge.svg)](http://unmaintained.tech/)

> The users who collectively registered for this application are referred to in the application by the term "groot." Groot is merely an individual Marvel fan. Groot

## Prerequisite :
This project uses Java 17, Spring Boot 3.0.6 and Node v20.0.0 - If your intent to run this application locally make sure you have a right SDK's

## Project structure

- `ApplicationLayout.java` in `src/main/java/me/dhvakr` contains the navigation setup (i.e., the side/top bar and the main menu). This setup uses [App Layout](https://vaadin.com/docs/components/app-layout).
- `ui` package in `src/main/java/me/dhvakr` contains the server-side Java views of the application.
- `views` folder in `frontend/` contains the client-side JavaScript views of the application.
- `themes` folder in `frontend/` contains the custom CSS styles.

## Running the application Locally

Simple run this application with required database credentials, importantly create a database called `hungry_groot` or anything with your name of choice

#### ENV DETAILS TO RUN: 

| NAME                  | VALUE                                         |
|-----------------------|-----------------------------------------------|
| DATASOURCE_URL        | jdbc:postgresql://localhost:5432/hungry_groot |
| DATASOURCE_USERNAME   | YOUR_DATABASE_USERNAME                        |
| DATASOURCE_PASSWORD   | YOUR_DATABASE_PASSWORD                        |

After successfully running the application, look for (http://localhost:8045 in your browser.)

## Some Points to Note

- By Default the admin credentials is `hgAdmin/hgAdmin`
- Once application starts it can be found in `http://localhost:8045/`
- To register new account the application requires some random `dhvakr.me` domain at end to register the account
- The application of tracking food is schedules to open dropdown by 5 to 7 for dinner, It can be configured by your choice in `src/main/java/me/dhvakr/config/RecurringCheckBox` - Currently Time configure is Hardcoded
- This application is already build out the production ready jar of vaadin to build docker images, Jar can be found in libs folder
- 
## Create a Production Build

To create a production build, call `mvnw clean package -Pproduction` (Windows),
or `./mvnw clean package -Pproduction` (Mac & Linux).
This will build a JAR file with all the dependencies with front-end and backend resources, 
ready to be deployed. The file can be found in the `target` folder after the build completes.

## Running the application using Docker-Compose [ Recommended ]

For this, create a production build to generate a Jar 

```shell
mvn clean package -Pproduction
```

Then build the Dockerized version of this project, run
```
docker build . -t hg:4.5.0
```
Once the Docker image is correctly built, you can run it locally using

```
docker-compose up
```