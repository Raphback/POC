"""
Module d'exportation des resultats FESUP 2026 (simplifie).
"""

from typing import List
import os
import pandas as pd
import numpy as np

from models import Solution, Student, Room
from config import get_slot_time, N_CONFERENCES


def _sort_presentation(name: str) -> tuple:
    """Cle de tri pour les presentations."""
    if name.startswith('Conf'):
        return (0, int(name[4:]))
    elif name.startswith('TR'):
        return (1, int(name[2:]))
    elif name.startswith('FM'):
        return (2, int(name[2:]))
    return (3, 0)


def export_presenters(solution: Solution, rooms: List[Room],
                     pres_names: List[str], output_path: str, half_day: int = 0) -> pd.DataFrame:
    """Exporte le planning pour les presentateurs."""
    as_, ae = solution.as_, solution.ae
    P, S, T = as_.shape

    data = []
    for p in range(P):
        row = {'Presentation': pres_names[p]}
        for t in range(T):
            col = f'Slot {t} ({get_slot_time(half_day, t)})'
            room = next((rooms[s].name for s in range(S) if as_[p, s, t] > 0.5), None)
            n = int(np.sum(ae[:, p, t]))
            row[col] = f"{room} ({n} el.)" if room and n > 0 else "-"
        data.append(row)

    df = pd.DataFrame(data)
    df['_sort'] = df['Presentation'].apply(_sort_presentation)
    df = df.sort_values('_sort').drop('_sort', axis=1)
    df.to_excel(output_path, index=False)
    return df


def export_students(solution: Solution, students: List[Student], rooms: List[Room],
                   pres_names: List[str], output_path: str, half_day: int = 0) -> pd.DataFrame:
    """Exporte le planning pour les eleves."""
    ae, as_ = solution.ae, solution.as_
    E, P, T = ae.shape

    data = []
    for e, student in enumerate(students):
        valid = set(student.get_valid_slots())
        row = {
            'Eleve_ID': student.id,
            'Creneau': student.timeslot,
            'Arrivee': "Tot" if student.timeslot % 2 == 0 else "Tard"
        }

        for t in range(T):
            if t not in valid:
                row[f'Slot_{t}_Pres'] = "(absent)"
                row[f'Slot_{t}_Salle'] = "-"
                continue

            pres = next((pres_names[p] for p in range(P) if ae[e, p, t] > 0.5), "?")
            room = "?"
            for p in range(P):
                if ae[e, p, t] > 0.5:
                    room = next((rooms[s].name for s in range(len(rooms)) if as_[p, s, t] > 0.5), "?")
                    break
            row[f'Slot_{t}_Pres'] = pres
            row[f'Slot_{t}_Salle'] = room
        data.append(row)

    df = pd.DataFrame(data)
    df.to_excel(output_path, index=False)
    return df


def export_statistics(solution: Solution, students: List[Student],
                     pres_names: List[str], output_path: str, half_day: int = 0) -> pd.DataFrame:
    """Exporte les statistiques."""
    ae = solution.ae
    E, P, T = ae.shape

    # Stats par voeu
    voeu_stats = {i: 0 for i in range(1, 6)}
    for e, s in enumerate(students):
        valid = s.get_valid_slots()
        for i, v in enumerate(s.voeux):
            if sum(ae[e, v, t] for t in valid) > 0.5:
                voeu_stats[i + 1] += 1

    # Stats par presentation
    pres_stats = []
    for p in range(P):
        total = int(np.sum(ae[:, p, :]))
        sessions = sum(1 for t in range(T) if np.sum(ae[:, p, t]) > 0)
        ptype = "Conference" if p < N_CONFERENCES else ("Table Ronde" if p < N_CONFERENCES + 6 else "Flash Metier")
        pres_stats.append({'Presentation': pres_names[p], 'Type': ptype,
                         'Total_eleves': total, 'Sessions': sessions})

    # General stats
    data = [
        {'Statistique': 'Demi-journee', 'Valeur': str(half_day)},
        {'Statistique': 'Status', 'Valeur': solution.status},
        {'Statistique': 'Objectif', 'Valeur': f"{solution.objective_value:.0f}"},
        {'Statistique': 'Temps', 'Valeur': f"{solution.computation_time:.2f}s"},
        {'Statistique': 'Eleves', 'Valeur': str(E)},
    ]
    for i in range(1, 6):
        pct = 100 * voeu_stats[i] / E if E > 0 else 0
        data.append({'Statistique': f'Voeu {i}', 'Valeur': f"{voeu_stats[i]} ({pct:.1f}%)"})

    with pd.ExcelWriter(output_path) as writer:
        pd.DataFrame(data).to_excel(writer, sheet_name='Statistiques', index=False)
        pd.DataFrame(pres_stats).to_excel(writer, sheet_name='Presentations', index=False)

    return pd.DataFrame(data)


def export_all(solution: Solution, students: List[Student], rooms: List[Room],
              pres_names: List[str], output_dir: str, half_day: int = 0):
    """Exporte tous les resultats."""
    os.makedirs(output_dir, exist_ok=True)

    export_presenters(solution, rooms, pres_names,
                     os.path.join(output_dir, "planning_presentateurs.xlsx"), half_day)
    export_students(solution, students, rooms, pres_names,
                   os.path.join(output_dir, "planning_eleves.xlsx"), half_day)
    export_statistics(solution, students, pres_names,
                     os.path.join(output_dir, "statistiques.xlsx"), half_day)

    print(f"Resultats exportes dans {output_dir}/")
