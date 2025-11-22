$gitPaths = @(
    "C:\Program Files\Git\bin\git.exe",
    "C:\Program Files\Git\cmd\git.exe",
    "C:\Program Files (x86)\Git\bin\git.exe",
    "C:\Program Files (x86)\Git\cmd\git.exe",
    "$env:USERPROFILE\AppData\Local\Programs\Git\bin\git.exe",
    "$env:USERPROFILE\AppData\Local\Programs\Git\cmd\git.exe",
    "$env:USERPROFILE\Downloads\PortableGit\bin\git.exe",
    "C:\Git\bin\git.exe",
    "C:\Git\cmd\git.exe"
)

$gitExe = $null
foreach ($path in $gitPaths) {
    if (Test-Path $path) {
        $gitExe = $path
        break
    }
}

if (-not $gitExe) {
    # Try to find via where.exe
    try {
        $gitExe = (Get-Command git -ErrorAction SilentlyContinue).Source
    }
    catch {}
}

if (-not $gitExe) {
    Write-Host "Git introuvable dans les emplacements standards." -ForegroundColor Red
    Write-Host "Debug: USERPROFILE = $env:USERPROFILE"
    
    Write-Host "Recherche dans Program Files..."
    $found = Get-ChildItem -Path "C:\Program Files", "C:\Program Files (x86)" -Filter "git.exe" -Recurse -ErrorAction SilentlyContinue | Select-Object -First 1
    if ($found) { $gitExe = $found.FullName }
}

if (-not $gitExe) {
    Write-Host "Recherche dans le dossier utilisateur (peut être long)..."
    $found = Get-ChildItem -Path "$env:USERPROFILE" -Filter "git.exe" -Recurse -ErrorAction SilentlyContinue | Select-Object -First 1
    if ($found) { $gitExe = $found.FullName }
}

if (-not $gitExe) {
    Write-Host "IMPOSSIBLE DE TROUVER GIT. Veuillez l'installer manuellement." -ForegroundColor Red
    exit 1
}

Write-Host "Git trouve : $gitExe" -ForegroundColor Green

# Define git command alias
function git { & $gitExe @args }

Write-Host "Verification du repository..."
if (-not (Test-Path ".git")) {
    Write-Host "Initialisation de Git..."
    git init
}

Write-Host "Statut Git :"
git status

Write-Host "Remotes :"
git remote -v

# Check if we need to commit
git add .
git commit -m "Auto-commit: FESUP 2026 update"

# Check if remote exists
$remotes = git remote
if (-not $remotes) {
    Write-Host "Aucun remote configure." -ForegroundColor Yellow
    Write-Host "Veuillez configurer le remote avec :"
    Write-Host "git remote add origin <URL>"
    Write-Host "git push -u origin main"
}
else {
    Write-Host "Tentative de push..."
    git push
}
