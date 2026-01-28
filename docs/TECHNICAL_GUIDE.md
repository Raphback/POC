# 🛠️ Guide Technique - FESUP 2026

Ce document détaille l'architecture technique, les choix technologiques et les procédures de maintenance de la plateforme FESUP 2026.

---

## 🏗️ Architecture Globale

Le projet suit une architecture découplée composée de trois modules principaux :

### 1. Frontend (Angular)
- **Localisation** : `/frontend`
- **Techno** : Angular 17, Bootstrap 5.
- **Rôle** : Interface utilisateur pour les étudiants, les administrateurs et les lycées. Communique avec le backend via une API REST.

### 2. Backend (Spring Boot)
- **Localisation** : `/backend`
- **Techno** : Java 17, Spring Boot 3, Spring Security, H2 Database.
- **Rôle** : Gestion de la persistance, authentification, logique métier et exposition de l'API.

### 3. Moteur d'Optimisation (Python)
- **Localisation** : `/room_attribution`
- **Techno** : Python 3.10+, Google OR-Tools (CP-SAT Solver).
- **Rôle** : Résolution du problème d'affectation sous contraintes.

---

## 🧠 Algorithme d'Optimisation

Le moteur utilise le solveur **CP-SAT** pour maximiser le score de satisfaction des étudiants.

### Contraintes Implémentées :
- **Affectation Unique** : Un étudiant ne peut pas être à deux endroits en même temps.
- **Capacité des Salles** : Ne jamais dépasser la jauge définie dans les inputs.
- **Vœux Prioritaires** : Les vœux 1 et 2 sont traités comme des contraintes strictes.
- **Sessions par Étudiant** : Chaque étudiant doit avoir exactement 4 sessions.
- **Vagues (Waves)** :
    - Vague 1 : Temps 1 à 4.
    - Vague 2 : Temps 2 à 5.
- **Diversité** : Limitation du nombre de types d'activités (ex: max 1 Flash Métier).

---

## 🔧 Installation et Développement

### Prérequis
- Docker & Docker Compose
- JDK 17
- Node.js & npm
- Python 3.10

### Lancement via Docker
```bash
docker-compose up --build
```

### Développement Local
1. **Backend** : `mvn spring-boot:run` ou utiliser `start-backend.bat`.
2. **Frontend** : `npm install` puis `npm start` ou utiliser `start-frontend.bat`.
3. **Optimiseur** :
   ```bash
   cd room_attribution
   pip install -r requirements.txt
   python src/main_grouped.py
   ```

---

## 📊 Modèle de Données (H2)

La base de données H2 est utilisée pour la simplicité du POC.
- **Tables clés** : `Etudiant`, `Voeu`, `Presentation`, `Affectation`.
- **Console H2** : Accessible sur `/h2-console` en mode développement.

---

## 🧪 Tests

Les tests sont centralisés dans le dossier `/tests` :
- `backend/` : Tests unitaires et d'intégration JUnit.
- `frontend/` : Tests Jasmine/Karma.
- `algorithm/` : Scripts de vérification de la cohérence des résultats d'optimisation.
