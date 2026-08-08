# =====================================================
# Deploy Movie Ticket Booking to Kubernetes
# Usage: .\k8s\deploy.ps1
# Prerequisites: Docker Desktop K8s enabled (kind, 1 node)
# =====================================================

Write-Host "========================================" -ForegroundColor Cyan
Write-Host " Movie Ticket Booking — K8s Deployment" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan

# Step 1: Create namespace
Write-Host "`n[1/7] Creating namespace..." -ForegroundColor Yellow
kubectl apply -f k8s/namespace.yaml

# Step 2: Secrets + ConfigMap
Write-Host "`n[2/7] Applying secrets & config..." -ForegroundColor Yellow
kubectl apply -f k8s/secrets.yaml
kubectl apply -f k8s/configmap.yaml

# Step 3: Infrastructure (SQL Server, MongoDB, Redis, Kafka)
Write-Host "`n[3/7] Deploying infrastructure..." -ForegroundColor Yellow
kubectl apply -f k8s/infra/sqlserver.yaml
kubectl apply -f k8s/infra/mongodb.yaml
kubectl apply -f k8s/infra/redis.yaml
kubectl apply -f k8s/infra/kafka.yaml

Write-Host "`nWaiting 60s for infrastructure to start..." -ForegroundColor Gray
Start-Sleep -Seconds 60

# Step 4: Config Server (must be first)
Write-Host "`n[4/7] Deploying Config Server..." -ForegroundColor Yellow
kubectl apply -f k8s/services/config-server.yaml
Write-Host "Waiting 30s for config-server..." -ForegroundColor Gray
Start-Sleep -Seconds 30

# Step 5: Eureka Server
Write-Host "`n[5/7] Deploying Eureka Server..." -ForegroundColor Yellow
kubectl apply -f k8s/services/eureka-server.yaml
Write-Host "Waiting 30s for eureka..." -ForegroundColor Gray
Start-Sleep -Seconds 30

# Step 6: API Gateway + Business Services
Write-Host "`n[6/7] Deploying API Gateway + Business Services..." -ForegroundColor Yellow
kubectl apply -f k8s/services/api-gateway.yaml
kubectl apply -f k8s/services/business-services.yaml

# Step 7: Frontend
Write-Host "`n[7/7] Deploying Frontend..." -ForegroundColor Yellow
kubectl apply -f k8s/services/frontend.yaml

Write-Host "`n========================================" -ForegroundColor Green
Write-Host " Deployment Complete!" -ForegroundColor Green
Write-Host "========================================" -ForegroundColor Green
Write-Host "`nAccess:" -ForegroundColor Cyan
Write-Host "  Frontend:    http://localhost:30000" -ForegroundColor White
Write-Host "  API Gateway: http://localhost:30080" -ForegroundColor White
Write-Host "`nCheck status:" -ForegroundColor Cyan
Write-Host "  kubectl get pods -n movie-booking" -ForegroundColor White
Write-Host "  kubectl get svc -n movie-booking" -ForegroundColor White
Write-Host "  kubectl logs -f deployment/booking-service -n movie-booking" -ForegroundColor White
