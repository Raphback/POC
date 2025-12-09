# Script de démarrage Docker pour POC FESUP 2026 (Windows)

Write-Host "🐳 Démarrage du projet POC FESUP avec Docker..." -ForegroundColor Cyan
Write-Host ""

# Vérifier que Docker est installé
if (-not (Get-Command docker -ErrorAction SilentlyContinue)) {
    Write-Host "❌ Docker n'est pas installé. Veuillez installer Docker Desktop d'abord." -ForegroundColor Red
    exit 1
}

# Vérifier que Docker Compose est installé
if (-not (Get-Command docker-compose -ErrorAction SilentlyContinue)) {
    Write-Host "❌ Docker Compose n'est pas installé. Veuillez installer Docker Desktop d'abord." -ForegroundColor Red
    exit 1
}

Write-Host "✅ Docker et Docker Compose sont installés" -ForegroundColor Green
Write-Host ""

# Construire et lancer les conteneurs
Write-Host "🔨 Construction des images Docker..." -ForegroundColor Yellow
docker-compose build

Write-Host ""
Write-Host "🚀 Démarrage des services..." -ForegroundColor Yellow
docker-compose up -d

Write-Host ""
Write-Host "⏳ Attente du démarrage des services..." -ForegroundColor Yellow
Start-Sleep -Seconds 5

Write-Host ""
Write-Host "✅ Services démarrés !" -ForegroundColor Green
Write-Host ""
Write-Host "📍 Accès aux applications :" -ForegroundColor Cyan
Write-Host "   - Frontend : http://localhost:4200"
Write-Host "   - Backend  : http://localhost:8080"
Write-Host "   - H2 Console : http://localhost:8080/h2-console"
Write-Host ""
Write-Host "📋 Commandes utiles :" -ForegroundColor Cyan
Write-Host "   - Voir les logs : docker-compose logs -f"
Write-Host "   - Arrêter : docker-compose down"
Write-Host "   - Redémarrer : docker-compose restart"
Write-Host ""

