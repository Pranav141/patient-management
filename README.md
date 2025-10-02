# Patient Management System

A microservices-based healthcare management platform that provides comprehensive patient, billing, and analytics services.

## System Architecture

The system is built using a microservices architecture with the following components:

- **Patient Service**: Manages patient information and medical records
- **Billing Service**: Handles payment processing and invoicing
- **Analytics Service**: Provides insights and reporting capabilities
- **Auth Service**: Manages user authentication and authorization
- **API Gateway**: Routes and manages incoming requests
- **Discovery Service**: Service registration and discovery using Eureka

## Technology Stack

- **Backend Framework**: Java Spring Boot
- **Service Communication**: 
  - gRPC with Protocol Buffers for synchronous communication
  - Apache Kafka for asynchronous event-driven communication
- **Service Discovery**: Netflix Eureka
- **API Gateway**: Spring Cloud Gateway
- **Load Balancing**: Spring Cloud Load Balancer
- **Build Tool**: Maven
- **Containerization**: Docker
- **Database**: SQL (with JPA/Hibernate)

## Services Overview

### Patient Service
- Manages patient records
- Handles patient registration and updates
- Provides patient search and retrieval endpoints

### Billing Service
- Processes medical bills
- Manages payment transactions
- Generates invoices and statements

### Analytics Service
- Processes patient and billing data
- Generates reports and insights
- Handles data aggregation and analysis

### Auth Service
- Manages user authentication
- Handles authorization and access control
- Secures service-to-service communication

## Getting Started

### Prerequisites
- Java 17 or higher
- Maven 3.6 or higher
- Docker
- Kafka

### Setup and Installation

1. Clone the repository:
```bash
git clone https://github.com/Pranav141/patient-management.git
cd patient-management
```

2. Build all services:
```bash
mvn clean install
```

3. Start the services using Docker:
```bash
docker-compose up
```

## Service Endpoints

### Discovery Service
- Default Port: 8761
- Eureka Dashboard: http://localhost:8761

### API Gateway
- Default Port: 8080
- Main entry point for all client requests

### Patient Service
- Default Port: 8081
- Endpoints documented in `/api-requests/patient-service`

### Billing Service
- Default Port: 8082
- gRPC and REST endpoints available

### Analytics Service
- Default Port: 8083
- Kafka consumer for event processing

### Auth Service
- Default Port: 8084
- Authentication and authorization endpoints

## Contributing

1. Fork the repository
2. Create your feature branch (`git checkout -b feature/AmazingFeature`)
3. Commit your changes (`git commit -m 'Add some AmazingFeature'`)
4. Push to the branch (`git push origin feature/AmazingFeature`)
5. Open a Pull Request

## License

This project is licensed under the MIT License - see the LICENSE file for details.