# Script de démarrage du Frontend FESUP POC

Write-Host "=== Démarrage du Frontend FESUP ===" -ForegroundColor Cyan

# Vérification de Node.js
try {
    $nodeVersion = node -v 2>&1
    Write-Host "Node.js trouvé: $nodeVersion" -ForegroundColor Yellow
}
catch {
    Write-Host "ERREUR: Node.js non trouvé. Installez Node.js v18 ou supérieur." -ForegroundColor Red
    exit 1
}

Write-Host "`nDémarrage d'Angular..." -ForegroundColor Green

Set-Location $PSScriptRoot
npm start

Write-Host "Angular arrêté." -ForegroundColor Yellow
