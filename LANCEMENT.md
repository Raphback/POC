# 🚀 Guide de Démarrage - POC FESUP 2026

Ce document explique comment lancer l'application complète (Backend + Frontend).

## 1. Pré-requis

-   **Java 17** ou supérieur.
-   **Node.js** (v20 recommandé).
-   **Maven**.
-   **Git** (optionnel pour le lancement, requis pour le versionning).

## 2. Lancement Automatisé (Recommandé)

Des scripts PowerShell sont fournis pour simplifier le démarrage.

### Étape 1 : Démarrer le Backend (Serveur)
Ouvrez un terminal (PowerShell) dans le dossier `backend` et exécutez :

```powershell
cd backend
.\start-backend.ps1
```

> **Note** : Le serveur démarre sur le port **8080**. Les logs s'affichent dans `backend/backend.log`.
> Attendez de voir "Started PocApplication" dans les logs ou que la fenêtre ne signale pas d'erreur immédiate.

### Étape 2 : Démarrer le Frontend (Interface)
Ouvrez un **nouveau** terminal dans le dossier `frontend` et exécutez :

```powershell
cd frontend
.\start-frontend.ps1
```

> **Note** : L'application sera accessible sur **http://localhost:4200**.

## 3. Accès à l'Application

-   **Interface Utilisateur** : [http://localhost:4200](http://localhost:4200)
-   **Base de Données (Console H2)** : [http://localhost:8080/h2-console](http://localhost:8080/h2-console)
    -   JDBC URL : `jdbc:h2:file:./data/fesup_db`
    -   User : `sa`
    -   Password : (vide)

## 4. Dépannage

-   **Port 8080 ou 4200 déjà utilisé ?**
    -   Fermez les terminaux ouverts.
    -   Forcez l'arrêt des processus Java/Node :
        ```powershell
        Stop-Process -Name "java" -Force -ErrorAction SilentlyContinue
        Stop-Process -Name "node" -Force -ErrorAction SilentlyContinue
        ```

-   **Les données ne s'affichent pas ?**
    -   Vérifiez que les fichiers Excel sont bien présents dans le dossier `Inputs`.
    -   Consultez `backend/backend.log` pour voir les erreurs d'import.
