# FESUP 2026 Optimizer

Algorithme d'optimisation pour l'affectation des élèves aux présentations lors de l'événement FESUP 2026.

## 📋 Vue d'ensemble

**Objectif:** Affecter 4,377 élèves à des présentations FESUP 2026 en respectant leurs voeux et les contraintes de capacité.

**Méthode:** Programmation par contraintes (CP-SAT) avec le solveur Google OR-Tools

**Stratégie:** Optimisation par demi-journée (4 optimisations séparées)

## 📊 Données

| Élément | Quantité |
|---------|----------|
| Élèves | 4,377 répartis en 8 créneaux → 4 demi-journées |
| Présentations | 31 (19 conférences + 6 tables rondes + 6 flash-métiers) |
| Salles | 22 avec capacité totale ~1,265 places (+20% ajouté) |
| Temps de calcul | ~1h pour les 4 demi-journées |

### Structure des créneaux

| Demi-journée | Date | Horaire | Élèves |
|--------------|------|---------|--------|
| 0 | 26/03 | 08h30-12h00 | ~1050 |
| 1 | 26/03 | 13h30-17h00 | ~1050 |
| 2 | 27/03 | 08h30-12h00 | ~1100 |
| 3 | 27/03 | 13h30-17h00 | ~1150 |

Chaque demi-journée contient 2 groupes d'élèves :
- **Groupe tôt** : arrive au début, assiste aux slots 0-3
- **Groupe tard** : arrive 45min après, assiste aux slots 1-4

## 🔒 Contraintes

### Contraintes strictes (obligatoires)

1. ✅ **Voeu 1 respecté** à 100%
2. ✅ **Voeu 2 respecté** à 100%
3. ✅ **4 présentations par élève** issues des 5 voeux
4. ✅ **Un slot = une présentation** par élève
5. ✅ **Pas de répétition** de présentation
6. ✅ **Capacités des salles respectées**
7. ✅ **Une salle max par présentation** par slot
8. ✅ **Max 1 Table Ronde** par élève
9. ✅ **Max 1 Flash Métier** par élève

### Fonction objectif

Minimiser l'utilisation du voeu #5 (on préfère les voeux 1-4).

## 📁 Structure du projet

```
achart/
├── src/                          # Code source Python
│   ├── main_grouped.py           # Script principal d'optimisation
│   ├── fesup_optimizer_ortools.py # Algorithme CP-SAT
│   ├── fesup_optimizer.py        # Module alias
│   ├── data_loader.py            # Chargement des données
│   ├── result_exporter.py        # Export des résultats
│   ├── timeslots_mapping.py      # Mapping créneaux horaires
│   └── verify_results.py         # Vérification des résultats
├── resultats/                    # Résultats générés
│   ├── demi_journee_0/
│   ├── demi_journee_1/
│   ├── demi_journee_2/
│   └── demi_journee_3/
├── venv/                         # Environnement virtuel Python
└── README.md
```

## 🚀 Utilisation

### 1. Installation

```bash
cd achart
python3 -m venv venv
source venv/bin/activate
pip install -r requirements.txt
```

### 2. Lancer l'optimisation

```bash
source venv/bin/activate
python src/main_grouped.py
```

**Durée:** ~1 heure pour les 4 demi-journées

### 3. Vérifier les résultats

```bash
python src/verify_results.py
```

Options :
- `python src/verify_results.py -d 0` - Vérifier uniquement la demi-journée 0
- `python src/verify_results.py -v` - Mode verbose

## 📊 Fichiers de sortie

### planning_presentateurs.xlsx

Planning pour chaque présentation avec salle et nombre d'élèves :

| Présentation | Slot 0 (08h30) | Slot 1 (09h15) | ... |
|--------------|----------------|----------------|-----|
| Conf1 | J021 (45 él.) | - | ... |
| Conf2 | A013 (22 él.) | J022 (38 él.) | ... |

### planning_eleves.xlsx

Planning individuel de chaque élève :

| Élève_ID | Créneau | Arrivée | Slot_0_Pres | Slot_0_Salle | ... |
|----------|---------|---------|-------------|--------------|-----|
| 0 | 0 | Tôt | Conf5 | J021 | ... |
| 1 | 1 | Tard | (absent) | - | ... |

### statistiques.xlsx

- Status de l'optimisation (OPTIMAL/FEASIBLE)
- Temps de calcul
- Statistiques des voeux utilisés
- Répartition des combinaisons (4 conf, 3 conf + 1 TR, etc.)

## ⚠️ Statuts possibles

| Status | Signification |
|--------|---------------|
| ✅ OPTIMAL | Solution optimale trouvée |
| ⚠️ FEASIBLE | Solution trouvée mais potentiellement non optimale |
| ❌ INFEASIBLE | Aucune solution possible (contraintes incompatibles) |

## 🔧 Configuration

### Ajuster la capacité des salles

Dans `src/data_loader.py`, ligne ~170 :
```python
capacity = int(base_capacity * 1.20)  # +20% de marge
```

### Ajuster le temps limite

Dans `src/fesup_optimizer_ortools.py`, méthode `optimize()` :
```python
solver.parameters.max_time_in_seconds = 600  # 10 minutes
```

## 📖 Dépendances

- Python 3.8+
- ortools >= 9.0
- pandas >= 2.0.0
- numpy >= 1.24.0
- openpyxl >= 3.1.0

## 👤 Auteur

Implémenté avec Claude Code (Anthropic) pour FESUP 2026.
