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
import pandas as pd


def export_aggregated_results(achart_dir: Path, students_by_half_day: dict, solutions: dict, all_results: dict):
    """Exporte un fichier Excel avec les résultats agrégés des 4 demi-journées."""
    
    print("\n" + "="*60)
    print("EXPORT DES RÉSULTATS AGRÉGÉS")
    print("="*60)
    
    output_file = achart_dir / "resultats" / "resultats_complets.xlsx"
    
    with pd.ExcelWriter(output_file, engine='openpyxl') as writer:
        
        # ============================================================
        # FEUILLE 1: STATISTIQUES GLOBALES
        # ============================================================
        stats_data = []
        total_students = 0
        total_voeu5 = 0
        total_time = 0
        
        for hd in sorted(solutions.keys()):
            solution = solutions[hd]
            n_students = len(students_by_half_day[hd])
            total_students += n_students
            total_voeu5 += solution.objective_value
            total_time += solution.computation_time
            
            stats_data.append({
                'Demi-journée': HALF_DAY_NAMES[hd],
                'Élèves': n_students,
                'Status': solution.status,
                'Voeux 5 utilisés': int(solution.objective_value),
                'Temps calcul (s)': round(solution.computation_time, 1),
                'Contraintes OK': 'Oui' if all(all_results[hd].values()) else 'Non'
            })
        
        # Ligne totale
        stats_data.append({
            'Demi-journée': 'TOTAL',
            'Élèves': total_students,
            'Status': '-',
            'Voeux 5 utilisés': int(total_voeu5),
            'Temps calcul (s)': round(total_time, 1),
            'Contraintes OK': '-'
        })
        
        df_stats = pd.DataFrame(stats_data)
        df_stats.to_excel(writer, sheet_name='Statistiques', index=False)
        print("  ✓ Feuille 'Statistiques' créée")
        
        # ============================================================
        # FEUILLE 2: PLANNING ÉLÈVES COMPLET
        # ============================================================
        all_students_data = []
        
        for hd in sorted(solutions.keys()):
            planning_file = achart_dir / "resultats" / f"demi_journee_{hd}" / "planning_eleves.xlsx"
            if planning_file.exists():
                df_hd = pd.read_excel(planning_file)
                df_hd.insert(0, 'Demi-journée', HALF_DAY_NAMES[hd])
                all_students_data.append(df_hd)
        
        if all_students_data:
            df_all_students = pd.concat(all_students_data, ignore_index=True)
            df_all_students.to_excel(writer, sheet_name='Planning Élèves', index=False)
            print(f"  ✓ Feuille 'Planning Élèves' créée ({len(df_all_students)} élèves)")
        
        # ============================================================
        # FEUILLE 3: PLANNING PRÉSENTATEURS COMPLET
        # ============================================================
        all_presenters_data = []
        
        for hd in sorted(solutions.keys()):
            planning_file = achart_dir / "resultats" / f"demi_journee_{hd}" / "planning_presentateurs.xlsx"
            if planning_file.exists():
                df_hd = pd.read_excel(planning_file)
                df_hd.insert(0, 'Demi-journée', HALF_DAY_NAMES[hd])
                all_presenters_data.append(df_hd)
        
        if all_presenters_data:
            df_all_presenters = pd.concat(all_presenters_data, ignore_index=True)
            df_all_presenters.to_excel(writer, sheet_name='Planning Présentateurs', index=False)
            print(f"  ✓ Feuille 'Planning Présentateurs' créée ({len(df_all_presenters)} lignes)")
        
        # ============================================================
        # FEUILLE 4: UTILISATION DES SALLES
        # ============================================================
        room_usage_data = []
        
        for hd in sorted(solutions.keys()):
            planning_file = achart_dir / "resultats" / f"demi_journee_{hd}" / "planning_presentateurs.xlsx"
            if planning_file.exists():
                df_hd = pd.read_excel(planning_file)
                for _, row in df_hd.iterrows():
                    pres = row['Présentation']
                    for col in df_hd.columns:
                        if 'Slot' in col and pd.notna(row[col]) and row[col] != '-':
                            cell = str(row[col])
                            if '(' in cell:
                                salle = cell.split('(')[0].strip()
                                n_str = cell.split('(')[1].replace('él.)', '').strip()
                                try:
                                    n_eleves = int(n_str)
                                except:
                                    n_eleves = 0
                                slot_num = col.split()[1]  # "Slot X (horaire)"
                                room_usage_data.append({
                                    'Demi-journée': HALF_DAY_NAMES[hd],
                                    'Présentation': pres,
                                    'Slot': slot_num,
                                    'Salle': salle,
                                    'Élèves': n_eleves
                                })
        
        if room_usage_data:
            df_room_usage = pd.DataFrame(room_usage_data)
            df_room_usage.to_excel(writer, sheet_name='Utilisation Salles', index=False)
            print(f"  ✓ Feuille 'Utilisation Salles' créée ({len(df_room_usage)} attributions)")
        
        # ============================================================
        # FEUILLE 5: RÉSUMÉ PAR PRÉSENTATION
        # ============================================================
        pres_summary = {}
        for item in room_usage_data:
            pres = item['Présentation']
            if pres not in pres_summary:
                pres_summary[pres] = {'sessions': 0, 'total_eleves': 0}
            pres_summary[pres]['sessions'] += 1
            pres_summary[pres]['total_eleves'] += item['Élèves']
        
        pres_data = []
        for pres, data in sorted(pres_summary.items()):
            pres_type = 'Conférence' if pres.startswith('Conf') else ('Table Ronde' if pres.startswith('TR') else 'Flash Métier')
            pres_data.append({
                'Présentation': pres,
                'Type': pres_type,
                'Sessions totales': data['sessions'],
                'Élèves totaux': data['total_eleves'],
                'Moyenne/session': round(data['total_eleves'] / data['sessions'], 1) if data['sessions'] > 0 else 0
            })
        
        if pres_data:
            df_pres = pd.DataFrame(pres_data)
            df_pres.to_excel(writer, sheet_name='Résumé Présentations', index=False)
            print(f"  ✓ Feuille 'Résumé Présentations' créée ({len(df_pres)} présentations)")
    
    print(f"\n📊 Fichier agrégé exporté: {output_file}")


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

    # 5. Exporter le fichier agrégé
    export_aggregated_results(achart_dir, students_by_half_day, solutions, all_results)

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
