"""
Script principal FESUP 2026 Optimizer - Optimisation par DEMI-JOURNÉE

Chaque demi-journée contient 2 créneaux qui se chevauchent:
- Créneaux pairs (0,2,4,6): arrivée tôt, slots 0-3
- Créneaux impairs (1,3,5,7): arrivée tard, slots 1-4

Les élèves des deux créneaux partagent les salles aux slots 1,2,3.
"""

import os
import sys
from pathlib import Path
from collections import defaultdict

from data_loader import load_data
from fesup_optimizer_ortools import FESUPOptimizer, Solution, HALF_DAY_NAMES
from result_exporter import export_all_results
import numpy as np


def main():
    """Fonction principale du programme."""

    # Chemins des fichiers d'entrée
    src_dir = Path(__file__).parent
    achart_dir = src_dir.parent
    project_root = achart_dir.parent
    students_csv = project_root / "Inputs" / "Eleves_Voeux.csv"
    rooms_excel = project_root / "Inputs" / "capacites.xlsx"

    # Vérifier que les fichiers existent
    if not students_csv.exists():
        print(f"Fichier non trouvé: {students_csv}")
        sys.exit(1)

    if not rooms_excel.exists():
        print(f"Fichier non trouvé: {rooms_excel}")
        sys.exit(1)

    # 1. Charger les données
    students, rooms, n_presentations, pres_mapping, pres_names = load_data(
        str(students_csv),
        str(rooms_excel)
    )

    # 2. Grouper les élèves par DEMI-JOURNÉE (pas par créneau!)
    print("\n" + "="*60)
    print("GROUPEMENT DES ÉLÈVES PAR DEMI-JOURNÉE")
    print("="*60)

    students_by_half_day = defaultdict(list)
    for student in students:
        if student.timeslot >= 0:
            half_day = student.timeslot // 2  # 0,1 → 0 | 2,3 → 1 | 4,5 → 2 | 6,7 → 3
            students_by_half_day[half_day].append(student)

    print(f"\nRépartition par demi-journée:")
    for hd in sorted(students_by_half_day.keys()):
        students_hd = students_by_half_day[hd]
        early = sum(1 for s in students_hd if s.timeslot % 2 == 0)
        late = sum(1 for s in students_hd if s.timeslot % 2 == 1)
        print(f"  {HALF_DAY_NAMES[hd]}")
        print(f"    - Total: {len(students_hd)} élèves")
        print(f"    - Arrivée tôt (créneau {hd*2}): {early} élèves")
        print(f"    - Arrivée tard (créneau {hd*2+1}): {late} élèves")

    # 3. Optimiser chaque demi-journée
    solutions = {}
    all_results = {}

    for hd in sorted(students_by_half_day.keys()):
        students_group = students_by_half_day[hd]

        print("\n" + "="*60)
        print(f"OPTIMISATION DEMI-JOURNÉE {hd}")
        print(f"{HALF_DAY_NAMES[hd]}")
        print(f"{len(students_group)} élèves")
        print("="*60)

        # Créer l'optimiseur pour cette demi-journée
        optimizer = FESUPOptimizer(
            students=students_group,
            rooms=rooms,
            n_presentations=n_presentations,
            n_time_slots=5  # 5 slots absolus par demi-journée
        )

        # Optimiser
        solution = optimizer.optimize(display=True)

        # Vérifier
        results = optimizer.verify_solution(solution)

        # Sauvegarder
        solutions[hd] = solution
        all_results[hd] = results

        # Exporter les résultats de cette demi-journée
        output_dir = achart_dir / "resultats" / f"demi_journee_{hd}"
        export_all_results(
            solution=solution,
            students=students_group,
            rooms=rooms,
            presentation_names=pres_names,
            output_dir=str(output_dir),
            half_day=hd
        )

    # 4. Résumé global
    print("\n" + "="*60)
    print("RÉSUMÉ GLOBAL")
    print("="*60)

    total_students = 0
    total_voeu5_used = 0

    for hd in sorted(solutions.keys()):
        solution = solutions[hd]
        results = all_results[hd]
        all_ok = all(results.values())
        n_students = len(students_by_half_day[hd])
        total_students += n_students

        status_icon = "OK" if all_ok else "ERREUR"
        print(f"\n[{status_icon}] {HALF_DAY_NAMES[hd]}")
        print(f"   Élèves: {n_students}")
        print(f"   Statut: {solution.status}")
        print(f"   Voeux 5 utilisés: {int(solution.objective_value)}")
        print(f"   Temps: {solution.computation_time:.2f}s")
        total_voeu5_used += solution.objective_value

        if not all_ok:
            for key, value in results.items():
                if not value:
                    print(f"   ERREUR: {key}")

    print("\n" + "="*60)
    print("STATISTIQUES GLOBALES")
    print("="*60)
    print(f"Total élèves: {total_students}")
    print(f"Total voeux 5 utilisés: {int(total_voeu5_used)}")
    print(f"Taux voeux 1-4: {100 * (1 - total_voeu5_used / (total_students * 4)):.1f}%")
    print(f"\nRésultats exportés dans: {achart_dir / 'resultats'}/")

    return 0


if __name__ == "__main__":
    try:
        sys.exit(main())
    except KeyboardInterrupt:
        print("\n\nInterruption par l'utilisateur")
        sys.exit(1)
    except Exception as e:
        print(f"\n\nErreur: {e}")
        import traceback
        traceback.print_exc()
        sys.exit(1)
