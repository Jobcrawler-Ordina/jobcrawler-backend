![Java CI with Maven](https://github.com/mostafa-221/jobcrawler-backend/workflows/Java%20CI%20with%20Maven/badge.svg)

# Jobcrawler Backend
Backend for the job crawler to find IT vacancies

## Endpoints
All endpoints and their functionality is described in the [endpoint document](docs/ENDPOINTS.md)

## Getting Started

These instructions will get you a copy of the project up and running on your local machine for development and testing purposes. See deployment for notes on how to deploy the project on a live system.

### Prerequisites

What things you need to install the application and how to install them

#### Maven
[Download](https://maven.apache.org/download.cgi) and [install](https://maven.apache.org/install.html) Maven.
Check if Maven is working:
```
mvn --version
```

#### Docker
Install docker as described [here](https://docs.docker.com/install/). If you're installing on Ubuntu or a different linux system, check out [these pages](https://docs.docker.com/install/linux/docker-ce/ubuntu/) for installation.
Check if Docker is working:
```
docker --version
```

### Installing

Install these things to be able to run the application locally:

#### Database
This application uses PostgreSQL as database.
To run the database locally, perform the following commands:

If you haven't created the container before, create and start the container with the "docker run" command. Replace the "\*password\*" part with your own password. This must be the same password as the "DB_PASS" variable declared [here](https://github.com/mostafa-221/jobcrawler-backend/blob/4f238dfdbbcb624aa1bf2215282a9dcc7edd289e/src/main/resources/application.properties#L10).
```
docker run --name jobcrawler-postgres -e POSTGRES_PASSWORD=*password* -d -p 5432:5432 postgres
```

Otherwise, if you did create the container before, run this:
```
docker start jobcrawler-postgres
```

Now navigate into the postgres docker container and open bash:
```
docker exec -it jobcrawler-postgres bash
```

Log into the postgres server with the user "postgres"
```
psql -U postgres
```

Now check if the jobcrawler database is present:
```
\list
```

If the jobcrawler database is not there, create the jobcrawler database:
```
create database jobcrawler;
```

## Running the tests

-- Explain how to run the automated tests for this system --

## Deployment

-- Add additional notes about how to deploy this on a live system --

## Built With

* [Maven](https://maven.apache.org/) - Dependency Management
* [Spring-Boot](https://spring.io/projects/spring-boot) - API framework
* [PostgreSQL](https://www.postgresql.org/) - Database

## Versioning

We use [SemVer](http://semver.org/) for versioning. For the versions available, see the [tags on this repository](https://github.com/mostafa-221/jobcrawler-backend/tags). 

## Authors

* **Adriaan Bakker** - [adriaanbakker-java](https://github.com/adriaanbakker-java)
* **Gijs Overvliet** - [ghyze](https://github.com/ghyze)
* **Mostafa Abd Elrehim** - [mostafa-221](https://github.com/mostafa-221)
* **Timo Koen** - [tkoen93](https://github.com/tkoen93)
* **Jelmer Pijnappel** - [ChocolatePinecone](https://github.com/ChocolatePinecone)

## Acknowledgments

* Hat tip to anyone whose code was used
