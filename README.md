# laa-spring-boot-microservice-template
[![Ministry of Justice Repository Compliance Badge](https://github-community.service.justice.gov.uk/repository-standards/api/laa-spring-boot-microservice-template/badge)](https://github-community.service.justice.gov.uk/repository-standards/laa-spring-boot-microservice-template)


## Overview
  The LAA Record Linking Service is used to link user with SILAS ID to their old CCMS user id 

- [Checkstyle](https://docs.gradle.org/current/userguide/checkstyle_plugin.html)
- [Dependency Management](https://plugins.gradle.org/plugin/io.spring.dependency-management)
- [Jacoco](https://docs.gradle.org/current/userguide/jacoco_plugin.html)
- [Java](https://docs.gradle.org/current/userguide/java_plugin.html)
- [Maven Publish](https://docs.gradle.org/current/userguide/publishing_maven.html)
- [Spring Boot](https://plugins.gradle.org/plugin/org.springframework.boot)
- [Test Logger](https://github.com/radarsh/gradle-test-logger-plugin)
- [Versions](https://github.com/ben-manes/gradle-versions-plugin)

The plugin is provided by -  [laa-ccms-spring-boot-common](https://github.com/ministryofjustice/laa-ccms-spring-boot-common), where you can find
more information regarding setup and usage.

## Running the app locally

### Prerequisites

#### Install Java

Ensure you have installed the correct version of Java on your local machine. At the time of writing this is Java 21.
Some MoJ devices will come prepackaged with the latest version of Java

Check your local version of Java: `java -version`

To check all versions of Java you have installed locally run: `/usr/libexec/java_home -V`

Once you have downloaded Java, you can open your bash profile:

```
sudo nano ~/.bash_profile
```

enter your device password add export the version of Java you wish to use:

```
export JAVA_HOME=$(/usr/libexec/java_home -v 21.0.7)
```

Once exported be sure to source your latest bash profile: `source ~/.bash_profile   `


#### Obtaining an Entra user

- You need a valid **MoJ DEVL External email address** for Entra ID authentication. This email will be used for validation.
    - If you do not have one, reach out to an Entra admin in the #staff-identity-external-authentication-service Slack channel.
    - Use the **`New Admin Account`** button at the bottom of the slack channel


#### Creating a GitHub Token

1. Ensure you have created a classic GitHub Personal Access Token with the following permissions:
    1. repo
    2. write:packages
    3. read:packages
2. The token **must be authorised with (MoJ) SSO**.

#### Creating gradle properties file
1. `cd ~/.gradle`
2. `nano gradle.properties`
3. Add the following parameters

```
project.ext.gitPackageUser = <your GitHub username>
project.ext.gitPackageKey = <your GitHub access token>

```

#### Filling out .env

Using the `.env-template` file as a template, copy to a new .env file
`cp .env-template .env`

Be sure to fill out all values as they are required for pulling dependencies for the application to run

### Starting the Database  
1. Ensure Docker is installed, running & you are signed in with a **licensed** Docker account (see prerequisites above).
2. Navigate to the root of the repository
3. Using the Terminal, run `docker-compose up -d` - this will start the database container using Docker.
4. To create database schema, run `./gradlew flywayMigrate` 
5. To add demo data for local:
   - update V5__insert_demo_data_local.sql to include your email 
   - run `INCLUDE_DEMO_DATA=true ./gradlew flywayMigrate` or set INCLUDE_DEMO_DATA to true in your .env file and run `./gradlew flywayMigrate`

### Build And Run Application
Ensure that all environment variables from `.env` set

`export $(grep -v '^#' .env | xargs)`

Once the environment variables are set, you can run the application. **Remember you must first start the database**

### Build application
`./gradlew clean build`

### Run integration tests
`./gradlew integrationTest`
### Run application
`./gradlew bootRun`

### Run application via Docker
`docker compose up`

## Application Endpoints

### API Documentation

#### Swagger UI
- http://localhost:8080/swagger-ui/index.html
#### API docs (JSON)
- http://localhost:8080/v3/api-docs

### Actuator Endpoints
The following actuator endpoints have been configured:
- http://localhost:8080/actuator
- http://localhost:8080/actuator/health
- http://localhost:8080/actuator/info

## Additional Information

### Libraries Used
- [Spring Boot Actuator](https://docs.spring.io/spring-boot/reference/actuator/index.html) - used to provide various endpoints to help monitor the application, such as view application health and information.
- [Spring Boot Web](https://docs.spring.io/spring-boot/reference/web/index.html) - used to provide features for building the REST API implementation.
- [Spring Data JPA](https://docs.spring.io/spring-data/jpa/reference/jpa.html) - used to simplify database access and interaction, by providing an abstraction over persistence technologies, to help reduce boilerplate code.
- [Springdoc OpenAPI](https://springdoc.org/) - used to generate OpenAPI documentation. It automatically generates Swagger UI, JSON documentation based on your Spring REST APIs.
- [Lombok](https://projectlombok.org/) - used to help to reduce boilerplate Java code by automatically generating common
  methods like getters, setters, constructors etc. at compile-time using annotations.
- [MapStruct](https://mapstruct.org/) - used for object mapping, specifically for converting between different Java object types, such as Data Transfer Objects (DTOs)
  and Entity objects. It generates mapping code at compile code.


