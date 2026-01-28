# 📖 Guide Utilisateur - FESUP 2026

Bienvenue dans le guide utilisateur de la plateforme FESUP 2026. Ce document explique comment utiliser les différentes fonctionnalités de l'application selon votre rôle.

---

## 🎓 Pour les Étudiants

### 1. Connexion
- Rendez-vous sur [http://localhost:4200](http://localhost:4200).
- Saisissez votre identifiant **INE** (ex: `120890177FA`).
- Cliquez sur "Se connecter". Aucun mot de passe n'est requis pour les étudiants.

### 2. Choix des Vœux
- Une fois connecté, vous verrez la liste des présentations disponibles.
- Vous devez choisir **5 vœux** par ordre de préférence.
- **Important** : Les vœux 1 et 2 sont prioritaires et seront garantis à 100% par l'algorithme d'affectation.
- Une fois vos choix faits, cliquez sur "Valider mes vœux". Un récapitulatif s'affichera.

### 3. Consultation des Résultats
- Après la phase d'optimisation par l'administrateur, reconnectez-vous avec votre INE.
- Votre planning personnalisé s'affichera, indiquant les 4 sessions auxquelles vous êtes affecté, avec les horaires et les numéros de salles.

---

## 🛡️ Pour les Administrateurs

### 1. Accès
- Connectez-vous avec le login `admin` et le mot de passe `admin`.

### 2. Gestion des Données (Import)
- Allez dans l'onglet **Administration** ou **Gestion des Données**.
- Vous pouvez importer les fichiers Excel sources depuis le dossier `Inputs/` :
    - Liste des étudiants.
    - Liste des présentations et capacités des salles.
- Le système gère automatiquement les doublons d'INE.

### 3. Lancement de l'Optimisation
- Une fois la période de saisie des vœux terminée, lancez le moteur d'optimisation.
- L'algorithme Python (basé sur Google OR-Tools) calculera la meilleure répartition possible en respectant toutes les contraintes (capacité, vagues, etc.).

### 4. Statistiques
- Consultez le tableau de bord des statistiques pour vérifier le taux de satisfaction des vœux et le remplissage des salles.

---

## 🏫 Pour les Lycées (Viewers)

### 1. Accès
- Utilisez les identifiants fournis (ex: `prof@fauriel.fr` / `prof`).

### 2. Consultation
- Les viewers peuvent consulter la liste de tous les étudiants de leur établissement et leurs affectations respectives.
- Cela permet un suivi logistique précis le jour de l'événement.

---

## 🆘 Support et Dépannage
- **INE non reconnu** : Contactez l'administrateur pour vérifier si vous êtes bien présent dans la base de données.
- **Problème d'affichage** : Rafraîchissez la page ou videz le cache de votre navigateur.
