"""
Script principal FESUP 2026 Optimizer (simplifie).

Optimise l'attribution des salles par demi-journee.
"""

import sys
from pathlib import Path
from collections import defaultdict

import pandas as pd

from loader import load_data
from optimizer import FESUPOptimizer
from exporter import export_all
from config import HALF_DAY_NAMES, N_TIME_SLOTS


def export_aggregated(base_dir: Path, students_by_hd: dict, solutions: dict, results: dict):
    """Exporte les resultats agreges."""
    output_file = base_dir / "resultats" / "resultats_complets.xlsx"

    with pd.ExcelWriter(output_file, engine='openpyxl') as writer:
        # Stats globales
        stats = []
        for hd in sorted(solutions.keys()):
            sol = solutions[hd]
            stats.append({
                'Demi-journee': HALF_DAY_NAMES[hd],
                'Eleves': len(students_by_hd[hd]),
                'Status': sol.status,
                'Voeux_5_utilises': int(sol.objective_value),
                'Temps_s': round(sol.computation_time, 1),
                'Contraintes_OK': 'Oui' if all(results[hd].values()) else 'Non'
            })

        total_students = sum(len(students_by_hd[hd]) for hd in solutions)
        total_v5 = sum(sol.objective_value for sol in solutions.values())
        total_time = sum(sol.computation_time for sol in solutions.values())
        stats.append({
            'Demi-journee': 'TOTAL',
            'Eleves': total_students,
            'Status': '-',
            'Voeux_5_utilises': int(total_v5),
            'Temps_s': round(total_time, 1),
            'Contraintes_OK': '-'
        })

        pd.DataFrame(stats).to_excel(writer, sheet_name='Statistiques', index=False)

        # Planning eleves complet
        all_students = []
        for hd in sorted(solutions.keys()):
            f = base_dir / "resultats" / f"demi_journee_{hd}" / "planning_eleves.xlsx"
            if f.exists():
                df = pd.read_excel(f)
                df.insert(0, 'Demi-journee', HALF_DAY_NAMES[hd])
                all_students.append(df)
        if all_students:
            pd.concat(all_students).to_excel(writer, sheet_name='Planning_Eleves', index=False)

        # Planning presentateurs complet
        all_pres = []
        for hd in sorted(solutions.keys()):
            f = base_dir / "resultats" / f"demi_journee_{hd}" / "planning_presentateurs.xlsx"
            if f.exists():
                df = pd.read_excel(f)
                df.insert(0, 'Demi-journee', HALF_DAY_NAMES[hd])
                all_pres.append(df)
        if all_pres:
            pd.concat(all_pres).to_excel(writer, sheet_name='Planning_Presentateurs', index=False)

    print(f"Fichier agrege: {output_file}")


def main():
    # Chemins
    src_dir = Path(__file__).parent
    base_dir = src_dir.parent
    project_root = base_dir.parent
    students_csv = project_root / "Inputs" / "Eleves_Voeux.csv"
    rooms_excel = project_root / "Inputs" / "capacites.xlsx"

    for f in [students_csv, rooms_excel]:
        if not f.exists():
            print(f"Fichier non trouve: {f}")
            return 1

    # Charger donnees
    students, rooms, n_pres, pres_mapping, pres_names = load_data(str(students_csv), str(rooms_excel))

    # Grouper par demi-journee
    students_by_hd = defaultdict(list)
    for s in students:
        if s.timeslot >= 0:
            students_by_hd[s.timeslot // 2].append(s)

    print("\nRepartition:")
    for hd in sorted(students_by_hd.keys()):
        print(f"  {HALF_DAY_NAMES[hd]}: {len(students_by_hd[hd])} eleves")

    # Optimiser chaque demi-journee
    solutions, all_results = {}, {}

    for hd in sorted(students_by_hd.keys()):
        group = students_by_hd[hd]
        print(f"\n{'='*60}\n{HALF_DAY_NAMES[hd]} - {len(group)} eleves\n{'='*60}")

        optimizer = FESUPOptimizer(group, rooms, n_pres, N_TIME_SLOTS)
        solution = optimizer.optimize(display=True)
        results = optimizer.verify_solution(solution)

        solutions[hd] = solution
        all_results[hd] = results

        output_dir = base_dir / "resultats" / f"demi_journee_{hd}"
        export_all(solution, group, rooms, pres_names, str(output_dir), hd)

    # Resume
    print(f"\n{'='*60}\nRESUME GLOBAL\n{'='*60}")
    total_v5 = 0
    for hd in sorted(solutions.keys()):
        sol = solutions[hd]
        ok = "OK" if all(all_results[hd].values()) else "ERREUR"
        print(f"[{ok}] {HALF_DAY_NAMES[hd]}: {sol.status}, v5={int(sol.objective_value)}")
        total_v5 += sol.objective_value

    total_students = sum(len(students_by_hd[hd]) for hd in solutions)
    print(f"\nTotal: {total_students} eleves, {int(total_v5)} voeux 5 utilises")

    export_aggregated(base_dir, students_by_hd, solutions, all_results)
    return 0


if __name__ == "__main__":
    try:
        sys.exit(main())
    except KeyboardInterrupt:
        print("\nInterrompu")
        sys.exit(1)
    except Exception as e:
        print(f"Erreur: {e}")
        import traceback
        traceback.print_exc()
        sys.exit(1)
