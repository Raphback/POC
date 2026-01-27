# 📝 Guide Git - POC FESUP 2026

Ce guide explique comment gérer le versioning Git du projet.

---

## 🚀 Push sur une Branche Existante

### Avec Git Bash

```bash
# 1. Se placer dans le dossier
cd "/d/Documents/Télécom st étienne/Cours/Semestre 9/POC"

# 2. Vérifier le statut
git status

# 3. Changer de branche
git checkout <nom-branche>

# 4. Ajouter les fichiers modifiés
git add .

# 5. Commit avec message descriptif
git commit -m "Description des modifications"

# 6. Push
git push origin <nom-branche>
```

---

## 🆕 Créer un Nouveau Repository

### 1. Sur GitHub

1. Aller sur https://github.com/new
2. Nom : `fesup-2026` (ou POC)
3. **NE PAS** cocher "Initialize with README"
4. Créer le repository

### 2. En local (Git Bash)

```bash
cd "/d/Documents/Télécom st étienne/Cours/Semestre 9/POC"

git init
git add .
git commit -m "Initial commit - FESUP 2026 v1.0"
git remote add origin https://github.com/<USERNAME>/<REPO>.git
git branch -M main
git push -u origin main
```

---

## ✅ Vérifications Avant Push

### Fichiers à NE PAS commit

Le `.gitignore` exclut automatiquement :
- ❌ `node_modules/`
- ❌ `target/`
- ❌ `*.log`
- ❌ `.gemini/`
- ❌ `data/`

Vérifiez avec `git status` que ces dossiers n'apparaissent pas.

### Tester le Build

```bash
# Backend
cd backend && mvn clean package

# Frontend
cd frontend && npm run build
```

---

## 🌿 Workflow Git Recommandé

### Branches

| Branche | Usage |
|---------|-------|
| `main` | Production (code stable) |
| `develop` | Développement actif |
| `feature/*` | Nouvelles fonctionnalités |
| `fix/*` | Corrections de bugs |
| `backend-*` | Modifications backend |
| `frontend-*` | Modifications frontend |

### Exemple de Feature

```bash
# Créer une branche
git checkout -b feature/nouvelle-fonctionnalite

# Travailler et commiter
git add .
git commit -m "Add: Description de la feature"

# Pousser
git push origin feature/nouvelle-fonctionnalite

# Sur GitHub : créer une Pull Request vers main/develop
```

---

## 🔒 Sécurité

**⚠️ JAMAIS commit** :
- Mots de passe / clés API
- Données personnelles
- Fichiers de config avec secrets

---

## 📊 État du Projet

✅ README.md à jour  
✅ .gitignore configuré  
✅ Documentation complète  
✅ Gestion des doublons INE  
✅ Docker Compose fonctionnel  

**Prêt pour le push !** 🚀
