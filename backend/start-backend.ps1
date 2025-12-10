# Script de démarrage du Backend FESUP (keeps process alive)

Write-Host "=== Démarrage du Backend FESUP ===" -ForegroundColor Cyan

# Configuration Java et Maven
$env:JAVA_HOME = "$env:USERPROFILE\.java\jdk-17.0.2"
$env:M2_HOME = "$env:USERPROFILE\.maven\apache-maven-3.9.5"
$env:PATH = "$env:JAVA_HOME\bin;$env:M2_HOME\bin;$env:PATH"

Write-Host "Java Home: $env:JAVA_HOME" -ForegroundColor Yellow
Write-Host "Maven Home: $env:M2_HOME" -ForegroundColor Yellow

# Vérification
java -version
mvn -version

Write-Host "`nDémarrage de Spring Boot..." -ForegroundColor Green
# Lancer Spring Boot en arrière‑plan
Start-Process -FilePath "mvn" -ArgumentList "spring-boot:run" -WorkingDirectory "$PSScriptRoot" -NoNewWindow -RedirectStandardOutput "backend.log" -RedirectStandardError "backend_error.log"
Write-Host "Spring Boot lancé en arrière‑plan (logs: backend.log)" -ForegroundColor Green
