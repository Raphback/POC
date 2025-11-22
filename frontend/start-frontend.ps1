# Script de démarrage du Frontend FESUP POC

Write-Host "=== Démarrage du Frontend FESUP ===" -ForegroundColor Cyan

# Configure Node/npm path
$env:PATH = "$env:USERPROFILE\.gemini\tools\node\node-v20.10.0-win-x64;$env:PATH"

Write-Host "`nDémarrage d'Angular..." -ForegroundColor Green
npm start
