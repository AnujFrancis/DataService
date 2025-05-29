# Data Service (Java)

This service provides CRUD operations for customer data management. It serves as the data layer for the three-tier architecture.

## Features

- Create, read, update, and delete customer records
- Local JSON file storage
- RESTful API endpoints
- Error handling and validation

## API Endpoints

- `GET /health` - Health check endpoint
- `POST /customers` - Create a new customer
- `GET /customers` - Get all customers
- `GET /customers/search` - Search for customers by ID, name, or alias
- `PUT /customers/:id` - Update a customer
- `DELETE /customers/:id` - Delete a customer

## Setup

1. Build the project:
   ```
   mvn clean package
   ```

2. Run the service:
   ```
   mvn spring-boot:run
   ```

The service will run on port 3002 by default.

## Data Structure

Customer records include:
- id: Unique identifier
- name: Customer's full name
- alias: Customer's login alias (unique)
- dob: Date of birth
- createdAt: Timestamp of record creation
- updatedAt: Timestamp of last update

## Technology Stack

- Java 11
- Spring Boot 2.7.8
- Jackson for JSON processing
