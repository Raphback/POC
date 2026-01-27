# 🚀 Guide de Lancement Local - POC FESUP 2026

Ce guide explique comment lancer l'application en local (sans Docker).

> **💡 Conseil** : Pour un démarrage plus simple, utilisez Docker :
> ```bash
> docker-compose up --build
> ```
> Voir [DOCKER.md](DOCKER.md) pour plus de détails.

---

## 1. Pré-requis

| Outil | Version | Vérification |
|-------|---------|--------------|
| **Java** | 17+ | `java -version` |
| **Node.js** | 18+ | `node -v` |
| **NPM** | 8+ | `npm -v` |

> **Note** : Maven n'a **pas** besoin d'être installé. Le projet utilise Maven Wrapper (`mvnw.cmd`).

---

## 2. Lancement Automatisé (Recommandé)

### Étape 1 : Démarrer le Backend

Double-cliquez sur `backend\start-backend.bat` ou exécutez :

```cmd
cd backend
start-backend.bat
```

✅ Attendez de voir ces messages dans la console :
```
✅ DataInitializer: Initialization complete!
Started PocApplication in X seconds
```

### Étape 2 : Démarrer le Frontend

Dans un **nouveau** terminal, double-cliquez sur `frontend\start-frontend.bat` :

```cmd
cd frontend
start-frontend.bat
```

✅ L'application sera accessible sur **http://localhost:4200**

---

## 3. Accès à l'Application

| Service | URL |
|---------|-----|
| **Interface Web** | http://localhost:4200 |
| **API Backend** | http://localhost:8080/api |
| **Console H2** | http://localhost:8080/h2-console |

### Connexion H2 Console
- JDBC URL : `jdbc:h2:file:./data/fesup_db`
- User : `sa`
- Password : *(vide)*

---

## 4. Identifiants de Test

| Rôle | Login | Mot de passe |
|------|-------|--------------|
| **Admin** | `admin` | `admin` |
| **Viewer (Fauriel)** | `prof@fauriel.fr` | `prof` |
| **Viewer (Brassens)** | `prof@brassens.fr` | `prof` |
| **Étudiant** | INE (ex: `120890177FA`) | - |

---

## 5. Dépannage

### Port déjà utilisé (8080 ou 4200)

```cmd
# Trouver le processus qui utilise le port 8080
netstat -ano | findstr :8080

# Tuer le processus (remplacer XXXX par le PID)
taskkill /PID XXXX /F
```

### Les données ne s'affichent pas

1. Vérifiez que les fichiers Excel sont dans le dossier `Inputs/`
2. Consultez la console backend pour les erreurs d'import
3. Le système gère automatiquement les doublons d'INE

### Erreur "Execution Policy" PowerShell

Utilisez les fichiers `.bat` à la place des `.ps1`, ou exécutez :
```powershell
Set-ExecutionPolicy -Scope CurrentUser -ExecutionPolicy RemoteSigned
```

---

## 6. Arrêter les Services

1. **Backend** : `Ctrl+C` dans le terminal backend
2. **Frontend** : `Ctrl+C` dans le terminal frontend

Ou fermez simplement les fenêtres de terminal.
