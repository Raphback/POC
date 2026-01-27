# 🐳 Guide Docker - POC FESUP 2026

Ce guide explique comment lancer le projet avec Docker.

## 📋 Prérequis

| Outil | Version | Vérification |
|-------|---------|--------------|
| **Docker** | 20.10+ | `docker --version` |
| **Docker Compose** | 2.0+ | `docker compose version` |

---

## 🚀 Démarrage Rapide

### 1. Construire et lancer

```bash
# Depuis la racine du projet
docker-compose up --build
```

Cette commande va :
- ✅ Construire les images Docker (backend + frontend)
- ✅ Créer le réseau Docker pour la communication
- ✅ Lancer les conteneurs
- ✅ Gérer automatiquement les doublons d'INE

### 2. Accéder aux applications

| Service | URL |
|---------|-----|
| **Frontend** | http://localhost:4200 |
| **Backend API** | http://localhost:8080 |
| **Console H2** | http://localhost:8080/h2-console |

### 3. Arrêter les services

```bash
# Arrêter (conserver les données)
docker-compose down

# Arrêter et supprimer les volumes (reset complet)
docker-compose down -v
```

---

## 🔧 Commandes Utiles

### Logs

```bash
# Tous les services
docker-compose logs -f

# Backend uniquement
docker-compose logs -f backend

# Frontend uniquement
docker-compose logs -f frontend
```

### Reconstruire

```bash
# Reconstruire sans cache
docker-compose build --no-cache

# Reconstruire et relancer
docker-compose up --build
```

### État des conteneurs

```bash
docker-compose ps
```

---

## 🏗️ Architecture

```
┌─────────────────┐
│   Frontend      │  (nginx + Angular)
│   Port: 4200    │
└────────┬────────┘
         │ /api/*
┌────────▼────────┐
│   Backend       │  (Spring Boot + H2)
│   Port: 8080    │
└─────────────────┘
```

| Composant | Description |
|-----------|-------------|
| **Frontend** | nginx servant l'app Angular compilée |
| **Backend** | Spring Boot avec H2 embarqué |
| **Réseau** | `poc-network` (bridge) |
| **Volume** | `backend_data` pour la persistance H2 |

---

## 🔍 Dépannage

### Port déjà utilisé

Modifier les ports dans `docker-compose.yml` :
```yaml
ports:
  - "8081:8080"  # Backend sur 8081
  - "4201:80"    # Frontend sur 4201
```

### Erreur de build / JAR corrompu

```bash
# Nettoyage complet et rebuild
docker-compose down
docker system prune -f
docker builder prune -af
docker-compose up --build
```

### Problème de CORS

Le backend accepte les requêtes depuis :
- `http://localhost:4200` (dev)
- `http://frontend:80` (Docker)

---

## 📁 Structure Docker

```
POC/
├── docker-compose.yml     # Orchestration des services
├── backend/
│   └── Dockerfile         # Build multi-stage Maven → JRE
├── frontend/
│   ├── Dockerfile         # Build multi-stage Node → nginx
│   └── nginx.conf         # Config proxy API
└── Inputs/                # Monté en volume pour les Excel
```

---

## 💡 Fonctionnalités Automatiques

Le conteneur backend gère automatiquement :

- 🧹 **Détection des doublons INE** : Suppression automatique au démarrage
- 📥 **Import des données** : Chargement depuis `data.sql`
- 👤 **Création des comptes** : Admin et Viewers par défaut
- 📊 **Activités** : 30 activités créées (conférences, tables rondes, flash métiers)

---

## 🚀 Déploiement en Production

Pour la production, considérez :

1. **Variables d'environnement** : Fichier `.env`
2. **Base de données** : PostgreSQL/MySQL au lieu de H2
3. **HTTPS** : Reverse proxy avec certificats SSL
4. **Monitoring** : Prometheus + Grafana

---

Bon développement ! 🎓
