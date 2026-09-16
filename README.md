# Gym System

Gym System is a Spring Boot backend application developed as part of the EPAM Java Lab. It provides trainee, trainer, and training management while demonstrating REST API development, security, persistence, asynchronous messaging, microservices, NoSQL storage, and containerized deployment.

The project consists of two Spring Boot services connected through ActiveMQ:

* **Gym Service** — manages users, trainees, trainers, authentication, and training operations.
* **Training Aggregator** — consumes trainer workload events and maintains aggregated training statistics in MongoDB.

## Architecture

```text
                          ┌─────────────────────┐
                          │      Client         │
                          └──────────┬──────────┘
                                     │
                                     │ REST
                                     ▼
                          ┌─────────────────────┐
                          │     Gym Service     │
                          │     Port 8080       │
                          └───────┬───────┬─────┘
                                  │       │
                         JPA      │       │ JMS
                                  │       │
                                  ▼       ▼
                         ┌────────────┐  ┌────────────┐
                         │ PostgreSQL │  │  ActiveMQ  │
                         └────────────┘  └──────┬─────┘
                                               │
                                               │ JMS
                                               ▼
                                    ┌─────────────────────┐
                                    │ Training Aggregator │
                                    │      Port 8081      │
                                    └──────────┬──────────┘
                                               │
                                               ▼
                                         ┌─────────┐
                                         │ MongoDB │
                                         └─────────┘
```

## Main Features

### Gym Service

* Trainee registration and management
* Trainer registration and management
* Training creation and retrieval
* Trainer assignment
* Training type management
* PostgreSQL persistence using JPA/Hibernate
* BCrypt password hashing
* JWT-based authentication
* Role-based authorization
* Login brute-force protection
* Token revocation on logout
* Centralized exception handling
* Transaction ID logging
* Spring Boot Actuator monitoring
* Application metrics

### Training Aggregator

* Receives trainer workload events through ActiveMQ
* Processes `ADD` and `DELETE` training events
* Aggregates trainer workload by year and month
* Stores workload summaries in MongoDB
* Maintains trainer identity and active status information
* Uses asynchronous JMS messaging between services

## Technology Stack

### Backend

* Java 21
* Spring Boot 3
* Spring MVC
* Spring Security
* Spring Data JPA
* Spring Data MongoDB
* Hibernate
* Jakarta Persistence
* Maven

### Databases

* PostgreSQL
* MongoDB

### Messaging

* Apache ActiveMQ
* Spring JMS

### Security

* Spring Security
* JWT authentication
* RSA public/private key signing
* BCrypt password hashing
* Role-based authorization

### Infrastructure

* Docker
* Docker Compose

### Testing

* JUnit 5
* Mockito
* Spring Boot Test
* Testcontainers

## Repository Structure

```text
.
├── gym/
│   ├── Dockerfile
│   ├── pom.xml
│   └── src/
│
├── training-aggregator/
│   ├── Dockerfile
│   ├── pom.xml
│   └── src/
│
├── messaging-test/
│   ├── pom.xml
│   └── src/
│
├── docker-compose.yml
└── README.md
```

## Running the Application

### Requirements

The easiest way to run the complete system is with Docker.

Required:

* Docker
* Docker Compose

No local PostgreSQL, MongoDB, or ActiveMQ installation is required.

### Start the complete stack

From the repository root:

```bash
docker compose up -d --build
```

Docker Compose starts:

* PostgreSQL
* MongoDB
* ActiveMQ
* Gym Service
* Training Aggregator

Check the running containers:

```bash
docker compose ps
```

A successfully started environment should contain:

```text
activemq
postgres
mongodb
gym
training-aggregator
```

PostgreSQL and MongoDB should report a healthy status.

## Service Ports

| Service              |    Port |
| -------------------- | ------: |
| Gym API              |  `8080` |
| Training Aggregator  |  `8081` |
| ActiveMQ Broker      | `61616` |
| ActiveMQ Web Console |  `8161` |

The databases are available internally through the Docker Compose network and are not exposed to host ports.

Services communicate using Docker service names:

```text
Gym → postgres:5432
Gym → activemq:61616

Training Aggregator → mongodb:27017
Training Aggregator → activemq:61616
```

## Health Check

The Gym service exposes Spring Boot Actuator endpoints.

Example:

```bash
curl http://localhost:8080/actuator/health
```

A healthy application should return a response containing:

```json
{
  "status": "UP"
}
```

Additional Actuator endpoints include:

```text
/actuator/health
/actuator/info
/actuator/metrics
/actuator/prometheus
```

## Messaging Flow

When a training affects trainer workload, the Gym service publishes an event to ActiveMQ.

```text
Training operation
        │
        ▼
Gym Service
        │
        │ TrainerWorkloadEvent
        ▼
ActiveMQ
        │
        ▼
Training Aggregator
        │
        ▼
MongoDB
```

A workload event contains information such as:

```text
trainerUsername
firstName
lastName
isActive
trainingDate
trainingDuration
actionType
```

`actionType` indicates whether the workload should be added or removed.

The aggregator stores trainer workload in a nested structure based on years and months.

## Configuration

The Gym service uses a local Spring profile for local-development configuration:

```text
application.yml
application-local.yml
```

When running through Docker Compose, the profile is activated through:

```text
SPRING_PROFILES_ACTIVE=local
```

Docker Compose overrides infrastructure addresses using environment variables.

For example:

```text
SPRING_DATASOURCE_URL=jdbc:postgresql://postgres:5432/gym_db

SPRING_ACTIVEMQ_BROKER_URL=tcp://activemq:61616
```

The Training Aggregator similarly receives its MongoDB connection through:

```text
SPRING_DATA_MONGODB_URI=mongodb://mongodb:27017/gym_db
```

This allows the same application code to run locally or inside containers without hard-coding Docker-specific addresses.

## Security

The Gym API uses stateless JWT authentication.

Authentication flow:

```text
Credentials
    │
    ▼
AuthenticationManager
    │
    ▼
JWT issued
    │
    ▼
Client sends Bearer token
    │
    ▼
Spring Security validates JWT
```

JWTs are signed using RSA keys.

Authorization is role-based, primarily using:

```text
TRAINEE
TRAINER
```

Selected public endpoints include registration, login, and application health checks.

Other endpoints require authentication and the appropriate role.

## Stopping the Application

Stop all containers:

```bash
docker compose down
```

To also remove persistent database volumes:

```bash
docker compose down -v
```

The `-v` option deletes PostgreSQL and MongoDB data stored in Docker volumes.

## Project Purpose

The project was developed to practice and demonstrate backend engineering concepts including:

* layered Spring application architecture
* REST API development
* relational persistence
* NoSQL persistence
* authentication and authorization
* asynchronous communication
* microservice interaction
* health monitoring and metrics
* automated testing
* Docker containerization
* multi-service orchestration with Docker Compose
