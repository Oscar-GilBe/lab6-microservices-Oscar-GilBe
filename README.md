# Web Engineering 2025-2026 / Lab 6: Microservices

A Spring Boot + Spring Cloud starter demonstrating microservices architecture with service discovery (Eureka) and centralized configuration. Complete the tasks in `docs/GUIDE.md` to verify and demonstrate the system's resilience.

## Tech stack

- Spring Boot 3.5.3
- Spring Cloud 2025.0.0 (Northfields)
- Netflix Eureka (Service Discovery)
- Spring Cloud Config (Centralized Configuration)
- Kotlin 2.2.10
- Java 21 (toolchain)
- Gradle 8.5

## Prerequisites

- Java 21
- Git

## Quick start

Start services in separate terminals (in order):

```bash
# Terminal 1: Discovery Server (Eureka)
./gradlew discovery:bootRun

# Terminal 2: Config Server
./gradlew config:bootRun

# Terminal 3: Accounts Service
./gradlew accounts:bootRun

# Terminal 4: Web Service
./gradlew web:bootRun
```

Access points:
- Eureka Dashboard: http://localhost:8761
- Web Application: http://localhost:4444
- Accounts API: http://localhost:3333

## Project structure

- `discovery/`: Eureka server for service registration and discovery (port 8761)
- `config/`: Spring Cloud Config server for centralized configuration (port 8762)
  - `config/config/`: Configuration files for all services (native backend)
- `accounts/`: RESTful accounts service with in-memory database (port 3333)
- `web/`: MVC frontend consuming the accounts service (port 4444)
- `docs/GUIDE.md`: Assignment instructions with detailed guidance

## Assignment overview

This lab teaches microservices architecture through hands-on experimentation:

1. **Verify**: Confirm services register with Eureka
2. **Configure**: Modify service configurations via Config Server
3. **Scale**: Run multiple service instances
4. **Test Resilience**: Observe behavior during service failures and recovery

See `docs/GUIDE.md` for detailed instructions.

## Learning objectives

- Understand microservices architecture patterns
- Apply service discovery with Netflix Eureka
- Use centralized configuration with Spring Cloud Config
- Observe system resilience and self-healing behavior

## Code quality and formatting

```bash
./gradlew ktlintFormat ktlintCheck
```

## Bonus opportunities

Complete **at least one** of the following tasks to earn a bonus (first submissions evaluated first):

### 1. **Circuit Breaker Pattern** ✅

- **Description**: Implement a circuit breaker for requests from the web service to the accounts service.
- **Implementation**: Use [Spring Cloud Circuit Breaker](https://spring.io/guides/gs/cloud-circuit-breaker/) with Resilience4j to prevent cascading failures.
- **Goal**: Provide graceful degradation with fallback responses when the accounts service is unavailable.
- **Benefit**: Demonstrates resilience patterns in distributed systems.

### 2. **API Gateway**

- **Description**: Add Spring Cloud Gateway as a single entry point for all client requests.
- **Implementation**: Route requests to services, implement rate limiting, and add request/response logging.
- **Goal**: Centralize cross-cutting concerns and provide unified API access.
- **Benefit**: Shows understanding of edge service patterns in microservices architecture.

### 3. **Distributed Tracing**

- **Description**: Implement end-to-end request tracing across all services.
- **Implementation**: Use Micrometer Tracing with Zipkin to visualize request flows through multiple services.
- **Goal**: Enable debugging of distributed transactions and performance bottlenecks.
- **Benefit**: Demonstrates observability patterns in distributed systems.

### 4. **Dynamic Configuration Refresh**

- **Description**: Enable configuration updates without service restarts.
- **Implementation**: Use Spring Cloud Bus with RabbitMQ or Kafka to broadcast configuration changes to all service instances.
- **Goal**: Update configuration in running services via `/actuator/refresh` or message bus.
- **Benefit**: Shows understanding of operational agility in microservices.

### 5. **Persistent Database**

- **Description**: Replace in-memory H2 database with PostgreSQL or MySQL.
- **Implementation**: Configure JPA with a persistent database, use Docker for database deployment, and manage schema migrations with Flyway or Liquibase.
- **Goal**: Enable data persistence across service restarts.
- **Benefit**: Demonstrates production-ready data management patterns.

### 6. **RESTful API Documentation**

- **Description**: Generate interactive API documentation for all services.
- **Implementation**: Use SpringDoc OpenAPI 3 to auto-generate API documentation with Swagger UI.
- **Goal**: Provide browsable, testable API documentation at `/swagger-ui.html`.
- **Benefit**: Shows understanding of API documentation best practices.

### 7. **Security with OAuth2/JWT**

- **Description**: Implement authentication and authorization across microservices.
- **Implementation**: Use Spring Security with OAuth2 Resource Server and JWT tokens. Add an authorization server or use Keycloak.
- **Goal**: Secure service-to-service and client-to-service communication.
- **Benefit**: Demonstrates security patterns in distributed systems.

### 8. **Dockerize with Multi-stage Builds**

- **Description**: Create optimized Docker images for all services using multi-stage builds.
- **Implementation**: Use Gradle's Docker build support or Jib plugin for layered, cache-friendly images. Create `docker-compose.yml` for orchestration.
- **Goal**: Enable containerized deployment with fast builds and minimal image sizes.
- **Benefit**: Shows mastery of container-based deployment and optimization.

### 9. **Load Testing and Performance Analysis**

- **Description**: Perform load testing and analyze system performance under stress.
- **Implementation**: Use Gatling or JMeter to simulate concurrent users. Test with multiple service instances and measure response times, throughput, and failure rates.
- **Goal**: Document system behavior under load and identify bottlenecks.
- **Benefit**: Demonstrates performance testing methodologies for distributed systems.

### 10. **Resilience Testing with Chaos Engineering**

- **Description**: Implement chaos engineering experiments to test system resilience.
- **Implementation**: Use Chaos Monkey for Spring Boot or create scripts to randomly kill services, introduce latency, or cause failures.
- **Goal**: Verify that the system degrades gracefully and recovers automatically.
- **Benefit**: Shows advanced understanding of reliability engineering in microservices.

**Note**: Unless the goal specifies a particular framework, you are free to use alternative implementations. Document your approach clearly in your submission.
