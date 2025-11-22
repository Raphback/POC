# 🚀 Push Git - Instructions Simplifiées

## Étapes Rapides (Git Bash)

1. **Ouvrir Git Bash** dans le dossier POC
   ```
   Clic droit dans le dossier POC → "Git Bash Here"
   ```

2. **Initialiser & Commit**
   ```bash
   git init
   git add .
   git commit -m "Initial commit - FESUP 2026 v1.0"
   ```

3. **Push sur GitHub**
   ```bash
   # Remplacer <USERNAME> par votre nom GitHub
   git remote add origin https://github.com/<USERNAME>/fesup-2026.git
   git branch -M main
   git push -u origin main
   ```

## Alternative : Créer le Repo GitHub d'abord

1. Aller sur https://github.com/new
2. Nom : `fesup-2026`
3. **NE PAS** cocher "Initialize with README"
4. Créer le repository
5. Copier l'URL affichée
6. Dans Git Bash :
   ```bash
   git init
   git add .
   git commit -m "Initial commit - FESUP 2026 v1.0"
   git remote add origin <URL_COPIEE>
   git push -u origin main
   ```

## ✅ C'est tout !

Vos collègues cloneront avec :
```bash
git clone https://github.com/<USERNAME>/fesup-2026.git
```

Et suivront le README.md pour installer.
