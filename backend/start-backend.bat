@echo off
echo === Demarrage du Backend FESUP ===
echo.

REM Verification de Java
java -version >nul 2>&1
if errorlevel 1 (
    echo ERREUR: Java non trouve. Installez Java 17 ou superieur.
    pause
    exit /b 1
)

echo Java trouve. Demarrage de Spring Boot...
echo.

REM Utiliser Maven Wrapper (mvnw.cmd) inclus dans le projet
cd /d "%~dp0"
call mvnw.cmd spring-boot:run

echo.
echo Spring Boot arrete.
pause
