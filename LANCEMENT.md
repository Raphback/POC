# 🚀 Guide de Démarrage - POC FESUP 2026

## ✅ Installation Terminée !

Tout est configuré automatiquement dans votre répertoire utilisateur :
- ☕ **Java 17** : `C:\Users\ArthurLemarc_v4i1hl3\.java\jdk-17.0.2`
- 📦 **Maven 3.9.5** : `C:\Users\ArthurLemarc_v4i1hl3\.maven\apache-maven-3.9.5`  
- 🗄️ **Base de données H2** : Embarquée (aucune installation)
- 🅰️ **Angular CLI** : Installé via npm

---

## 🎯 Démarrage Rapide

### Option 1 : Utiliser les scripts automatiques

**Terminal 1 - Backend** :
```powershell
cd "c:\Users\ArthurLemarc_v4i1hl3\OneDrive - Noveo\Bureau\TSE\Semestre 9\POC\backend"
.\start-backend.ps1
```

**Terminal 2 - Frontend** :
```powershell
cd "c:\Users\ArthurLemarc_v4i1hl3\OneDrive - Noveo\Bureau\TSE\Semestre 9\POC\frontend"
.\start-frontend.ps1
```

### Option 2 : Commandes manuelles

**Backend** :
```powershell
cd backend
$env:JAVA_HOME = "$env:USERPROFILE\.java\jdk-17.0.2"
$env:M2_HOME = "$env:USERPROFILE\.maven\apache-maven-3.9.5"
$env:PATH = "$env:JAVA_HOME\bin;$env:M2_HOME\bin;$env:PATH"
mvn spring-boot:run
```

**Frontend** :
```powershell
cd frontend
npm start
```

---

## 🌐 Accès aux Applications

- **Frontend** : http://localhost:4200
- **Backend API** : http://localhost:8080
- **Console H2** (Base de données) : http://localhost:8080/h2-console
  - JDBC URL : `jdbc:h2:file:./data/fesup_db`
  - Username : `sa`
  - Password : (vide)

---

## 🧪 Test du Parcours Complet

1. **Ouvrir** : http://localhost:4200
2. **Se connecter** avec l'étudiant de test :
   - **Matricule** : `12345`
   - **Nom** : `Doe`
3. **Saisir les vœux** (Conférences + Options)
4. **Admin Dashboard** : http://localhost:4200/admin
5. **Lancer l'algorithme** d'affectation
6. **Télécharger le PDF** des convocations

---

## 🔧 Dépannage

### "mvn n'est pas reconnu"
Relancez le script `start-backend.ps1` qui configure automatiquement les variables d'environnement.

### "Port 4200 déjà utilisé"
Arrêtez les anciens serveurs : `Stop-Process -Name "node" -Force`

### "javac n'est pas reconnu"
Vérifiez que Java est bien installé : 
```powershell
$env:JAVA_HOME = "$env:USERPROFILE\.java\jdk-17.0.2"
java -version
```

---

## 📊 Structure de la Base de Données

H2 stocke les données dans : `backend/data/fesup_db.mv.db`

Les données persistent entre les redémarrages !

---

Bon coding ! 🎓
