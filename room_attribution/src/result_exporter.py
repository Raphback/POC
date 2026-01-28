"""
Module d'exportation des résultats FESUP 2026

Ce module exporte les résultats de l'optimisation dans différents formats
pour les présentateurs et les élèves.

Structure des slots par demi-journée:
- Matin: Slot 0 (08h30), Slot 1 (09h15), Slot 2 (10h00), Slot 3 (10h45), Slot 4 (11h30)
- Après-midi: Slot 0 (13h30), Slot 1 (14h15), Slot 2 (15h00), Slot 3 (15h45), Slot 4 (16h30)

Les élèves n'utilisent que 4 slots sur 5:
- Arrivée tôt (créneaux pairs): slots 0, 1, 2, 3
- Arrivée tard (créneaux impairs): slots 1, 2, 3, 4
"""

from typing import List, Dict
import pandas as pd
import numpy as np
from fesup_optimizer import Solution, Student, Room


# Horaires des slots selon la demi-journée
SLOT_TIMES_MATIN = {
    0: "08h30-09h15",
    1: "09h15-10h00",
    2: "10h00-10h45",
    3: "10h45-11h30",
    4: "11h30-12h15"
}

SLOT_TIMES_APREM = {
    0: "13h30-14h15",
    1: "14h15-15h00",
    2: "15h00-15h45",
    3: "15h45-16h30",
    4: "16h30-17h15"
}


def get_slot_time(half_day: int, slot: int) -> str:
    """Retourne l'horaire d'un slot selon la demi-journée."""
    if half_day % 2 == 0:  # Matin (demi-journées 0 et 2)
        return SLOT_TIMES_MATIN.get(slot, f"Slot {slot}")
    else:  # Après-midi (demi-journées 1 et 3)
        return SLOT_TIMES_APREM.get(slot, f"Slot {slot}")


def export_schedule_for_presenters(
    solution: Solution,
    rooms: List[Room],
    presentation_names: List[str],
    output_path: str,
    half_day: int = 0
) -> pd.DataFrame:
    """Exporte le planning pour les présentateurs.

    Args:
        solution: Solution de l'optimisation
        rooms: Liste des salles
        presentation_names: Noms des présentations
        output_path: Chemin du fichier de sortie
        half_day: Numéro de la demi-journée (0-3)

    Returns:
        DataFrame du planning
    """
    print(f"\nExportation du planning pour les présentateurs...")

    as_ = solution.as_
    ae = solution.ae
    P, S, T = as_.shape

    # Créer le DataFrame
    data = []

    for p in range(P):
        row = {
            'Présentation': presentation_names[p]
        }

        for t in range(T):
            slot_time = get_slot_time(half_day, t)
            col_name = f'Slot {t} ({slot_time})'

            # Trouver la salle affectée
            room_assigned = None
            for s, room in enumerate(rooms):
                if as_[p, s, t] > 0.5:
                    room_assigned = room.name
                    break

            # Compter le nombre d'élèves
            n_students = int(np.sum(ae[:, p, t]))

            if room_assigned and n_students > 0:
                row[col_name] = f"{room_assigned} ({n_students} él.)"
            else:
                row[col_name] = "-"

        data.append(row)

    df = pd.DataFrame(data)

    # Trier par type de présentation (Conf, TR, FM)
    def sort_key(pres_name):
        if pres_name.startswith('Conf'):
            return (0, int(pres_name[4:]))
        elif pres_name.startswith('TR'):
            return (1, int(pres_name[2:]))
        elif pres_name.startswith('FM'):
            return (2, int(pres_name[2:]))
        return (3, 0)

    df['_sort'] = df['Présentation'].apply(sort_key)
    df = df.sort_values('_sort').drop('_sort', axis=1)

    # Exporter
    if output_path.endswith('.xlsx'):
        df.to_excel(output_path, index=False)
    else:
        df.to_csv(output_path, index=False, encoding='utf-8')

    print(f"  ✓ Planning présentateurs exporté vers {output_path}")

    return df


def export_schedule_for_students(
    solution: Solution,
    students: List[Student],
    rooms: List[Room],
    presentation_names: List[str],
    output_path: str,
    half_day: int = 0
) -> pd.DataFrame:
    """Exporte le planning pour les élèves.

    Args:
        solution: Solution de l'optimisation
        students: Liste des élèves
        rooms: Liste des salles
        presentation_names: Noms des présentations
        output_path: Chemin du fichier de sortie
        half_day: Numéro de la demi-journée (0-3)

    Returns:
        DataFrame du planning
    """
    print(f"\nExportation du planning pour les élèves...")

    ae = solution.ae
    as_ = solution.as_
    E, P, T = ae.shape

    # Créer le DataFrame
    data = []

    for e, student in enumerate(students):
        # Déterminer les slots valides pour cet élève
        valid_slots = set(student.get_valid_slots())
        arrival_type = "Tôt" if student.timeslot % 2 == 0 else "Tard"

        row = {
            'Élève_ID': student.id,
            'Créneau': student.timeslot,
            'Arrivée': arrival_type
        }

        # Pour chaque slot
        presentations_assigned = []
        for t in range(T):
            slot_time = get_slot_time(half_day, t)

            if t not in valid_slots:
                # L'élève n'est pas présent à ce slot
                row[f'Slot_{t}_Pres'] = "(absent)"
                row[f'Slot_{t}_Salle'] = "-"
                continue

            # Trouver la présentation affectée
            pres_assigned = None
            room_assigned = None

            for p in range(P):
                if ae[e, p, t] > 0.5:
                    pres_assigned = presentation_names[p]
                    presentations_assigned.append(pres_assigned)

                    # Trouver la salle
                    for s, room in enumerate(rooms):
                        if as_[p, s, t] > 0.5:
                            room_assigned = room.name
                            break
                    break

            row[f'Slot_{t}_Pres'] = pres_assigned if pres_assigned else "?"
            row[f'Slot_{t}_Salle'] = room_assigned if room_assigned else "?"

        data.append(row)

    df = pd.DataFrame(data)

    # Exporter
    if output_path.endswith('.xlsx'):
        df.to_excel(output_path, index=False)
    else:
        df.to_csv(output_path, index=False, encoding='utf-8')

    print(f"  ✓ Planning élèves exporté vers {output_path}")

    return df


def export_statistics(
    solution: Solution,
    students: List[Student],
    presentation_names: List[str],
    output_path: str,
    half_day: int = 0
) -> pd.DataFrame:
    """Exporte des statistiques sur la solution.

    Args:
        solution: Solution de l'optimisation
        students: Liste des élèves
        presentation_names: Noms des présentations
        output_path: Chemin du fichier de sortie
        half_day: Numéro de la demi-journée (0-3)

    Returns:
        DataFrame des statistiques
    """
    print(f"\nExportation des statistiques...")

    ae = solution.ae
    E, P, T = ae.shape

    # Statistiques par voeu
    voeu_stats = {i: 0 for i in range(1, 6)}

    for e, student in enumerate(students):
        valid_slots = student.get_valid_slots()
        for i, voeu in enumerate(student.voeux):
            count = sum(ae[e, voeu, t] for t in valid_slots)
            if count > 0.5:
                voeu_stats[i + 1] += 1

    # Statistiques par type de présentation
    conf_students = 0
    trfm_students = 0
    for e, student in enumerate(students):
        valid_slots = student.get_valid_slots()
        for p in range(P):
            count = sum(ae[e, p, t] for t in valid_slots)
            if count > 0.5:
                if p < 19:  # Conférences
                    conf_students += 1
                else:  # TR/FM
                    trfm_students += 1

    # Statistiques par présentation
    pres_stats = []
    for p in range(P):
        total_students = int(np.sum(ae[:, p, :]))
        sessions_with_students = sum(1 for t in range(T) if np.sum(ae[:, p, t]) > 0)

        pres_type = "Conférence" if p < 19 else ("Table Ronde" if p < 25 else "Flash Métier")

        pres_stats.append({
            'Présentation': presentation_names[p],
            'Type': pres_type,
            'Total élèves': total_students,
            'Sessions actives': sessions_with_students,
        })

    # Créer le DataFrame de statistiques générales
    data = []
    data.append({'Statistique': 'Demi-journée', 'Valeur': str(half_day)})
    data.append({'Statistique': 'Status', 'Valeur': solution.status})
    data.append({'Statistique': 'Objectif', 'Valeur': f"{solution.objective_value:.0f}"})
    data.append({'Statistique': 'Temps de calcul', 'Valeur': f"{solution.computation_time:.2f}s"})
    data.append({'Statistique': '', 'Valeur': ''})
    data.append({'Statistique': 'Nombre d\'élèves', 'Valeur': str(E)})
    data.append({'Statistique': '', 'Valeur': ''})

    # Stats voeux
    for i in range(1, 6):
        percentage = (voeu_stats[i] / E) * 100 if E > 0 else 0
        voeu_type = "Conférence" if i <= 3 else "TR/FM"
        data.append({
            'Statistique': f'Voeu {i} utilisé ({voeu_type})',
            'Valeur': f"{voeu_stats[i]} ({percentage:.1f}%)"
        })

    data.append({'Statistique': '', 'Valeur': ''})
    data.append({'Statistique': 'Conférences suivies (total)', 'Valeur': str(conf_students)})
    data.append({'Statistique': 'TR/FM suivis (total)', 'Valeur': str(trfm_students)})
    data.append({'Statistique': 'Moyenne conf/élève', 'Valeur': f"{conf_students/E:.2f}" if E > 0 else "0"})
    data.append({'Statistique': 'Moyenne TR-FM/élève', 'Valeur': f"{trfm_students/E:.2f}" if E > 0 else "0"})

    df_stats = pd.DataFrame(data)
    df_pres = pd.DataFrame(pres_stats)

    # Exporter
    if output_path.endswith('.xlsx'):
        with pd.ExcelWriter(output_path) as writer:
            df_stats.to_excel(writer, sheet_name='Statistiques', index=False)
            df_pres.to_excel(writer, sheet_name='Par Présentation', index=False)
    else:
        df_stats.to_csv(output_path.replace('.csv', '_general.csv'), index=False, encoding='utf-8')
        df_pres.to_csv(output_path.replace('.csv', '_presentations.csv'), index=False, encoding='utf-8')

    print(f"  ✓ Statistiques exportées vers {output_path}")

    return df_stats


def export_all_results(
    solution: Solution,
    students: List[Student],
    rooms: List[Room],
    presentation_names: List[str],
    output_dir: str = ".",
    half_day: int = 0
):
    """Exporte tous les résultats dans différents fichiers.

    Args:
        solution: Solution de l'optimisation
        students: Liste des élèves
        rooms: Liste des salles
        presentation_names: Noms des présentations
        output_dir: Répertoire de sortie
        half_day: Numéro de la demi-journée (0-3)
    """
    print("\n" + "="*60)
    print("EXPORTATION DES RÉSULTATS")
    print("="*60)

    import os
    os.makedirs(output_dir, exist_ok=True)

    # Export pour les présentateurs
    export_schedule_for_presenters(
        solution, rooms, presentation_names,
        os.path.join(output_dir, "planning_presentateurs.xlsx"),
        half_day=half_day
    )

    # Export pour les élèves
    export_schedule_for_students(
        solution, students, rooms, presentation_names,
        os.path.join(output_dir, "planning_eleves.xlsx"),
        half_day=half_day
    )

    # Export des statistiques
    export_statistics(
        solution, students, presentation_names,
        os.path.join(output_dir, "statistiques.xlsx"),
        half_day=half_day
    )

    print("\n" + "="*60)
    print(f"Tous les résultats exportés dans {output_dir}/")
    print("="*60)
