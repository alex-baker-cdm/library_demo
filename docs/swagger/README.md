# Library Demo API Documentation

This directory contains the OpenAPI/Swagger documentation for the Library Demo API.

## Files

- `openapi.yaml` - OpenAPI 3.0.3 specification for the Library Demo API

## API Overview

The Library Demo API provides endpoints for managing patron profiles in a library system. The API follows Domain-Driven Design principles and uses HATEOAS (Hypermedia as the Engine of Application State) for resource linking.

### Available Endpoints

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/profiles/{patronId}` | Get patron profile |
| GET | `/profiles/{patronId}/holds/` | Get all holds for a patron |
| POST | `/profiles/{patronId}/holds` | Place a hold on a book |
| GET | `/profiles/{patronId}/holds/{bookId}` | Get a specific hold |
| DELETE | `/profiles/{patronId}/holds/{bookId}` | Cancel a hold |
| GET | `/profiles/{patronId}/checkouts/` | Get all checkouts for a patron |
| GET | `/profiles/{patronId}/checkouts/{bookId}` | Get a specific checkout |

## Viewing the Documentation

You can view this OpenAPI specification using various tools:

### Swagger UI

1. Visit [Swagger Editor](https://editor.swagger.io/)
2. Import the `openapi.yaml` file

### Redoc

1. Visit [Redocly](https://redocly.github.io/redoc/)
2. Provide the URL to the raw `openapi.yaml` file

### Local Development

If you have the application running locally, you can integrate Swagger UI by adding the `springdoc-openapi` dependency to the project.

## Domain Concepts

- **Patron**: A library user who can place holds and checkout books
- **Hold**: A reservation placed on a book by a patron
- **Checkout**: A book that has been borrowed by a patron
- **Library Branch**: A physical location where books can be held and checked out

## Business Rules

- Available books can be placed on hold only by one patron at any given point in time
- A restricted book can only be held by a researcher patron
- A regular patron is limited to five holds at any given moment
- A researcher patron is allowed an unlimited number of holds
- Only a researcher patron can request an open-ended hold duration
- Any patron with more than two overdue checkouts at a library branch will get a rejection if trying a hold at that same library branch
- A book can be checked out for up to 60 days
