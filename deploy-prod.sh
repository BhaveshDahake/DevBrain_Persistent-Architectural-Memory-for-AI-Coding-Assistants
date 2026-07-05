#!/bin/bash
# Production deployment script for DevBrain
# Usage: ./deploy-prod.sh [environment-file]

set -e

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(dirname "$SCRIPT_DIR")"
ENV_FILE="${1:-.env.prod}"

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

echo -e "${GREEN}DevBrain Production Deployment${NC}"
echo "================================"

# Verify environment file
if [ ! -f "$ENV_FILE" ]; then
    echo -e "${RED}Error: Environment file '$ENV_FILE' not found${NC}"
    exit 1
fi

echo -e "${YELLOW}Loading environment from: $ENV_FILE${NC}"
export $(cat "$ENV_FILE" | grep -v '^#' | xargs)

# Verify required variables
REQUIRED_VARS=("DATASOURCE_PASSWORD" "COGNEE_API_KEY" "GROQ_API_KEY")
for var in "${REQUIRED_VARS[@]}"; do
    if [ -z "${!var}" ]; then
        echo -e "${RED}Error: Required environment variable '$var' is not set${NC}"
        exit 1
    fi
done

# Build images
echo -e "${YELLOW}Building Docker images...${NC}"
docker-compose -f "$PROJECT_ROOT/docker-compose-prod.yml" build

# Pull latest Cognee image
echo -e "${YELLOW}Pulling latest Cognee image...${NC}"
docker pull cognee/cognee:latest

# Start services
echo -e "${YELLOW}Starting services...${NC}"
docker-compose -f "$PROJECT_ROOT/docker-compose-prod.yml" up -d

# Wait for services to be ready
echo -e "${YELLOW}Waiting for services to be healthy...${NC}"
sleep 10

# Check health
BACKEND_HEALTH=$(docker exec devbrain_backend curl -s http://localhost:8080/actuator/health/readiness || echo "error")
FRONTEND_HEALTH=$(docker exec devbrain_frontend wget -q -O - http://localhost:5173/ > /dev/null 2>&1 && echo "ok" || echo "error")

if [[ $BACKEND_HEALTH == *"UP"* ]]; then
    echo -e "${GREEN}✓ Backend is healthy${NC}"
else
    echo -e "${RED}✗ Backend health check failed${NC}"
    docker-compose -f "$PROJECT_ROOT/docker-compose-prod.yml" logs backend
    exit 1
fi

if [ "$FRONTEND_HEALTH" == "ok" ]; then
    echo -e "${GREEN}✓ Frontend is healthy${NC}"
else
    echo -e "${RED}✗ Frontend health check failed${NC}"
    docker-compose -f "$PROJECT_ROOT/docker-compose-prod.yml" logs frontend
    exit 1
fi

echo -e "${GREEN}Deployment successful!${NC}"
echo "================================"
echo "Backend: http://localhost:8080"
echo "Frontend: http://localhost:5173"
echo "Actuator: http://localhost:8080/actuator"
