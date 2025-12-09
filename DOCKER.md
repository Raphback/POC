# 🐳 Guide Docker - POC FESUP 2026

Ce guide explique comment lancer le projet avec Docker.

## 📋 Prérequis

- **Docker** (version 20.10+)
- **Docker Compose** (version 2.0+)

Vérifiez l'installation :
```bash
docker --version
docker-compose --version
```

## 🚀 Démarrage Rapide

### 1. Construire et lancer tous les services

```bash
# Depuis la racine du projet
docker-compose up --build
```

Cette commande va :
- Construire les images Docker pour le backend et le frontend
- Lancer les deux conteneurs
- Créer un réseau Docker pour la communication entre services
- Créer un volume pour persister les données H2

### 2. Accéder aux applications

Une fois les conteneurs démarrés :

- **Frontend** : http://localhost:4200
- **Backend API** : http://localhost:8080
- **Console H2** : http://localhost:8080/h2-console
  - JDBC URL : `jdbc:h2:file:./data/fesup_db`
  - Username : `sa`
  - Password : (vide)

### 3. Arrêter les services

```bash
# Arrêter les conteneurs (garder les données)
docker-compose down

# Arrêter et supprimer les volumes (perte de données)
docker-compose down -v
```

## 🔧 Commandes Utiles

### Voir les logs

```bash
# Tous les services
docker-compose logs -f

# Backend uniquement
docker-compose logs -f backend

# Frontend uniquement
docker-compose logs -f frontend
```

### Reconstruire un service spécifique

```bash
# Reconstruire le backend
docker-compose up --build backend

# Reconstruire le frontend
docker-compose up --build frontend
```

### Accéder au shell d'un conteneur

```bash
# Backend
docker-compose exec backend sh

# Frontend
docker-compose exec frontend sh
```

### Voir l'état des conteneurs

```bash
docker-compose ps
```

## 🏗️ Architecture

Le projet utilise une architecture multi-conteneurs :

```
┌─────────────────┐
│   Frontend      │  (nginx + Angular)
│   Port: 4200     │
└────────┬────────┘
         │
         │ /api/*
         │
┌────────▼────────┐
│   Backend       │  (Spring Boot + H2)
│   Port: 8080    │
└─────────────────┘
```

- **Frontend** : Serveur nginx qui sert l'application Angular compilée
- **Backend** : Application Spring Boot avec base de données H2 embarquée
- **Réseau** : Les services communiquent via le réseau Docker `poc-network`
- **Volumes** : Les données H2 sont persistées dans un volume Docker

## 📁 Structure des Dockerfiles

### Backend (`backend/Dockerfile`)
- Build multi-stage avec Maven
- Utilise Java 17 (eclipse-temurin)
- Compile l'application Spring Boot
- Expose le port 8080

### Frontend (`frontend/Dockerfile`)
- Build multi-stage avec Node.js
- Compile l'application Angular
- Serve avec nginx
- Configuration nginx pour le routing Angular et le proxy API

## 🔍 Dépannage

### Port déjà utilisé

Si le port 8080 ou 4200 est déjà utilisé :

```bash
# Modifier les ports dans docker-compose.yml
ports:
  - "8081:8080"  # Backend sur 8081
  - "4201:80"    # Frontend sur 4201
```

### Erreur de build

```bash
# Nettoyer et reconstruire
docker-compose down
docker system prune -f
docker-compose up --build
```

### Problème de CORS

Le backend est configuré pour accepter les requêtes depuis :
- `http://localhost:4200` (développement local)
- `http://frontend:80` (Docker)

Si vous accédez depuis un autre domaine, modifiez `SecurityConfig.java`.

### Voir les logs détaillés

```bash
# Logs avec timestamps
docker-compose logs -f --timestamps

# Dernières 100 lignes
docker-compose logs --tail=100
```

## 🚀 Déploiement en Production

Pour la production, considérez :

1. **Variables d'environnement** : Utiliser un fichier `.env` pour les configurations
2. **Base de données** : Remplacer H2 par PostgreSQL ou MySQL
3. **HTTPS** : Ajouter un reverse proxy (nginx/traefik) avec certificats SSL
4. **Healthchecks** : Les healthchecks sont déjà configurés dans docker-compose.yml
5. **Monitoring** : Ajouter des outils de monitoring (Prometheus, Grafana)

## 📝 Notes

- Les données H2 sont persistées dans un volume Docker nommé `backend-data`
- Le frontend fait du proxy vers le backend via nginx (pas besoin de CORS côté frontend)
- Les builds sont mis en cache par Docker pour accélérer les reconstructions

---

Bon développement ! 🎓

