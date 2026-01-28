#!/usr/bin/env python3
"""
FESUP 2026 - Script de vérification des résultats de l'algorithme

Ce script vérifie:
1. Le respect des voeux des élèves
2. La capacité des salles vs le nombre de personnes allouées
3. Les attributions de salles pour les conférences, flash-métiers, tables rondes

Usage:
    python verify_results.py [--half-day N] [--verbose]
"""

import argparse
import sys
from pathlib import Path
from typing import Dict, List, Tuple
from dataclasses import dataclass
from collections import defaultdict

import pandas as pd


# ============================================================================
# CONFIGURATION
# ============================================================================

# Indices des types de présentations
CONF_INDICES = set(range(0, 19))           # 0-18 (Conf1-Conf19)
TR_INDICES = set(range(19, 25))            # 19-24 (TR1-TR6)
FM_INDICES = set(range(25, 31))            # 25-30 (FM1-FM6)

PRESENTATION_NAMES = (
    [f"Conf{i}" for i in range(1, 20)] +
    [f"TR{i}" for i in range(1, 7)] +
    [f"FM{i}" for i in range(1, 7)]
)

HALF_DAY_NAMES = {
    0: "Jour 1 - Matin (08h30-12h00)",
    1: "Jour 1 - Après-midi (13h30-17h00)",
    2: "Jour 2 - Matin (08h30-12h00)",
    3: "Jour 2 - Après-midi (13h30-17h00)"
}


# ============================================================================
# STRUCTURES DE DONNÉES
# ============================================================================

@dataclass
class StudentAssignment:
    """Représente l'attribution d'un élève."""
    id: int
    creneau: int
    arrivee: str
    slots: Dict[int, Tuple[str, str]]  # slot -> (presentation, salle)


@dataclass
class RoomAssignment:
    """Représente l'attribution d'une salle."""
    presentation: str
    room: str
    slot: int
    n_students: int


@dataclass
class VerificationResult:
    """Résultat d'une vérification."""
    name: str
    passed: bool
    details: List[str]
    warnings: List[str]


# ============================================================================
# CHARGEMENT DES DONNÉES
# ============================================================================

def load_students_wishes(csv_path: Path) -> pd.DataFrame:
    """Charge les voeux des élèves depuis le fichier CSV."""
    for encoding in ['utf-8', 'latin-1', 'iso-8859-1', 'cp1252']:
        try:
            df = pd.read_csv(csv_path, sep='\t', encoding=encoding)
            return df
        except UnicodeDecodeError:
            continue
    raise ValueError(f"Impossible de lire {csv_path}")


def load_room_capacities(excel_path: Path) -> Dict[str, int]:
    """Charge les capacités des salles depuis le fichier Excel."""
    df = pd.read_excel(excel_path)
    
    # Trouver les colonnes
    room_col = None
    capacity_col = None
    for col in df.columns:
        col_lower = str(col).lower()
        if 'salle' in col_lower or 'nom' in col_lower:
            room_col = col
        if 'capacit' in col_lower:
            capacity_col = col
    
    if room_col is None:
        room_col = df.columns[0]
    if capacity_col is None:
        capacity_col = df.columns[-1]
    
    capacities = {}
    for _, row in df.iterrows():
        room_name = str(row[room_col]).strip()
        capacity = int(row[capacity_col])
        capacities[room_name] = capacity
    
    return capacities


def load_planning_eleves(excel_path: Path) -> List[StudentAssignment]:
    """Charge le planning des élèves depuis le fichier Excel."""
    df = pd.read_excel(excel_path)
    
    students = []
    for _, row in df.iterrows():
        slots = {}
        for slot in range(5):
            pres_col = f'Slot_{slot}_Pres'
            salle_col = f'Slot_{slot}_Salle'
            if pres_col in df.columns and salle_col in df.columns:
                pres = str(row[pres_col]).strip() if pd.notna(row[pres_col]) else '-'
                salle = str(row[salle_col]).strip() if pd.notna(row[salle_col]) else '-'
                # Skip empty, absent, or invalid entries
                if pres != '-' and pres != '(absent)' and pres != 'nan' and not pres.startswith('('):
                    slots[slot] = (pres, salle)
        
        student = StudentAssignment(
            id=int(row['Élève_ID']),
            creneau=int(row['Créneau']) if pd.notna(row.get('Créneau', None)) else 0,
            arrivee=str(row.get('Arrivée', '')),
            slots=slots
        )
        students.append(student)
    
    return students


def load_planning_presentateurs(excel_path: Path) -> Dict[str, Dict[int, Tuple[str, int]]]:
    """Charge le planning des présentateurs depuis le fichier Excel.
    
    Returns:
        Dict[presentation, Dict[slot, (salle, n_eleves)]]
    """
    df = pd.read_excel(excel_path)
    
    planning = {}
    for _, row in df.iterrows():
        pres = str(row['Présentation']).strip()
        planning[pres] = {}
        
        for slot in range(5):
            for col in df.columns:
                if f'Slot {slot}' in col:
                    cell = str(row[col]).strip() if pd.notna(row[col]) else '-'
                    if cell != '-' and cell != 'nan':
                        # Format: "SALLE (N él.)"
                        if '(' in cell:
                            salle = cell.split('(')[0].strip()
                            n_str = cell.split('(')[1].replace('él.)', '').strip()
                            try:
                                n_eleves = int(n_str)
                            except ValueError:
                                n_eleves = 0
                        else:
                            salle = cell
                            n_eleves = 0
                        planning[pres][slot] = (salle, n_eleves)
                    break
    
    return planning


# ============================================================================
# VÉRIFICATIONS
# ============================================================================

def verify_wishes_respect(
    students_wishes_df: pd.DataFrame,
    students_assignments: List[StudentAssignment],
    half_day: int,
    verbose: bool = False
) -> VerificationResult:
    """Vérifie que les voeux des élèves sont respectés."""
    
    details = []
    warnings = []
    errors = []
    
    # Créer un mapping ID -> voeux
    wishes_by_id = {}
    for _, row in students_wishes_df.iterrows():
        voeux = [
            str(row['Voeu 1']).strip(),
            str(row['Voeu 2']).strip(),
            str(row['Voeu 3']).strip(),
            str(row['Voeu 4']).strip(),
            str(row['Voeu 5']).strip()
        ]
        # L'ID est l'index dans le dataframe
        wishes_by_id[row.name] = voeux
    
    # Statistiques
    total_students = len(students_assignments)
    voeu_stats = {1: 0, 2: 0, 3: 0, 4: 0, 5: 0, 'hors_voeux': 0}
    students_with_issues = []
    
    for student in students_assignments:
        student_id = student.id
        
        if student_id not in wishes_by_id:
            warnings.append(f"Élève {student_id} non trouvé dans la liste des voeux")
            continue
        
        voeux = wishes_by_id[student_id]
        assigned_presentations = [pres for slot, (pres, salle) in student.slots.items()]
        
        # Vérifier voeu 1 (obligatoire)
        if voeux[0] in assigned_presentations:
            voeu_stats[1] += 1
        else:
            students_with_issues.append(f"Élève {student_id}: voeu 1 ({voeux[0]}) non respecté")
        
        # Vérifier voeu 2 (obligatoire)
        if voeux[1] in assigned_presentations:
            voeu_stats[2] += 1
        else:
            students_with_issues.append(f"Élève {student_id}: voeu 2 ({voeux[1]}) non respecté")
        
        # Vérifier les autres voeux
        for pres in assigned_presentations:
            if pres in voeux:
                idx = voeux.index(pres)
                if idx >= 2:
                    voeu_stats[idx + 1] += 1
            else:
                voeu_stats['hors_voeux'] += 1
                students_with_issues.append(f"Élève {student_id}: présentation {pres} hors voeux")
    
    # Résultats
    v1_pct = 100 * voeu_stats[1] / total_students if total_students > 0 else 0
    v2_pct = 100 * voeu_stats[2] / total_students if total_students > 0 else 0
    
    details.append(f"Total élèves: {total_students}")
    details.append(f"Voeu 1 respecté: {voeu_stats[1]} ({v1_pct:.1f}%)")
    details.append(f"Voeu 2 respecté: {voeu_stats[2]} ({v2_pct:.1f}%)")
    details.append(f"Voeu 3 utilisé: {voeu_stats[3]}")
    details.append(f"Voeu 4 utilisé: {voeu_stats[4]}")
    details.append(f"Voeu 5 utilisé: {voeu_stats[5]}")
    
    if voeu_stats['hors_voeux'] > 0:
        warnings.append(f"Attributions hors voeux: {voeu_stats['hors_voeux']}")
    
    if verbose and students_with_issues:
        for issue in students_with_issues[:10]:
            details.append(f"  ⚠️ {issue}")
        if len(students_with_issues) > 10:
            details.append(f"  ... et {len(students_with_issues) - 10} autres problèmes")
    
    # Succès si voeux 1 et 2 sont respectés à 100%
    passed = voeu_stats[1] == total_students and voeu_stats[2] == total_students
    
    return VerificationResult(
        name="Respect des voeux",
        passed=passed,
        details=details,
        warnings=warnings + students_with_issues[:5]
    )


def verify_room_capacity(
    room_capacities: Dict[str, int],
    planning_presentateurs: Dict[str, Dict[int, Tuple[str, int]]],
    verbose: bool = False
) -> VerificationResult:
    """Vérifie que la capacité des salles n'est pas dépassée."""
    
    details = []
    warnings = []
    errors = []
    
    total_checks = 0
    capacity_exceeded = []
    utilization_stats = []
    
    for pres, slots in planning_presentateurs.items():
        for slot, (salle, n_eleves) in slots.items():
            total_checks += 1
            
            if salle not in room_capacities:
                warnings.append(f"Salle {salle} non trouvée dans capacites.xlsx")
                continue
            
            capacity = room_capacities[salle]
            # Note: Le système ajoute 10% de marge, donc on compare à la capacité avec marge
            capacity_with_margin = int(capacity * 1.10)
            
            utilization = 100 * n_eleves / capacity if capacity > 0 else 0
            utilization_stats.append((pres, slot, salle, n_eleves, capacity, utilization))
            
            if n_eleves > capacity_with_margin:
                capacity_exceeded.append(
                    f"{pres} slot {slot}: {n_eleves} élèves > {capacity} places ({salle})"
                )
    
    # Statistiques
    if utilization_stats:
        avg_utilization = sum(u[5] for u in utilization_stats) / len(utilization_stats)
        max_utilization = max(u[5] for u in utilization_stats)
        
        details.append(f"Total vérifications: {total_checks}")
        details.append(f"Utilisation moyenne: {avg_utilization:.1f}%")
        details.append(f"Utilisation max: {max_utilization:.1f}%")
    
    if capacity_exceeded:
        for err in capacity_exceeded[:5]:
            details.append(f"  ❌ {err}")
    
    if verbose:
        # Afficher les 5 plus utilisées
        top_5 = sorted(utilization_stats, key=lambda x: -x[5])[:5]
        details.append("Top 5 utilisations:")
        for pres, slot, salle, n, cap, util in top_5:
            details.append(f"  • {pres} slot {slot}: {n}/{cap} ({util:.0f}%)")
    
    passed = len(capacity_exceeded) == 0
    
    return VerificationResult(
        name="Capacité des salles",
        passed=passed,
        details=details,
        warnings=warnings + capacity_exceeded
    )


def verify_room_assignments(
    planning_presentateurs: Dict[str, Dict[int, Tuple[str, int]]],
    verbose: bool = False
) -> VerificationResult:
    """Vérifie la cohérence des attributions de salles."""
    
    details = []
    warnings = []
    errors = []
    
    # Vérifier qu'une salle n'est pas utilisée deux fois au même slot
    room_usage = defaultdict(list)  # (slot, salle) -> [presentations]
    
    stats = {
        'conferences': {'count': 0, 'slots': 0},
        'tables_rondes': {'count': 0, 'slots': 0},
        'flash_metiers': {'count': 0, 'slots': 0}
    }
    
    for pres, slots in planning_presentateurs.items():
        # Classifier la présentation
        if pres.startswith('Conf'):
            cat = 'conferences'
        elif pres.startswith('TR'):
            cat = 'tables_rondes'
        elif pres.startswith('FM'):
            cat = 'flash_metiers'
        else:
            cat = 'conferences'
        
        if len(slots) > 0:
            stats[cat]['count'] += 1
            stats[cat]['slots'] += len(slots)
        
        for slot, (salle, n_eleves) in slots.items():
            room_usage[(slot, salle)].append(pres)
    
    # Détecter les conflits
    conflicts = []
    for (slot, salle), presentations in room_usage.items():
        if len(presentations) > 1:
            conflicts.append(f"Slot {slot}, {salle}: {', '.join(presentations)}")
    
    # Statistiques
    details.append(f"Conférences: {stats['conferences']['count']} (total {stats['conferences']['slots']} créneaux)")
    details.append(f"Tables rondes: {stats['tables_rondes']['count']} (total {stats['tables_rondes']['slots']} créneaux)")
    details.append(f"Flash-métiers: {stats['flash_metiers']['count']} (total {stats['flash_metiers']['slots']} créneaux)")
    
    if conflicts:
        details.append(f"Conflits de salle: {len(conflicts)}")
        for conflict in conflicts[:5]:
            details.append(f"  ❌ {conflict}")
    else:
        details.append("Aucun conflit de salle détecté")
    
    passed = len(conflicts) == 0
    
    return VerificationResult(
        name="Attributions de salles",
        passed=passed,
        details=details,
        warnings=conflicts
    )


def verify_student_schedule(
    students_assignments: List[StudentAssignment],
    verbose: bool = False
) -> VerificationResult:
    """Vérifie la cohérence des plannings élèves."""
    
    details = []
    warnings = []
    
    # Vérifier que chaque élève a exactement 4 présentations
    wrong_count = []
    slot_conflicts = []
    
    for student in students_assignments:
        n_pres = len(student.slots)
        if n_pres != 4:
            wrong_count.append(f"Élève {student.id}: {n_pres} présentations au lieu de 4")
        
        # Vérifier les types
        presentations = [pres for slot, (pres, salle) in student.slots.items()]
        
        n_tr = sum(1 for p in presentations if p.startswith('TR'))
        n_fm = sum(1 for p in presentations if p.startswith('FM'))
        
        if n_tr > 1:
            warnings.append(f"Élève {student.id}: {n_tr} tables rondes (max 1 attendu)")
        if n_fm > 1:
            warnings.append(f"Élève {student.id}: {n_fm} flash-métiers (max 1 attendu)")
    
    # Statistiques
    details.append(f"Total élèves: {len(students_assignments)}")
    details.append(f"Élèves avec mauvais nombre: {len(wrong_count)}")
    
    if wrong_count:
        for err in wrong_count[:5]:
            details.append(f"  ⚠️ {err}")
    
    passed = len(wrong_count) == 0 and len(slot_conflicts) == 0
    
    return VerificationResult(
        name="Planning élèves",
        passed=passed,
        details=details,
        warnings=warnings[:5]
    )


# ============================================================================
# MAIN
# ============================================================================

def run_verification(half_day: int, verbose: bool = False) -> bool:
    """Exécute toutes les vérifications pour une demi-journée."""
    
    print("\n" + "=" * 70)
    print(f"VÉRIFICATION DES RÉSULTATS - {HALF_DAY_NAMES.get(half_day, f'Demi-journée {half_day}')}")
    print("=" * 70)
    
    # Chemins
    src_dir = Path(__file__).parent
    achart_dir = src_dir.parent
    project_root = achart_dir.parent
    
    students_csv = project_root / "Inputs" / "Eleves_Voeux.csv"
    capacites_xlsx = project_root / "Inputs" / "capacites.xlsx"
    results_dir = achart_dir / "resultats" / f"demi_journee_{half_day}"
    
    # Vérifier l'existence des fichiers
    if not results_dir.exists():
        print(f"\n❌ Dossier de résultats non trouvé: {results_dir}")
        print("   Exécutez d'abord main_grouped.py pour générer les résultats.")
        return False
    
    planning_eleves_xlsx = results_dir / "planning_eleves.xlsx"
    planning_presentateurs_xlsx = results_dir / "planning_presentateurs.xlsx"
    
    for f in [students_csv, capacites_xlsx, planning_eleves_xlsx, planning_presentateurs_xlsx]:
        if not f.exists():
            print(f"\n❌ Fichier non trouvé: {f}")
            return False
    
    # Charger les données
    print("\n📂 Chargement des données...")
    students_wishes_df = load_students_wishes(students_csv)
    room_capacities = load_room_capacities(capacites_xlsx)
    students_assignments = load_planning_eleves(planning_eleves_xlsx)
    planning_presentateurs = load_planning_presentateurs(planning_presentateurs_xlsx)
    
    print(f"   • {len(students_wishes_df)} élèves (fichier voeux)")
    print(f"   • {len(room_capacities)} salles")
    print(f"   • {len(students_assignments)} élèves (planning)")
    print(f"   • {len(planning_presentateurs)} présentations")
    
    # Exécuter les vérifications
    results = []
    
    print("\n🔍 Vérification en cours...")
    
    # 1. Respect des voeux
    result = verify_wishes_respect(students_wishes_df, students_assignments, half_day, verbose)
    results.append(result)
    
    # 2. Capacité des salles
    result = verify_room_capacity(room_capacities, planning_presentateurs, verbose)
    results.append(result)
    
    # 3. Attributions de salles
    result = verify_room_assignments(planning_presentateurs, verbose)
    results.append(result)
    
    # 4. Planning élèves
    result = verify_student_schedule(students_assignments, verbose)
    results.append(result)
    
    # Afficher les résultats
    print("\n" + "-" * 70)
    print("RÉSULTATS DES VÉRIFICATIONS")
    print("-" * 70)
    
    all_passed = True
    for result in results:
        icon = "✅" if result.passed else "❌"
        print(f"\n{icon} {result.name}")
        
        for detail in result.details:
            print(f"   {detail}")
        
        if result.warnings and not result.passed:
            print("   Avertissements:")
            for warning in result.warnings[:5]:
                print(f"     ⚠️ {warning}")
        
        if not result.passed:
            all_passed = False
    
    # Résumé final
    print("\n" + "=" * 70)
    if all_passed:
        print("✅ TOUTES LES VÉRIFICATIONS SONT PASSÉES")
    else:
        print("❌ CERTAINES VÉRIFICATIONS ONT ÉCHOUÉ")
    print("=" * 70)
    
    return all_passed


def main():
    """Point d'entrée principal."""
    parser = argparse.ArgumentParser(
        description="Vérifie les résultats de l'algorithme FESUP 2026"
    )
    parser.add_argument(
        '--half-day', '-d',
        type=int,
        choices=[0, 1, 2, 3],
        help="Demi-journée à vérifier (0-3). Si non spécifié, vérifie toutes."
    )
    parser.add_argument(
        '--verbose', '-v',
        action='store_true',
        help="Affiche plus de détails"
    )
    parser.add_argument(
        '--all', '-a',
        action='store_true',
        help="Vérifie toutes les demi-journées"
    )
    
    args = parser.parse_args()
    
    if args.half_day is not None:
        success = run_verification(args.half_day, args.verbose)
        sys.exit(0 if success else 1)
    else:
        # Par défaut, vérifier toutes les demi-journées disponibles
        all_success = True
        found_any = False
        for hd in range(4):
            results_dir = Path(__file__).parent.parent / "resultats" / f"demi_journee_{hd}"
            if results_dir.exists():
                found_any = True
                success = run_verification(hd, args.verbose)
                if not success:
                    all_success = False
        
        if not found_any:
            print("Aucun résultat trouvé. Exécutez d'abord main_grouped.py")
            sys.exit(1)
        
        # Résumé global
        print("\n" + "=" * 70)
        if all_success:
            print("✅ TOUTES LES DEMI-JOURNÉES ONT PASSÉ LES VÉRIFICATIONS")
        else:
            print("❌ CERTAINES DEMI-JOURNÉES ONT DES ERREURS")
        print("=" * 70)
        
        sys.exit(0 if all_success else 1)


if __name__ == "__main__":
    main()
