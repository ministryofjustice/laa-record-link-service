# laa-spring-boot-microservice-template
[![Ministry of Justice Repository Compliance Badge](https://github-community.service.justice.gov.uk/repository-standards/api/laa-spring-boot-microservice-template/badge)](https://github-community.service.justice.gov.uk/repository-standards/laa-spring-boot-microservice-template)

### ⚠️ WORK IN PROGRESS ⚠️
This template is still under development and features may be added or subject to change.

## Overview
  The Laa Record Linking Service is used to link user with LASSIE id to their old CCMS user id 

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


## Setup Instructions
Once you've created your repository using this template, perform the following steps:

### Update README
Edit this `README.md` file to document your project accurately. Take the time to create a clear, engaging, and informative`README.md` file. Include information such as what your project does, how to install and run it, how to contribute, and any other pertinent details.

### Add Branch Protection rules
Ensure branch protection is set up on the `main` branch.

### Configure Dependabot
Change all `uk.gov.laa.springboot.microservice.*` package references to `uk.gov.laa.{application-package-name}.*`.

Uncomment the `registries` section and follow the instructions in the comments.

### Add Repository To Snyk
Ensure that your repository has been added to the [Legal Aid Agency Snyk](https://app.snyk.io/org/legal-aid-agency) organisation.

Also add `SNYK_TOKEN` as a repository secret.

### Update Project Files
<details>

<summary>Click here for more details on which files to update.</summary>

#### 1. Rename subproject directories
Ensure to rename `spring-boot-microservice-api` and `spring-boot-microservice-service` directories to your application name:
`{application-name}-api` and `{application-name}-service`.

Update `settings.gradle` as follows:
```
rootProject.name = '{repository-name}'

include '{application-name}-api'
include '{application-name}-service'
```

Update `build.gradle` in the project root directory as follows:
```
subprojects {
    group = 'uk.gov.justice.laa.{application-name}'
}
```

#### 2. Update api subproject
Update the following files found in the `{application-name}-api` directory:

- `open-api-specification.yml` - replace the contents of this file with the API specification for your application.
- `build.gradle` - replace all references to `spring-boot-microservice-api` with `{service-name}-api`.

#### 3. Update service subproject

Rename the package name/directory - `uk.gov.justice.laa.springboot.microservice` to `uk.gov.justice.laa.{application-package-name}`
under `src/integrationTest/java`, `src/main/java`, `src/test/java`.

Update the following properties in `src/main/resources/application.yml` with your application details:
`spring.application.name`, `info.app.name`, `info.app.description`

#### 4. Update Dockerfile
Rename the `laa-spring-boot-microservice` directory and jar file name to  `laa-{application-name}`.

#### 5. Update GitHub workflow
The following workflows have been provided:

* Build and test PR - `build-test-pr.yml`
* Build and deploy after PR merged - `pr-merge-main.yml` 

In the above workflow files, change all occurrences of the `spring-boot-microservice-service/build/` build path to `{application-name}-service/build/`.

</details>

### Database scripts
The *.sql scripts in  `src/main/resources` have been included to provide an example database for demonstration purposes only and should be removed for your application.

## Build And Run Application
Ensure that all environment variables from `.env` set

`export $(grep -v '^#' .env | xargs)`

Once the environment variables are set, you can run must first start the database:

### Starting the Database  
1. Ensure Docker is installed, running & you are signed in with a **licensed** Docker account (see prerequisites above).
2. Navigate to the root of the repository
3. Using the Terminal, run `docker-compose up -d` - this will start the database container using Docker.
4. To create database schema, run `./gradlew flywayMigrate` 
5. To add demo data for local:
   - update V3__insert_demo_data_local.sql to include your email 
   - run `INCLUDE_DEMO_DATA=true ./gradlew flywayMigrate` or set INCLUDE_DEMO_DATA to true in your .env file and run `./gradlew flywayMigrate`

### Build application
`./gradlew clean build`

### Run integration tests
`./gradlew integrationTest`
### Starting Databse
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
- [H2](https://www.h2database.com/html/main.html) - used to provide an example database and should not be used in production.


