# 🎓 FESUP 2026 - Plateforme de Gestion et Optimisation

Bienvenue sur le projet de gestion du Forum FESUP 2026. Cette plateforme complète permet de gérer les vœux des étudiants, l'attribution des salles et l'optimisation des plannings pour plus de 4 000 élèves.

---

## 📖 Sommaire
1. [Vue d'ensemble](#-vue-densemble)
2. [Documentation Utilisateur](#-documentation-utilisateur)
3. [Documentation Technique](#-documentation-technique)
4. [Moteur d'Optimisation](#-moteur-doptimisation)
5. [Dépannage](#-dépannage)

---

## 📋 Vue d'ensemble

Le projet se compose de trois piliers majeurs :
- **Portail Web Étudiant** : Saisie des vœux et consultation des affectations.
- **Tableau de Bord Administrateur** : Import de données, suivi des statistiques et pilotage global.
- **Moteur d'Optimisation (CP-SAT)** : Algorithme puissant basé sur Google OR-Tools pour garantir une affectation optimale respectant 100% des vœux prioritaires.

---

## 👤 Documentation Utilisateur

### 🔑 Identifiants de Test
| Rôle | Login / Identifiant | Mot de passe |
|------|---------------------|--------------|
| **Administrateur** | `admin` | `admin` |
| **Viewer (Lycée Fauriel)** | `prof@fauriel.fr` | `prof` |
| **Viewer (Lycée Brassens)** | `prof@brassens.fr` | `prof` |
| **Étudiant** | INE (ex: `120890177FA`) | *(aucun)* |

### 🛠️ Processus Métier
1. **Initialisation** : L'administrateur dépose les fichiers Excel (`Inputs/`) contenant la liste des élèves et les capacités des salles.
2. **Saisie des Vœux** : Les étudiants se connectent avec leur INE pour choisir leurs 5 activités préférées.
3. **Optimisation** : Une fois les vœux recueillis, l'administrateur lance le moteur d'optimisation.
4. **Consultation** : Les étudiants et les lycées (viewers) consultent les plannings générés sur la plateforme.

---

## 🛠️ Documentation Technique

### 🏗️ Stack Technologique
- **Backend** : Java 17, Spring Boot 3, H2 Database, Apache POI (Excel).
- **Frontend** : Angular 17, Bootstrap 5.
- **Optimisation** : Python 3, Google OR-Tools (CP-SAT Solver).
- **Infrastructure** : Docker, Docker Compose.

### 🚀 Guide de Démarrage

#### Option A : Docker (Recommandé 🐳)
C'est la méthode la plus simple pour lancer tout l'écosystème web.
```bash
docker-compose up --build
```
- **Interface Web** : [http://localhost:4200](http://localhost:4200)
- **API Backend** : [http://localhost:8080](http://localhost:8080)

#### Option B : Lancement Local (Sans Docker)
1. **Backend** : Allez dans `backend/` et lancez `start-backend.bat`.
2. **Frontend** : Allez dans `frontend/` et lancez `start-frontend.bat`.
3. **Accès** : [http://localhost:4200](http://localhost:4200).

---

## 🧠 Moteur d'Optimisation

L'algorithme (`room_attribution/`) gère la complexité mathématique de l'événement.

### ⚖️ Règles et Contraintes
- **Vœux 1 & 2** : Garantis à **100%**.
- **Quantité** : 4 présentations par élève par demi-journée.
- **Diversité** : Maximum 1 Table Ronde et 1 Flash Métier par élève.
- **Capacité** : Strict respect des jauges de salles.
- **Logistique** : 2 vagues d'arrivée (Tôt / Tard) pour lisser les flux.

### 🏃 Lancement de l'Optimiseur
Si vous souhaitez relancer l'algorithme Python manuellement :
```bash
cd room_attribution
python -m venv venv
# Windows
.\venv\Scripts\activate
# Linux/Mac
source venv/bin/activate
pip install -r requirements.txt
python src/main_grouped.py
```

---

## 📂 Structure du Projet
```
POC/
├── backend/          # Serveur Spring Boot & Tests
├── frontend/         # Application Angular & Tests
├── room_attribution/ # Moteur d'optimisation Python
├── tests/            # Dossier centralisé de tous les tests
│   ├── backend/
│   ├── frontend/
│   └── algorithm/    # Script de vérification de l'algorithme
├── Inputs/           # Fichiers sources (Excel)
└── docker-compose.yml
```

---

## 🆘 Dépannage
- **Doublons** : Le système détecte et supprime automatiquement les doublons d'INE lors de l'import.
- **Ports** : Assurez-vous que les ports `8080` et `4200` ne sont pas utilisés par d'autres applications.
- **Logs** : Les logs détaillés de l'import Excel sont disponibles dans la console du backend.
