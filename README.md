# 🎓 FESUP 2026 - Plateforme de Gestion et Optimisation

Bienvenue sur le projet de gestion du Forum FESUP 2026. Cette plateforme complète permet de gérer les vœux des étudiants, l'attribution des salles et l'optimisation des plannings pour plus de 4 000 élèves.

---

## 📚 Documentations

Pour une documentation complète, veuillez consulter les guides suivants :

- 👤 **[Documentation Utilisateur](file:///docs/USER_GUIDE.md)** : Guide pour les étudiants, administrateurs et lycées.
- 🛠️ **[Documentation Technique](file:///docs/TECHNICAL_GUIDE.md)** : Architecture, algorithme, installation et développement.

---

## 📋 Vue d'ensemble

Le projet se compose de trois piliers majeurs :
- **Portail Web Étudiant** : Saisie des vœux et consultation des affectations.
- **Tableau de Bord Administrateur** : Import de données, suivi des statistiques et pilotage global.
- **Moteur d'Optimisation (CP-SAT)** : Algorithme puissant basé sur Google OR-Tools pour garantir une affectation optimale respectant 100% des vœux prioritaires.

---

## 🚀 Démarrage Rapide

### Option A : Docker (Recommandé 🐳)
```bash
docker-compose up --build
```
- **Interface Web** : [http://localhost:4200](http://localhost:4200)
- **API Backend** : [http://localhost:8080](http://localhost:8080)

### Option B : Lancement Local
Consultez le **[Guide Technique](file:///docs/TECHNICAL_GUIDE.md)** pour les instructions détaillées de lancement local sans Docker.

---

## 📂 Structure du Projet
```
POC/
├── docs/             # Documentation détaillée (Utilisateur et Technique)
├── backend/          # Serveur Spring Boot & Tests
├── frontend/         # Application Angular & Tests
├── room_attribution/ # Moteur d'optimisation Python
├── tests/            # Dossier centralisé de tous les tests
├── Inputs/           # Fichiers sources (Excel)
└── docker-compose.yml
```

---

## 🆘 Dépannage
Pour tout problème commun (identifiants, ports, logs), veuillez vous référer à la section **Dépannage** du **[Guide Utilisateur](file:///docs/USER_GUIDE.md)**.

