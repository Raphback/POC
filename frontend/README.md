# 🎨 Frontend - FESUP 2026

Application Angular pour la gestion des vœux du Forum FESUP 2026.

## 📋 Prérequis

- **Node.js** 18+ (`node -v`)
- **NPM** 8+ (`npm -v`)

## 🚀 Démarrage

### Installation des dépendances

```bash
npm install
```

### Serveur de développement

```bash
npm start
# ou
ng serve
```

L'application est accessible sur **http://localhost:4200**

## 🏗️ Build

```bash
# Build de production
npm run build

# Les fichiers sont générés dans dist/frontend/
```

## 🧪 Tests

```bash
# Tests unitaires
npm test

# Tests e2e
npm run e2e
```

## 📁 Structure

```
src/
├── app/
│   ├── components/     # Composants Angular
│   │   ├── login/
│   │   ├── admin-dashboard/
│   │   ├── viewer-dashboard/
│   │   ├── voeu-form/
│   │   └── voeu-confirmation/
│   ├── services/       # Services (API calls)
│   ├── models/         # Interfaces TypeScript
│   └── app-routing.module.ts
├── assets/             # Images, fonts
└── styles.css          # Styles globaux
```

## 🎨 Design

L'interface utilise :
- **Bootstrap 5** pour la mise en page
- **CSS custom** avec effets lumineux
- Design "Neon" personnalisé

## 🔗 API Backend

Le frontend communique avec le backend via :
- **Dev local** : `http://localhost:8080`
- **Docker** : proxy nginx vers `http://backend:8080`

---

Voir [README principal](../README.md) pour plus d'infos.
