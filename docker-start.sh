#!/bin/bash

# Script de démarrage Docker pour POC FESUP 2026

echo "🐳 Démarrage du projet POC FESUP avec Docker..."
echo ""

# Vérifier que Docker est installé
if ! command -v docker &> /dev/null; then
    echo "❌ Docker n'est pas installé. Veuillez installer Docker d'abord."
    exit 1
fi

# Vérifier que Docker Compose est installé
if ! command -v docker-compose &> /dev/null; then
    echo "❌ Docker Compose n'est pas installé. Veuillez installer Docker Compose d'abord."
    exit 1
fi

echo "✅ Docker et Docker Compose sont installés"
echo ""

# Construire et lancer les conteneurs
echo "🔨 Construction des images Docker..."
docker-compose build

echo ""
echo "🚀 Démarrage des services..."
docker-compose up -d

echo ""
echo "⏳ Attente du démarrage des services..."
sleep 5

echo ""
echo "✅ Services démarrés !"
echo ""
echo "📍 Accès aux applications :"
echo "   - Frontend : http://localhost:4200"
echo "   - Backend  : http://localhost:8080"
echo "   - H2 Console : http://localhost:8080/h2-console"
echo ""
echo "📋 Commandes utiles :"
echo "   - Voir les logs : docker-compose logs -f"
echo "   - Arrêter : docker-compose down"
echo "   - Redémarrer : docker-compose restart"
echo ""

