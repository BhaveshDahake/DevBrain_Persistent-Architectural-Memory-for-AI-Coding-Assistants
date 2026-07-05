# Production Deployment Checklist for DevBrain

## Pre-Deployment Verification

### 1. Security Audit ✓
- [x] No hardcoded API keys or secrets in source code
- [x] All secrets loaded from environment variables
- [x] API keys not exposed in error responses
- [x] .env file excluded from version control (.gitignore)
- [x] .env.example provided for reference
- [x] CORS properly configured (can be restricted via environment)
- [x] Authentication/authorization in place (Supabase optional)
- [x] File upload validation implemented
- [x] File size limits enforced (100MB)

### 2. Configuration ✓
- [x] application.properties for default development
- [x] application-prod.properties for production settings
- [x] logback-spring.xml for production logging
- [x] Database configured for production (PostgreSQL)
- [x] Connection pooling configured
- [x] Graceful shutdown configured
- [x] Server thread pool configured
- [x] Multipart upload limits configured
- [x] Timeout settings configured

### 3. Resilience & Fault Tolerance ✓
- [x] Cognee fallback mechanism implemented
- [x] Circuit breaker pattern for Cognee
- [x] Retry logic with exponential backoff
- [x] Health check endpoints exposed
- [x] Readiness probe configured
- [x] Liveness probe configured
- [x] Graceful degradation when Cognee unavailable
- [x] Local repository search as fallback

### 4. Observability ✓
- [x] Structured logging configured
- [x] Log rotation configured (30 days, 1GB cap)
- [x] Health endpoint (/api/health)
- [x] Readiness endpoint (/api/health/readiness)
- [x] Liveness endpoint (/api/health/liveness)
- [x] Deployment info endpoint (/api/deployment/info)
- [x] Actuator metrics exposed
- [x] Request/response logging in filters

### 5. Container Deployment ✓
- [x] Dockerfile created with multi-stage build
- [x] Dockerfile.frontend created for React app
- [x] nginx configuration for static file serving
- [x] Health checks configured in containers
- [x] Non-root user for security
- [x] Volume mounts for persistent data
- [x] Environment variable injection
- [x] Resource limits can be specified

### 6. Orchestration ✓
- [x] docker-compose.yml for development
- [x] docker-compose-prod.yml for production
- [x] PostgreSQL service included
- [x] Cognee service included
- [x] Service dependencies configured
- [x] Health checks for all services
- [x] Network configuration
- [x] Volume management for persistence

### 7. Deployment Scripts ✓
- [x] deploy-prod.sh for automated deployment
- [x] .env.prod.example with required variables
- [x] Pre-deployment verification steps
- [x] Health check validation
- [x] Rollback considerations documented

### 8. Database ✓
- [x] H2 (development) configured
- [x] PostgreSQL (production) configured
- [x] Connection pool configured
- [x] DDL auto set to validate in production
- [x] Migrations approach documented
- [x] Backup considerations noted

### 9. Upload Handling ✓
- [x] Max file size: 100MB
- [x] Max request size: 100MB
- [x] Multipart location: /tmp/devbrain-uploads
- [x] Temporary files cleaned up
- [x] Rate limiting: 5 uploads/hour
- [x] Dataset max zip size: 500MB
- [x] File sanitization implemented

### 10. Memory Management ✓
- [x] Heap size configured: -Xmx1024m -Xms512m
- [x] G1GC configured for production
- [x] GC pause targets set
- [x] Connection pool size limits
- [x] Async log appender for performance

### 11. Performance ✓
- [x] Tomcat thread pool configured
- [x] Connection timeout: 20 seconds
- [x] Request max connections: 10000
- [x] Accept queue size: 100
- [x] Rate limiting: 30 req/min (chat), 20 req/hour (memory)
- [x] Gzip compression enabled

### 12. Error Handling ✓
- [x] Global exception handler implemented
- [x] Validation error handling
- [x] Cognee error mapping
- [x] Graceful error responses
- [x] Error logging without sensitive data
- [x] Custom error pages

## Deployment Steps

### 1. Prepare Environment
```bash
# Copy production environment template
cp .env.prod.example .env.prod

# Edit with actual values
nano .env.prod

# Verify all required variables are set
grep -E "DATASOURCE_PASSWORD|COGNEE_API_KEY|GROQ_API_KEY" .env.prod
```

### 2. Build & Test
```bash
# Run unit tests
./mvnw test

# Build production Docker images
docker-compose -f docker-compose-prod.yml build

# Verify images
docker images | grep devbrain
```

### 3. Deploy
```bash
# Deploy using script
./deploy-prod.sh .env.prod

# Or manually
docker-compose -f docker-compose-prod.yml up -d
```

### 4. Verify
```bash
# Check service health
docker-compose -f docker-compose-prod.yml ps

# Test backend health
curl http://localhost:8080/api/health

# Test deployment info
curl http://localhost:8080/api/deployment/info

# Check logs
docker-compose -f docker-compose-prod.yml logs -f backend
```

### 5. Monitor
```bash
# Monitor logs
docker-compose -f docker-compose-prod.yml logs -f

# Check metrics
curl http://localhost:8080/actuator/metrics

# Health status
curl http://localhost:8080/actuator/health
```

## Post-Deployment Verification

### Endpoint Testing
```bash
# Health endpoint
curl http://localhost:8080/api/health
Expected: {"status":"UP","service":"devbrain-backend",...}

# Readiness probe
curl http://localhost:8080/actuator/health/readiness
Expected: {"status":"UP"}

# Liveness probe  
curl http://localhost:8080/actuator/health/liveness
Expected: {"status":"UP"}

# Deployment info
curl http://localhost:8080/api/deployment/info
Expected: {"service":"devbrain-backend","version":"0.0.1-SNAPSHOT",...}
```

### Upload Testing
```bash
# Create test file
echo "test content" > test.zip

# Test upload
curl -F "file=@test.zip" http://localhost:8080/api/dataset/upload
Expected: 200 OK response

# Verify rate limiting (upload limit: 5/hour)
# Repeat upload 6 times - 6th should return 429
```

### Fallback Testing
```bash
# Check fallback status in health endpoint
curl http://localhost:8080/api/health | jq '.fallbackActive'
Expected: false (when Cognee is healthy)

# Stop Cognee service to trigger fallback
docker stop devbrain_cognee

# After cooldown period, fallback should activate
curl http://localhost:8080/api/health | jq '.fallbackActive'
Expected: true (after cooldown)
```

## Production Configuration Notes

### Database
- **Development**: H2 In-Memory (not persisted)
- **Production**: PostgreSQL 16 with persistent volume
- **DDL Mode**: validate (requires migrations)
- **Connection Pool**: 20 max, 5 min, 30min timeout
- **Backup**: Use `pg_dump` for backups

### Cognee Integration
- **Cloud URL**: https://api.cognee.ai
- **Fallback**: Local repository search when unavailable
- **Circuit Breaker**: Activates after 3 failures
- **Cooldown**: 1 minute before recovery attempt
- **Timeout**: 60 seconds per request

### Logging
- **Level**: WARN (production), INFO (app code)
- **File Rotation**: Daily or 10MB (whichever first)
- **Retention**: 30 days, max 1GB total
- **Appender**: Async for performance
- **Format**: ISO-8601 timestamp, thread, level, logger, message

### Security
- **CORS**: Configure allowed origins in environment
- **Authentication**: Optional Supabase integration
- **File Uploads**: Validated and size-limited
- **Secrets**: All from environment, never in code
- **Headers**: Security headers added by nginx

### Performance Tuning
- **Heap**: Xmx1024m -Xms512m (adjustable)
- **GC**: G1GC with 200ms pause target
- **Threads**: 200 max, 10 min spare, 100 accept queue
- **Connection Pool**: 20 max, idle timeout 10min
- **Gzip**: Enabled for text content

## Troubleshooting

### Service Won't Start
1. Check logs: `docker-compose -f docker-compose-prod.yml logs backend`
2. Verify environment variables: `docker inspect devbrain_backend`
3. Check database connectivity: `docker exec devbrain_postgres psql -U postgres -d devbrain`
4. Verify port availability: `docker ps | grep devbrain`

### Cognee Connection Issues
1. Check Cognee health: `curl http://localhost:8000/health`
2. Verify API key: `echo $COGNEE_API_KEY`
3. Test connectivity: `curl -H "X-Api-Key: $COGNEE_API_KEY" $COGNEE_BASE_URL/api/v1/search`
4. Check fallback is working: `curl http://localhost:8080/api/health | jq '.fallbackActive'`

### Upload Failures
1. Check file size: Must be < 100MB
2. Check rate limit: 5 uploads/hour
3. Check disk space: Need 500MB for processing
4. Check temp directory: `/tmp/devbrain-uploads`

### Memory Issues
1. Check current usage: `docker stats devbrain_backend`
2. Increase heap: Modify JAVA_OPTS in docker-compose-prod.yml
3. Check GC logs: Enable -XX:+PrintGCDetails
4. Profile: Use jvm profiler container

## Backup & Recovery

### Database Backup
```bash
docker exec devbrain_postgres pg_dump -U postgres devbrain > backup.sql
```

### Database Restore
```bash
docker exec -i devbrain_postgres psql -U postgres devbrain < backup.sql
```

### Volume Backup
```bash
docker run --rm -v devbrain_workspace:/data -v $(pwd):/backup alpine tar czf /backup/workspace.tar.gz /data
```

## Scaling Considerations

- **Stateless Backend**: Can run multiple instances behind load balancer
- **Database**: Needs separate PostgreSQL cluster for HA
- **Cognee**: Can use cloud service instead of container
- **Frontend**: Serve from CDN for global distribution
- **Session State**: Consider Redis for session management

## Security Hardening

- [ ] Enable SSL/TLS in production
- [ ] Set up rate limiting at reverse proxy level
- [ ] Enable WAF (Web Application Firewall)
- [ ] Regular security scanning
- [ ] API key rotation schedule
- [ ] Database encryption at rest
- [ ] Network isolation/VPC setup
- [ ] Regular backups and disaster recovery testing
