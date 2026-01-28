"""
Module de chargement des donnees FESUP 2026 (simplifie).
"""

from typing import List, Tuple, Dict
import pandas as pd

from models import Student, Room
from config import (
    PRESENTATION_MAPPING, PRESENTATION_NAMES, N_PRESENTATIONS,
    CAPACITY_BUFFER, get_timeslot
)


def _read_csv(path: str) -> pd.DataFrame:
    """Lit un CSV en testant plusieurs encodages."""
    for enc in ['utf-8', 'latin-1', 'iso-8859-1', 'cp1252']:
        try:
            return pd.read_csv(path, sep='\t', encoding=enc)
        except UnicodeDecodeError:
            continue
    raise ValueError(f"Impossible de lire {path}")


def load_students(csv_path: str) -> List[Student]:
    """Charge les eleves depuis le fichier CSV."""
    df = _read_csv(csv_path)
    students = []

    for idx, row in df.iterrows():
        voeux = [
            PRESENTATION_MAPPING.get(str(row[f'Voeu {i}']).strip(), 0)
            for i in range(1, 6)
        ]
        timeslot = get_timeslot(str(row['Date']).strip(), str(row['Heure']).strip())
        vague = 1 if '26/03/2026' in str(row['Date']) else 2

        students.append(Student(id=idx, voeux=voeux, vague=vague, timeslot=timeslot))

    print(f"Charge {len(students)} eleves")
    return students


def load_rooms(excel_path: str) -> List[Room]:
    """Charge les salles depuis le fichier Excel."""
    df = pd.read_excel(excel_path)

    # Detecter colonnes
    room_col = next((c for c in df.columns if 'salle' in c.lower() or 'nom' in c.lower()), df.columns[0])
    cap_col = next((c for c in df.columns if 'capacit' in c.lower()), df.columns[1])

    rooms = [
        Room(id=idx, name=str(row[room_col]).strip(),
             capacity=int(row[cap_col] * CAPACITY_BUFFER))
        for idx, row in df.iterrows()
    ]

    print(f"Charge {len(rooms)} salles, capacite totale: {sum(r.capacity for r in rooms)}")
    return rooms


def load_data(students_csv: str, rooms_excel: str) -> Tuple[List[Student], List[Room], int, Dict[str, int], List[str]]:
    """Charge toutes les donnees."""
    students = load_students(students_csv)
    rooms = load_rooms(rooms_excel)
    return students, rooms, N_PRESENTATIONS, PRESENTATION_MAPPING, list(PRESENTATION_NAMES)
