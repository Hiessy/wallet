Alias Service – Manages user creation and authentication.

Microservice	Responsibilities
User Service	- Create and manage users
- TODO Handle authentication (JWT, OAuth2)
- Maintain user profile data

# Alias Management Application

This is a Spring Boot application for managing aliases. It allows you to create aliases with unique names and passwords. The application also publishes events to a Kafka topic when a new alias is created.

## Features

- **Create Alias**: Create a new alias with a unique name and password.
- **Kafka Integration**: Publishes an event to a Kafka topic when an alias is created.
- **Database Storage**: Stores aliases in a PostgreSQL database.
- **Validation**: Ensures alias names are unique and non-blank.

## Technologies Used

- **Spring Boot**: Backend framework.
- **PostgreSQL**: Relational database for storing aliases.
- **Kafka**: Message broker for publishing events.
- **Testcontainers**: For integration testing with real dependencies (PostgreSQL and Kafka).

---

## Getting Started

### Prerequisites

- Java 17 or higher
- Docker (for running PostgreSQL and Kafka locally)
- Maven (for building the project)

---

### Running the Application

1. **Clone the Repository**:
   ```bash
       git clone https://github.com/your-repo/alias-management-app.git
       cd alias-management-app
    ```
2. **Start Docker Containers**:
   ```bash
        docker-compose up -d
    ```
3. Build and Run the Application:
   ```bash
    mvn clean install
    mvn spring-boot:run
    ```
4. Access the Application:

    The application will be running at http://localhost:8080.

API Endpoints
Create Alias

Endpoint: POST /api/aliases

Request Body:
   ```json
    {
      "name": "JohnDoe123",
      "password": "password"
    }
   ```
Response:

Success: 204 No Content

Error:

400 Bad Request if the alias name is duplicate or invalid.

500 Internal Server Error for other errors.

Kafka Integration

When an alias is created, the application publishes an event to the alias-created-topic Kafka topic. The event contains the alias name and ID.
Example Kafka Event
```json
{
"name": "JohnDoe123",
"id": 1
}
```

Testing
Unit Tests

Run unit tests using:
```bash
mvn test
```
Integration Tests

Integration tests use Testcontainers to spin up real PostgreSQL and Kafka containers. Run them using:
```bash
mvn verify
```

Example Requests
Create Alias
```bash

curl -X POST http://localhost:8080/api/aliases \
-H "Content-Type: application/json" \
-d '{
"name": "JohnDoe123",
"password": "password"
}'
```
Response

Success: 204 No Content

Error:
```json
    {
      "error": "Validation error",
      "message": "Alias name already exists"
    }
```


# Start a producer
```bash
docker exec -it <kafka-container-id> kafka-console-producer --broker-list localhost:9092 --topic test-topic
```
# Start a consumer
```bash
docker exec -it kafka_container /opt/bitnami/kafka/bin/kafka-console-consumer.sh --bootstrap-server localhost:9092 --topic alias-created-topic --from-beginning
```

Contributing

Contributions are welcome! Please follow these steps:

Fork the repository.

Create a new branch (git checkout -b feature/your-feature).

Commit your changes (git commit -m 'Add some feature').

Push to the branch (git push origin feature/your-feature).

Open a pull request.

License

This project is licensed under the MIT License. See the LICENSE file for details.
Contact

For questions or feedback, please contact:

Your Name: your.email@example.com

GitHub: your-github-profile

