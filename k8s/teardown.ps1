# Teardown — Delete all K8s resources
Write-Host "Deleting all movie-booking resources..." -ForegroundColor Red
kubectl delete namespace movie-booking
Write-Host "Done! All resources removed." -ForegroundColor Green
