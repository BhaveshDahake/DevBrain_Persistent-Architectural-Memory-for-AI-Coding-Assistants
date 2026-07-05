# DevBrain

[![React](https://img.shields.io/badge/React-20232A?style=for-the-badge&logo=react&logoColor=61DAFB)](https://react.dev/)
[![Vite](https://img.shields.io/badge/Vite-646CFF?style=for-the-badge&logo=vite&logoColor=white)](https://vitejs.dev/)
[![Spring Boot](https://img.shields.io/badge/Spring_Boot-6DB33F?style=for-the-badge&logo=springboot&logoColor=white)](https://spring.io/projects/spring-boot)
[![Java](https://img.shields.io/badge/Java-21%2B-007396?style=for-the-badge&logo=openjdk&logoColor=white)](https://www.oracle.com/java/)
[![Supabase](https://img.shields.io/badge/Supabase-3ECF8E?style=for-the-badge&logo=supabase&logoColor=white)](https://supabase.com/)
[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](LICENSE)

DevBrain is a full-stack application that turns a repository into a persistent, queryable knowledge layer for developers. It combines a polished landing experience, authenticated access, a repository upload workflow, a chat interface, and a visual architecture graph to make codebase understanding more grounded and repeatable.

## What this project does

- Delivers a landing page and authentication flow with Supabase-backed sign-in and sign-up.
- Lets users upload repository archives and prepare them for semantic analysis.
- Builds a knowledge graph over repository structure and relationships.
- Supports conversational repository interaction through a React frontend and a Spring Boot backend.
- Includes deployment-ready configuration for GitHub, Vercel, and Render.

## Cognee lifecycle verbs

DevBrain is built around the four Cognee lifecycle verbs that power the product experience:

- Remember: ingest uploaded repositories, clean them, and build structured repository memory.
- Recall: answer questions about a repository using grounded retrieval and contextual references.
- Improve: reinforce useful responses so future retrieval becomes stronger over time.
- Forget: reset context and remove repository memory when a new task or project context is needed.

## Core experience

- Landing page with product storytelling and clear calls to action.
- Supabase authentication for sign-in, sign-up, and OAuth-based entry.
- Repository upload and processing workflow.
- Chat-based repository interaction with grounded responses.
- Interactive graph visualization for architecture exploration.

## Tech stack

- Frontend: React, Vite, Tailwind CSS
- Backend: Java, Spring Boot, Spring WebFlux, Spring AI
- Authentication: Supabase
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
- Supabase credentials for authentication

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
