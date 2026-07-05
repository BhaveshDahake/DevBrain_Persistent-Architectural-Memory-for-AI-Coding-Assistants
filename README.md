# DevBrain

DevBrain is a full-stack application that turns a repository into a persistent, queryable knowledge layer for developers. Users can upload a codebase, extract its structure, and ask questions about the repository in natural language while the system grounds answers in the uploaded project context.

## What this project does

- Uploads a repository archive and prepares it for semantic analysis.
- Builds a knowledge graph over repository structure and relationships.
- Exposes chat and graph-based interactions through a React frontend and a Spring Boot backend.
- Supports local development and deployment-friendly configuration for GitHub, Vercel, and Render.

## Tech stack

- Frontend: React, Vite, Tailwind CSS
- Backend: Java, Spring Boot, Spring WebFlux, Spring AI
- Memory layer: Cognee integration
- Deployment: Docker, Vercel, Render

## Project structure

```text
DevBrain/
├── frontend/          # React/Vite frontend
├── src/               # Spring Boot backend source
├── Dockerfile         # Backend container build
├── Dockerfile.frontend
├── docker-compose.yml
├── pom.xml            # Maven configuration
└── README.md
```

## Prerequisites

Before running the app locally, make sure you have:

- Java 21 or newer
- Node.js 18 or newer
- Maven or the provided Maven wrapper
- API credentials for the services you plan to use, such as Cognee and an LLM provider

## Environment variables

Create a copy of the example environment file and update the values as needed:

```bash
cp .env.example .env
```

The main variables include:

- PORT
- COGNEE_BASE_URL
- COGNEE_API_KEY
- GROQ_API_KEY
- GROQ_API_BASE
- GROQ_MODEL
- SUPABASE_URL
- SUPABASE_ANON_KEY

## Run locally

### 1. Start the backend

```bash
./mvnw spring-boot:run
```

The backend will start on the port defined in your environment configuration.

### 2. Start the frontend

```bash
cd frontend
npm install
npm run dev
```

Then open the local frontend URL shown by Vite in your browser.

## Build locally

### Backend

```bash
./mvnw clean package -DskipTests
```

### Frontend

```bash
cd frontend
npm install
npm run build
```

## Deployment notes

- The repository includes deployment configuration for Vercel and Render.
- The backend can be containerized with Docker.
- Store secrets in your hosting platform environment variables rather than in the repository.

## License

This project is licensed under the MIT License.
