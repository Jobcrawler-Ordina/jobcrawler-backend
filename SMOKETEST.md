
# Jobcrawler smoketest
Smoketest for both front end and backend for the JobCrawler application


### Settings for JDK 

Install JDK 11.0.6 (for example, the Oracle version, openJDK should also work).

In project settings: project SDK is 11, Project name is jobcrawler, project language level is 11 - local variable syntax for lambda parameters
Ditto (11 - local variable syntax for lambda parameters) for project modules

Install lombok into the IDE

### Test Backend run 1. Run from IntelliJ, separate Postgres docker file

Test whether the application runs and is visible from the browser. Postgres is present in a separate docker container that must be run.

### Creates the container, runs it and creates a database 'jobcrawler' in it:
#### 1. Start the shell in the source folder of the backend project
docker run --name jobcrawler-postgres -e POSTGRES_PASSWORD=admin -d -p 5432:5432 postgres

docker exec -it jobcrawler-postgres bash

psql -U postgres

create database jobcrawler;

Exit from this database and the container

Start IntelliJ in the backend project, edit application.properties and change spring.datasource-url to 'jdbc:postgresql://localhost:5432/jobcrawler'

Run the application from IntelliJ

What do I see: the application boots without problems and doesn't start scraping

#### 2. Start scraping

Edit the ScraperService.java and uncomment line @ PostConstruct before the scrape() method. Run the application again.

What do I see: application starts and starts scraping

Enter the docker container:

docker exec -it jobcrawler-postgres bash

Log in to Postgres:

psql -U postgres

Connect to the jobcrawler database:

\c jobcrawler

Perform the query to find the vacancies:

select title from vacancy;

What do I see: I should now see a number of vacancies

### Start frontend

#### 1. Start frontend in IntelliJ and in the browser

Open the frontend in IntelliJ

Install required dependencies with the command: npm install

Start the frontend with: npm start

What do I see: the application compiles successfully

When I click in the browser on http://localhost:4200

What do I see: a list of vacancies is shown. The filter column is still missing, because the skills are still empty.

#### 2. Add skills via frontend

http://localhost:4200/getskills

What do I see: a page that allows me to add skills.

Add a skill: Java

What do I see: Java is added to the skills

What do I see: When I enter the container again and the database the skill is present in the skills table.

#### 3. Relink the skills

Click the relink button

What do I see: in the IntelliJ console log it is visible that all vacancies are relinked to the Java skill.

What do I see: in the container, in the database, I can see that the link table now has a number of entries.

Click on "back to Vacancies".
Click on "By skill:", select "Java", and click "Search".
The list of Vacancies should be updated.
