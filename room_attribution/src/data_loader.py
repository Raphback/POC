"""
Module de chargement des données FESUP 2026

Ce module lit les fichiers d'entrée (CSV et Excel) et construit les structures
de données nécessaires à l'optimisation.
"""

from typing import List, Tuple, Dict
import pandas as pd
from datetime import datetime
from fesup_optimizer import Student, Room
from timeslots_mapping import get_timeslot_for_school


def load_presentations_mapping() -> Tuple[Dict[str, int], List[str]]:
    """Crée le mapping des noms de présentations vers des indices.

    Returns:
        Tuple (mapping, presentation_names) où:
        - mapping: Dict nom_presentation -> index
        - presentation_names: Liste des noms de présentations
    """
    # Définir toutes les présentations possibles
    # 19 conférences + 6 tables rondes + 6 flash-métiers = 31 présentations
    conferences = [f"Conf{i}" for i in range(1, 20)]
    tables_rondes = [f"TR{i}" for i in range(1, 7)]
    flash_metiers = [f"FM{i}" for i in range(1, 7)]

    presentation_names = conferences + tables_rondes + flash_metiers

    # Créer le mapping nom -> index
    mapping = {name: idx for idx, name in enumerate(presentation_names)}

    return mapping, presentation_names


def load_students_from_csv(csv_path: str) -> Tuple[List[Student], Dict[str, int], List[str]]:
    """Charge les élèves depuis le fichier CSV.

    Args:
        csv_path: Chemin vers le fichier Eleves_Voeux.csv

    Returns:
        Tuple (students, pres_mapping, pres_names) où:
        - students: Liste des élèves chargés
        - pres_mapping: Mapping nom_presentation -> index
        - pres_names: Liste des noms de présentations
    """
    print(f"\nChargement des élèves depuis {csv_path}...")

    # Charger le CSV (délimiteur = tabulation)
    # Essayer plusieurs encodages
    for encoding in ['utf-8', 'latin-1', 'iso-8859-1', 'cp1252']:
        try:
            df = pd.read_csv(csv_path, sep='\t', encoding=encoding)
            print(f"  - Encodage détecté: {encoding}")
            break
        except UnicodeDecodeError:
            continue
    else:
        raise ValueError("Impossible de détecter l'encodage du fichier CSV")

    print(f"  - {len(df)} lignes chargées")

    # Créer le mapping des présentations
    pres_mapping, pres_names = load_presentations_mapping()

    # Déterminer les vagues selon la date
    # 26/03/2026 = vague 1, 27/03/2026 = vague 2
    students = []

    for idx, row in df.iterrows():
        # Extraire les voeux
        voeu_cols = ['Voeu 1', 'Voeu 2', 'Voeu 3', 'Voeu 4', 'Voeu 5']
        voeux_names = [str(row[col]).strip() for col in voeu_cols]

        # Convertir en indices
        voeux_indices = []
        for voeu_name in voeux_names:
            if voeu_name in pres_mapping:
                voeux_indices.append(pres_mapping[voeu_name])
            else:
                print(f"  ⚠️  Voeu inconnu: {voeu_name} pour élève {idx}")
                # Utiliser la première présentation par défaut
                voeux_indices.append(0)

        # Déterminer la vague selon la date (deprecated)
        date_str = str(row['Date']).strip()
        if '26/03/2026' in date_str or date_str.startswith('26'):
            vague = 1
        else:
            vague = 2

        # Déterminer le créneau horaire selon la date et l'heure (pas le nom de l'établissement)
        # Car certains établissements ont des élèves à plusieurs créneaux
        etablissement = str(row['Etablissement']).strip()
        date = str(row['Date']).strip()
        heure = str(row['Heure']).strip()
        timeslot = get_timeslot_for_school(etablissement, date, heure)

        student = Student(
            id=idx,
            voeux=voeux_indices,
            vague=vague,
            timeslot=timeslot
        )
        students.append(student)

    # Compter les créneaux
    timeslot_counts = {}
    for s in students:
        timeslot_counts[s.timeslot] = timeslot_counts.get(s.timeslot, 0) + 1

    print(f"  - {len(students)} élèves chargés")
    print(f"  - Répartition par créneau:")
    for ts in sorted(timeslot_counts.keys()):
        if ts >= 0:
            print(f"    Créneau {ts}: {timeslot_counts[ts]} élèves")
        else:
            print(f"    ⚠️  Non assignés: {timeslot_counts[ts]} élèves")

    return students, pres_mapping, pres_names


def load_rooms_from_excel(excel_path: str) -> List[Room]:
    """Charge les salles depuis le fichier Excel.

    Args:
        excel_path: Chemin vers le fichier capacites.xlsx

    Returns:
        Liste des salles chargées
    """
    print(f"\nChargement des salles depuis {excel_path}...")

    # Charger l'Excel
    df = pd.read_excel(excel_path)

    print(f"  - {len(df)} lignes chargées")
    print(f"  - Colonnes: {df.columns.tolist()}")

    # Trouver les colonnes pertinentes
    # On cherche des colonnes comme "Salle", "Nom", "Capacité", etc.
    room_col = None
    capacity_col = None

    for col in df.columns:
        col_lower = str(col).lower()
        if 'salle' in col_lower or 'nom' in col_lower or 'name' in col_lower:
            room_col = col
        if 'capacit' in col_lower or 'capacity' in col_lower:
            capacity_col = col

    if room_col is None or capacity_col is None:
        # Essayer avec les indices de colonnes
        print("  ⚠️  Colonnes non détectées automatiquement, utilisation des indices")
        if len(df.columns) >= 2:
            room_col = df.columns[0]
            capacity_col = df.columns[1]
        else:
            raise ValueError("Impossible de détecter les colonnes salle et capacité")

    print(f"  - Colonne salle: {room_col}")
    print(f"  - Colonne capacité: {capacity_col}")

    # Créer les objets Room avec +10% de capacité (pour compenser ~10% d'absences)
    # avec seulement 10% de marge en plus le problème n'est pas optimisable on va donc essayer de résoudre le problèmes avec 20% de marge 
    rooms = []
    for idx, row in df.iterrows():
        room_name = str(row[room_col]).strip()
        base_capacity = int(row[capacity_col])
        # Ajouter 20% de capacité supplémentaire pour compenser les absences moyennes
        capacity = int(base_capacity * 1.20)

        room = Room(
            id=idx,
            name=room_name,
            capacity=capacity
        )
        rooms.append(room)

    total_capacity = sum(r.capacity for r in rooms)
    print(f"  - {len(rooms)} salles chargées")
    print(f"  - Capacité totale: {total_capacity} places (+10% ajouté)")

    return rooms


def load_data(students_csv: str, rooms_excel: str) -> Tuple[List[Student], List[Room], int, Dict[str, int], List[str]]:
    """Charge toutes les données nécessaires à l'optimisation.

    Args:
        students_csv: Chemin vers Eleves_Voeux.csv
        rooms_excel: Chemin vers capacites.xlsx

    Returns:
        Tuple (students, rooms, n_presentations, pres_mapping, pres_names)
    """
    print("\n" + "="*60)
    print("CHARGEMENT DES DONNÉES FESUP 2026")
    print("="*60)

    # Charger les élèves
    students, pres_mapping, pres_names = load_students_from_csv(students_csv)

    # Charger les salles
    rooms = load_rooms_from_excel(rooms_excel)

    # Nombre de présentations
    n_presentations = len(pres_names)

    print(f"\n  ✓ Données chargées avec succès")
    print(f"  - {len(students)} élèves")
    print(f"  - {len(rooms)} salles")
    print(f"  - {n_presentations} présentations")
    print("="*60)

    return students, rooms, n_presentations, pres_mapping, pres_names
