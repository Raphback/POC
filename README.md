# 🎓 FESUP 2026 - Gestion des Vœux

Application de gestion des vœux pour le Forum FESUP 2026.
Permet aux étudiants de saisir leurs choix de conférences et ateliers, et aux administrateurs de gérer les affectations.

## 🚀 Démarrage Rapide

### Option 1 : Docker (Recommandé) 🐳

```bash
# Depuis la racine du projet
docker-compose up --build
```

Accès :
- **Frontend** : http://localhost:4200
- **Backend API** : http://localhost:8080

> Pour plus de détails, voir [DOCKER.md](DOCKER.md)

### Option 2 : Lancement Local

Pour lancer le projet sans Docker, consultez [LANCEMENT.md](LANCEMENT.md)

## 👤 Identifiants de Test

| Rôle | Login | Mot de passe |
|------|-------|--------------|
| **Admin** | `admin` | `admin` |
| **Viewer (Fauriel)** | `prof@fauriel.fr` | `prof` |
| **Viewer (Brassens)** | `prof@brassens.fr` | `prof` |
| **Étudiant** | INE (ex: `120890177FA`) | - |

## 📂 Structure du Projet

```
POC/
├── backend/          # API Spring Boot (Java 17)
├── frontend/         # Application Angular 17
├── Inputs/           # Fichiers Excel des étudiants
├── docker-compose.yml
├── DOCKER.md         # Guide Docker détaillé
├── LANCEMENT.md      # Guide de lancement local
└── README.md         # Ce fichier
```

## 📂 Données et Inputs

Le projet charge automatiquement les données au démarrage depuis le dossier `Inputs/`.

### Fichiers Sources

1. **Étudiants** (`.xlsx` ou `.xls`) :
   - Liste des élèves (Nom, Prénom, Matricule, INE, Lycée)
   - Exemple : `LGT Fauriel FESUP 2026.xlsx`

2. **Activités et Capacités** (`capacites.xlsx`) :
   - Liste des conférences, tables rondes et flash métiers
   - Colonnes : Titre, Salle, Capacité

### Gestion des Doublons

Le système détecte et supprime automatiquement les doublons d'INE au démarrage pour éviter les conflits.

## 🛠️ Stack Technique

| Composant | Technologies |
|-----------|--------------|
| **Backend** | Java 17, Spring Boot 3, H2 Database, Apache POI |
| **Frontend** | Angular 17, Bootstrap, CSS custom |
| **DevOps** | Docker, Docker Compose, Maven, NPM |

## 📋 Fonctionnalités

- ✅ 3 modes : Admin, Viewer, Étudiant
- ✅ Connexion par INE pour les étudiants
- ✅ Import automatique des données Excel
- ✅ Gestion des doublons automatique
- ✅ Déploiement Docker simplifié

## 👥 Auteurs

Projet réalisé pour le FESUP 2026.

## 📝 Changelog

### v1.1.0 (Janvier 2026)
- Ajout de la détection/suppression automatique des doublons INE
- Mise à jour de Docker Compose (suppression version obsolète)
- Amélioration de la gestion des erreurs d'import

### v1.0.0
- Version initiale
