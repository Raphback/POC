@echo off
echo === Demarrage du Frontend FESUP ===
echo.

REM Verification de Node.js
node -v >nul 2>&1
if errorlevel 1 (
    echo ERREUR: Node.js non trouve. Installez Node.js v20 ou superieur.
    pause
    exit /b 1
)

echo Node.js trouve. Demarrage d'Angular...
echo.

cd /d "%~dp0"
call npm start

echo.
echo Angular arrete.
pause
