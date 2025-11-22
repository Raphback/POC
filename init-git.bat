@echo off
echo ========================================
echo   Initialisation Git - FESUP 2026
echo ========================================

cd /d "%~dp0"

REM Chercher git.exe
set GIT_PATH=
if exist "C:\Program Files\Git\bin\git.exe" set GIT_PATH=C:\Program Files\Git\bin\git.exe
if exist "C:\Program Files (x86)\Git\bin\git.exe" set GIT_PATH=C:\Program Files (x86)\Git\bin\git.exe
if exist "%USERPROFILE%\AppData\Local\Programs\Git\bin\git.exe" set GIT_PATH=%USERPROFILE%\AppData\Local\Programs\Git\bin\git.exe

if "%GIT_PATH%"=="" (
    echo [ERREUR] Git non trouve. Veuillez installer Git d'abord.
    pause
    exit /b 1
)

echo Git trouve : %GIT_PATH%
echo.

REM Initialiser le repository
echo [1/5] Initialisation du repository...
"%GIT_PATH%" init
if errorlevel 1 (
    echo [ERREUR] Echec de l'initialisation
    pause
    exit /b 1
)

REM Configurer user (si pas deja fait)
"%GIT_PATH%" config user.name > nul 2>&1
if errorlevel 1 (
    echo Configuration utilisateur Git...
    "%GIT_PATH%" config user.name "Arthur Lemarc"
    "%GIT_PATH%" config user.email "arthur.lemarc@example.com"
)

REM Ajouter tous les fichiers
echo [2/5] Ajout des fichiers...
"%GIT_PATH%" add .
if errorlevel 1 (
    echo [ERREUR] Echec de l'ajout des fichiers
    pause
    exit /b 1
)

REM Commit initial
echo [3/5] Commit initial...
"%GIT_PATH%" commit -m "Initial commit - FESUP 2026 v1.0"
if errorlevel 1 (
    echo [ERREUR] Echec du commit
    pause
    exit /b 1
)

REM Renommer en main
echo [4/5] Creation branche main...
"%GIT_PATH%" branch -M main

echo.
echo ========================================
echo   Repository Git initialise !
echo ========================================
echo.
echo PROCHAINE ETAPE : Pusher sur GitHub
echo.
echo 1. Creer un repository sur GitHub
echo 2. Copier l'URL du repository
echo 3. Executer :
echo    git remote add origin URL_COPIEE
echo    git push -u origin main
echo.
pause
