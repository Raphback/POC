# 📝 Instructions pour Push Git

Voici comment pousser ce projet sur Git une fois terminé.

---

## Option 1 : Nouveau Repository GitHub

### 1. Créer le Repository sur GitHub

1. Aller sur https://github.com
2. Cliquer sur **New repository**
3. Nom : `fesup-2026` (ou autre)
4. Description : "Système de gestion des vœux pour FESUP 2026"
5. **NE PAS** cocher "Initialize with README" (on en a déjà un)
6. Cliquer sur **Create repository**

### 2. Initialiser Git Localement

```powershell
cd "c:/Users/ArthurLemarc_v4i1hl3/OneDrive - Noveo/Bureau/TSE/Semestre 9/POC"

# Initialiser Git
git init

# Ajouter tous les fichiers (le .gitignore exclura automatiquement les fichiers inutiles)
git add .

# Premier commit
git commit -m "Initial commit - FESUP 2026 v1.0"
```

### 3. Lier au Repository GitHub

```powershell
# Remplacer <USERNAME> et <REPO_NAME> par vos valeurs
git remote add origin https://github.com/<USERNAME>/<REPO_NAME>.git

# Créer et pousser sur la branche main
git branch -M main
git push -u origin main
```

---

## Option 2 : Repository Existant

Si vous avez déjà un repo :

```powershell
cd "c:/Users/ArthurLemarc_v4i1hl3/OneDrive - Noveo/Bureau/TSE/Semestre 9/POC"

# Vérifier la branche actuelle
git branch

# Ajouter les modifications
git add .

# Commit
git commit -m "Update: Complete FESUP 2026 application with docs"

# Push
git push origin main
```

---

## ✅ Vérifications Avant Push

### 1. Vérifier le .gitignore

```powershell
git status
```

✅ **Vous NE devriez PAS voir** :
- `node_modules/`
- `target/`
- `*.log`
- `.gemini/`
- `data/`

### 2. Tester le Build

```powershell
# Backend
cd backend
mvn clean package
# ✅ Doit réussir

# Frontend
cd ../frontend
npm run build
# ✅ Doit réussir
```

---

## 📤 Partager avec l'Équipe

Une fois pushé, partager :

```
Repository: https://github.com/<USERNAME>/<REPO_NAME>
Branche: main
Docs: Voir README.md pour l'installation
```

### Commandes pour vos collègues

```bash
# Cloner
git clone https://github.com/<USERNAME>/<REPO_NAME>.git
cd <REPO_NAME>

# Installer & Lancer
# Suivre QUICK_START.md
```

---

## 🌿 Workflow Git Recommandé

### Pour Développer une Nouvelle Fonctionnalité

```bash
# Créer une branche feature
git checkout -b feature/nouvelle-fonctionnalite

# Travailler...
git add .
git commit -m "Add: Description de la feature"

# Pousser la branche
git push origin feature/nouvelle-fonctionnalite

# Sur GitHub: Créer une Pull Request vers main
```

### Branches Recommandées

- `main` : Production (code stable uniquement)
- `develop` : Développement actif
- `feature/*` : Nouvelles fonctionnalités
- `fix/*` : Corrections de bugs
- `docs/*` : Modifications de documentation

---

## 🔒 Sécurité

**⚠️ IMPORTANT** : Ne **JAMAIS** commit :
- Mots de passe ou clés API
- Données personnelles
- Fichiers de configuration sensibles

Le `.gitignore` empêche déjà la plupart des fichiers sensibles d'être inclus.

---

## 📊 État Actuel du Projet

✅ README.md créé  
✅ .gitignore configuré  
✅ Fichiers temporaires supprimés  
✅ Documentation complète  
✅ Code testé et fonctionnel  

**Prêt pour le push !** 🚀
