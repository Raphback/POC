# Script de démarrage du Backend FESUP (keeps process alive)

Write-Host "=== Démarrage du Backend FESUP ===" -ForegroundColor Cyan

# Vérification de Java
try {
    $javaVersion = java -version 2>&1 | Select-String "version"
    Write-Host "Java trouvé: $javaVersion" -ForegroundColor Yellow
} catch {
    Write-Host "ERREUR: Java non trouvé. Installez Java 17 ou supérieur." -ForegroundColor Red
    exit 1
}

Write-Host "`nDémarrage de Spring Boot avec Maven Wrapper..." -ForegroundColor Green

# Utiliser Maven Wrapper (mvnw.cmd) qui est inclus dans le projet
# Pas besoin d'installer Maven globalement
Set-Location $PSScriptRoot
.\mvnw.cmd spring-boot:run

Write-Host "Spring Boot arrêté." -ForegroundColor Yellow
