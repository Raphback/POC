"""
FESUP 2026 Optimizer - Version OR-Tools CP-SAT (Optimisé)

Optimisation par DEMI-JOURNÉE avec 5 slots absolus.
Les créneaux pairs (0,2,4,6) arrivent tôt et utilisent slots 0,1,2,3
Les créneaux impairs (1,3,5,7) arrivent tard et utilisent slots 1,2,3,4

STRUCTURE DES VOEUX:
- Voeux 1, 2, 3 = Conférences uniquement (Conf1-Conf19, indices 0-18)
- Voeux 4, 5 = Tables rondes ou Flash-métiers uniquement (TR1-6, FM1-6, indices 19-30)

Chaque élève assiste à 4 présentations sur 5 voeux:
- Voeux 1 et 2 obligatoires (2 conférences)
- 1 voeu parmi {3, 4, 5} est sauté
- Donc: 2 ou 3 conférences + 1 ou 2 TR/FM = 4 total
"""

from dataclasses import dataclass
from typing import List, Dict, Optional, Set, Tuple
import numpy as np
from ortools.sat.python import cp_model
import time


# Indices des types de présentations
N_CONFERENCES = 19  # Conf1-Conf19 = indices 0-18
N_TR = 6            # TR1-TR6 = indices 19-24
N_FM = 6            # FM1-FM6 = indices 25-30
CONF_INDICES = set(range(0, 19))           # 0-18
TR_INDICES = set(range(19, 25))            # 19-24 (TR1-TR6)
FM_INDICES = set(range(25, 31))            # 25-30 (FM1-FM6)
TR_FM_INDICES = TR_INDICES | FM_INDICES    # 19-30


@dataclass
class Student:
    """Représente un élève avec ses voeux de présentation."""
    id: int
    voeux: List[int]  # 5 voeux (indices de présentations 0 à P-1)
    vague: int  # 1 ou 2 - DEPRECATED
    timeslot: int = 0  # Créneau horaire (0-7)

    def get_valid_slots(self) -> List[int]:
        """Retourne les 4 slots valides pour cet élève selon son créneau."""
        if self.timeslot % 2 == 0:
            return [0, 1, 2, 3]
        else:
            return [1, 2, 3, 4]

    def get_half_day(self) -> int:
        """Retourne la demi-journée (0-3) pour cet élève."""
        return self.timeslot // 2

    def get_conference_voeux(self) -> List[int]:
        """Retourne les voeux qui sont des conférences (voeux 1, 2, 3)."""
        return self.voeux[0:3]

    def get_trfm_voeux(self) -> List[int]:
        """Retourne les voeux qui sont des TR/FM (voeux 4, 5)."""
        return self.voeux[3:5]


@dataclass
class Room:
    """Représente une salle avec sa capacité."""
    id: int
    name: str
    capacity: int


@dataclass
class Solution:
    """Résultat de l'optimisation."""
    ae: np.ndarray  # shape (E, P, T) avec T=5 slots
    as_: np.ndarray  # shape (P, S, T)
    objective_value: float
    status: str
    computation_time: float


HALF_DAY_NAMES = {
    0: "Jour 1 - Matin (08h30-12h00)",
    1: "Jour 1 - Après-midi (13h30-17h00)",
    2: "Jour 2 - Matin (08h30-12h00)",
    3: "Jour 2 - Après-midi (13h30-17h00)"
}

SLOT_TIMES = {
    0: {0: "08h30", 1: "09h15", 2: "10h00", 3: "10h45", 4: "11h30"},
    1: {0: "13h30", 1: "14h15", 2: "15h00", 3: "15h45", 4: "16h30"},
    2: {0: "08h30", 1: "09h15", 2: "10h00", 3: "10h45", 4: "11h30"},
    3: {0: "13h30", 1: "14h15", 2: "15h00", 3: "15h45", 4: "16h30"}
}


class FESUPOptimizerORTools:
    """Optimiseur FESUP utilisant OR-Tools CP-SAT.

    Version optimisée exploitant la structure des voeux:
    - Voeux 1-3 = Conférences
    - Voeux 4-5 = TR/FM

    Ne crée des variables que pour les voeux réels de chaque élève,
    réduisant drastiquement l'espace de recherche.
    """

    def __init__(self,
                 students: List[Student],
                 rooms: List[Room],
                 n_presentations: int,
                 n_time_slots: int = 5):
        self.students = students
        self.rooms = rooms
        self.n_presentations = n_presentations
        self.n_time_slots = n_time_slots

        self.E = len(students)
        self.P = n_presentations
        self.S = len(rooms)
        self.T = n_time_slots

        # Pré-calculer les slots valides pour chaque élève
        self.student_valid_slots = {
            e: set(student.get_valid_slots())
            for e, student in enumerate(self.students)
        }

        # Pré-calculer les voeux uniques par élève (pour réduire les variables)
        self.student_voeux_set = {
            e: set(student.voeux)
            for e, student in enumerate(self.students)
        }

        # Compter les élèves par type de créneau
        early_count = sum(1 for s in students if s.timeslot % 2 == 0)
        late_count = sum(1 for s in students if s.timeslot % 2 == 1)

        print(f"Dimensions du problème (OPTIMISÉ):")
        print(f"  - Élèves total: {self.E}")
        print(f"    - Arrivée tôt (slots 0-3): {early_count}")
        print(f"    - Arrivée tard (slots 1-4): {late_count}")
        print(f"  - Présentations: {self.P}")
        print(f"    - Conférences (voeux 1-3): {N_CONFERENCES}")
        print(f"    - TR/FM (voeux 4-5): {N_TR + N_FM}")
        print(f"  - Salles: {self.S}")
        print(f"  - Slots absolus: {self.T}")

        # Calculer la réduction de variables
        full_vars = self.E * self.P * 4  # Sans optimisation
        optimized_vars = sum(len(self.student_voeux_set[e]) * len(self.student_valid_slots[e])
                            for e in range(self.E))
        reduction = 100 * (1 - optimized_vars / full_vars)
        print(f"\n  Optimisation des variables:")
        print(f"    - Variables sans optimisation: {full_vars:,}")
        print(f"    - Variables avec optimisation: {optimized_vars:,}")
        print(f"    - Réduction: {reduction:.1f}%")

        # Afficher la répartition par slot
        print(f"\n  Répartition par slot:")
        for t in range(self.T):
            count = sum(1 for e in range(self.E) if t in self.student_valid_slots[e])
            print(f"    - Slot {t}: {count} élèves présents")

    def optimize(self, time_limit: Optional[int] = None, display: bool = True) -> Solution:
        """Résout le problème avec CP-SAT (version optimisée)."""
        start_time = time.time()

        print("\n" + "="*60)
        print("OPTIMISATION FESUP 2026 (OR-Tools CP-SAT - OPTIMISÉ)")
        print("="*60)

        model = cp_model.CpModel()

        # ================================================================
        # VARIABLES DE DÉCISION (réduites)
        # ================================================================
        print("\nCréation des variables (optimisées)...")

        # ae[e, p, t] = 1 si élève e suit présentation p au slot t
        # OPTIMISATION: On ne crée des variables QUE pour:
        # - Les slots valides de l'élève
        # - Les présentations qui sont dans ses voeux
        ae = {}
        for e, student in enumerate(self.students):
            valid_slots = self.student_valid_slots[e]
            voeux_set = self.student_voeux_set[e]
            for p in voeux_set:
                for t in valid_slots:
                    ae[e, p, t] = model.NewBoolVar(f'ae_{e}_{p}_{t}')

        print(f"  - Variables ae créées: {len(ae):,}")

        # as_[p, s, t] = 1 si présentation p est dans salle s au slot t
        as_ = {}
        for p in range(self.P):
            for s in range(self.S):
                for t in range(self.T):
                    as_[p, s, t] = model.NewBoolVar(f'as_{p}_{s}_{t}')

        print(f"  - Variables as créées: {len(as_):,}")

        # ================================================================
        # CONTRAINTES
        # ================================================================
        print("\nConstruction des contraintes...")

        # Contrainte 1: Voeu 1 respecté (exactement 1 fois) - CONFÉRENCE
        print("  - Contrainte 1: Voeux 1 respectés (conférence)")
        for e, student in enumerate(self.students):
            voeu_1 = student.voeux[0]
            valid_slots = self.student_valid_slots[e]
            model.Add(sum(ae[e, voeu_1, t] for t in valid_slots) == 1)

        # Contrainte 2: Voeu 2 respecté (exactement 1 fois) - CONFÉRENCE
        print("  - Contrainte 2: Voeux 2 respectés (conférence)")
        for e, student in enumerate(self.students):
            voeu_2 = student.voeux[1]
            valid_slots = self.student_valid_slots[e]
            model.Add(sum(ae[e, voeu_2, t] for t in valid_slots) == 1)

        # Contrainte 3: Exactement 4 présentations parmi les 5 voeux
        print("  - Contrainte 3: 4 présentations parmi les 5 voeux")
        for e, student in enumerate(self.students):
            valid_slots = self.student_valid_slots[e]
            voeux_set = self.student_voeux_set[e]
            model.Add(
                sum(ae[e, p, t] for p in voeux_set for t in valid_slots) == 4
            )

        # Contrainte 4: Pas de répétition (max 1 fois par présentation)
        print("  - Contrainte 4: Pas de répétition de présentation")
        for e, student in enumerate(self.students):
            valid_slots = self.student_valid_slots[e]
            voeux_set = self.student_voeux_set[e]
            for p in voeux_set:
                model.Add(sum(ae[e, p, t] for t in valid_slots) <= 1)

        # Contrainte 5: Un slot = une présentation par élève
        print("  - Contrainte 5: Un slot = une présentation")
        for e, student in enumerate(self.students):
            valid_slots = self.student_valid_slots[e]
            voeux_set = self.student_voeux_set[e]
            for t in valid_slots:
                model.Add(sum(ae[e, p, t] for p in voeux_set) == 1)

        # Contrainte 6: Une présentation max par salle par slot
        print("  - Contrainte 6: Une présentation max par salle par slot")
        for s in range(self.S):
            for t in range(self.T):
                model.Add(sum(as_[p, s, t] for p in range(self.P)) <= 1)

        # Contrainte 6b: Une salle max par présentation par slot
        # (Sans cette contrainte, le solveur peut assigner une présentation à plusieurs salles
        # simultanément, multipliant artificiellement la capacité disponible)
        print("  - Contrainte 6b: Une salle max par présentation par slot")
        for p in range(self.P):
            for t in range(self.T):
                model.Add(sum(as_[p, s, t] for s in range(self.S)) <= 1)

        # Contrainte 7: Respect de la capacité des salles
        print("  - Contrainte 7: Respect de la capacité des salles")

        # Pré-calculer quels élèves ont chaque présentation dans leurs voeux
        students_with_pres = {p: [] for p in range(self.P)}
        for e, student in enumerate(self.students):
            for p in self.student_voeux_set[e]:
                students_with_pres[p].append(e)

        for p in range(self.P):
            for t in range(self.T):
                # Élèves qui ont cette présentation dans leurs voeux ET sont présents à ce slot
                relevant_students = [
                    e for e in students_with_pres[p]
                    if t in self.student_valid_slots[e]
                ]
                if relevant_students:
                    model.Add(
                        sum(ae[e, p, t] for e in relevant_students) <=
                        sum(self.rooms[s].capacity * as_[p, s, t] for s in range(self.S))
                    )

        # Contrainte 8: Maximum 1 Table Ronde par élève
        # Combinaisons valides: 4 conf, 3 conf + 1 TR, 3 conf + 1 FM, 2 conf + 1 TR + 1 FM
        print("  - Contrainte 8: Maximum 1 Table Ronde par élève")
        for e, student in enumerate(self.students):
            valid_slots = self.student_valid_slots[e]
            voeux_set = self.student_voeux_set[e]
            # TR dans les voeux de cet élève
            tr_in_voeux = [p for p in voeux_set if p in TR_INDICES]
            if tr_in_voeux:
                model.Add(
                    sum(ae[e, p, t] for p in tr_in_voeux for t in valid_slots) <= 1
                )

        # Contrainte 9: Maximum 1 Flash Métier par élève
        print("  - Contrainte 9: Maximum 1 Flash Métier par élève")
        for e, student in enumerate(self.students):
            valid_slots = self.student_valid_slots[e]
            voeux_set = self.student_voeux_set[e]
            # FM dans les voeux de cet élève
            fm_in_voeux = [p for p in voeux_set if p in FM_INDICES]
            if fm_in_voeux:
                model.Add(
                    sum(ae[e, p, t] for p in fm_in_voeux for t in valid_slots) <= 1
                )

        # ================================================================
        # FONCTION OBJECTIF
        # ================================================================
        # Minimiser l'utilisation du voeu 5 (TR/FM), puis du voeu 4
        print("  - Définition de l'objectif (minimiser voeu 5, puis voeu 4)")

        objective_terms = []
        for e, student in enumerate(self.students):
            valid_slots = self.student_valid_slots[e]
            voeu_5 = student.voeux[4]
            voeu_4 = student.voeux[3]
            voeu_3 = student.voeux[2]

            # Pénalité forte pour voeu 5 (on préfère l'éviter)
            for t in valid_slots:
                objective_terms.append(10 * ae[e, voeu_5, t])

            # Pénalité moyenne pour voeu 4
            for t in valid_slots:
                objective_terms.append(5 * ae[e, voeu_4, t])

            # Pénalité faible pour voeu 3
            for t in valid_slots:
                objective_terms.append(1 * ae[e, voeu_3, t])

        model.Minimize(sum(objective_terms))

        # ================================================================
        # RÉSOLUTION
        # ================================================================
        print(f"\nLancement du solveur CP-SAT...")
        solver = cp_model.CpSolver()

        # Paramètres optimisés
        solver.parameters.num_workers = 8
        solver.parameters.log_search_progress = display

        # Stratégies pour améliorer la recherche
        solver.parameters.linearization_level = 2
        solver.parameters.cp_model_presolve = True

        if time_limit is not None:
            solver.parameters.max_time_in_seconds = time_limit

        status = solver.Solve(model)
        computation_time = time.time() - start_time

        print("\n" + "="*60)
        print("RÉSULTAT DE L'OPTIMISATION")
        print("="*60)

        status_names = {
            cp_model.OPTIMAL: "OPTIMAL",
            cp_model.FEASIBLE: "FEASIBLE",
            cp_model.INFEASIBLE: "INFEASIBLE",
            cp_model.MODEL_INVALID: "MODEL_INVALID",
            cp_model.UNKNOWN: "UNKNOWN"
        }
        status_str = status_names.get(status, "UNKNOWN")
        print(f"Status: {status_str}")

        if status == cp_model.INFEASIBLE or status == cp_model.MODEL_INVALID:
            print(f"Temps de calcul: {computation_time:.2f}s")
            return Solution(
                ae=np.zeros((self.E, self.P, self.T)),
                as_=np.zeros((self.P, self.S, self.T)),
                objective_value=float('inf'),
                status=status_str,
                computation_time=computation_time
            )

        print(f"Valeur objectif: {solver.ObjectiveValue()}")
        print(f"Temps de calcul: {computation_time:.2f}s")

        # Extraire la solution
        ae_result = np.zeros((self.E, self.P, self.T))
        as_result = np.zeros((self.P, self.S, self.T))

        for e, student in enumerate(self.students):
            valid_slots = self.student_valid_slots[e]
            voeux_set = self.student_voeux_set[e]
            for p in voeux_set:
                for t in valid_slots:
                    ae_result[e, p, t] = solver.Value(ae[e, p, t])

        for p in range(self.P):
            for s in range(self.S):
                for t in range(self.T):
                    as_result[p, s, t] = solver.Value(as_[p, s, t])

        # Statistiques sur les voeux utilisés
        voeu_counts = {1: 0, 2: 0, 3: 0, 4: 0, 5: 0}
        for e, student in enumerate(self.students):
            valid_slots = self.student_valid_slots[e]
            for i, voeu in enumerate(student.voeux):
                if sum(ae_result[e, voeu, t] for t in valid_slots) > 0.5:
                    voeu_counts[i + 1] += 1

        print(f"\nStatistiques des voeux utilisés:")
        for i in range(1, 6):
            pct = 100 * voeu_counts[i] / self.E
            print(f"  - Voeu {i}: {voeu_counts[i]} élèves ({pct:.1f}%)")

        return Solution(
            ae=ae_result,
            as_=as_result,
            objective_value=solver.ObjectiveValue(),
            status=status_str,
            computation_time=computation_time
        )

    def verify_solution(self, solution: Solution) -> Dict[str, bool]:
        """Vérifie que la solution respecte toutes les contraintes."""
        results = {}
        ae = solution.ae
        as_ = solution.as_

        print("\n" + "="*60)
        print("VÉRIFICATION DE LA SOLUTION")
        print("="*60)

        # 1. Voeux 1 et 2 respectés
        print("\n1. Vérification des voeux 1 et 2...")
        all_voeu1_ok = True
        all_voeu2_ok = True
        for e, student in enumerate(self.students):
            voeu_1 = student.voeux[0]
            voeu_2 = student.voeux[1]
            valid_slots = self.student_valid_slots[e]
            count_v1 = sum(ae[e, voeu_1, t] for t in valid_slots)
            count_v2 = sum(ae[e, voeu_2, t] for t in valid_slots)
            if abs(count_v1 - 1) > 1e-6:
                all_voeu1_ok = False
            if abs(count_v2 - 1) > 1e-6:
                all_voeu2_ok = False
        results['voeu_1_respecte'] = all_voeu1_ok
        results['voeu_2_respecte'] = all_voeu2_ok
        print(f"   {'✓' if all_voeu1_ok else '✗'} Voeu 1: {all_voeu1_ok}")
        print(f"   {'✓' if all_voeu2_ok else '✗'} Voeu 2: {all_voeu2_ok}")

        # 2. Nombre de présentations (4 par élève)
        print("\n2. Vérification du nombre de présentations par élève...")
        all_count_ok = True
        for e, student in enumerate(self.students):
            valid_slots = self.student_valid_slots[e]
            voeux_set = self.student_voeux_set[e]
            total = sum(ae[e, p, t] for p in voeux_set for t in valid_slots)
            if abs(total - 4) > 1e-6:
                all_count_ok = False
                print(f"   ✗ Élève {e} a {int(total)} présentations au lieu de 4")
                break
        results['nombre_presentations_ok'] = all_count_ok
        print(f"   {'✓' if all_count_ok else '✗'} Exactement 4 présentations: {all_count_ok}")

        # 3. Pas de répétition
        print("\n3. Vérification des répétitions...")
        no_repetition = True
        for e, student in enumerate(self.students):
            valid_slots = self.student_valid_slots[e]
            voeux_set = self.student_voeux_set[e]
            for p in voeux_set:
                count = sum(ae[e, p, t] for t in valid_slots)
                if count > 1 + 1e-6:
                    no_repetition = False
                    print(f"   ✗ Élève {e} assiste {int(count)} fois à la présentation {p}")
                    break
            if not no_repetition:
                break
        results['pas_de_repetition'] = no_repetition
        print(f"   {'✓' if no_repetition else '✗'} Pas de répétition: {no_repetition}")

        # 4. Un slot = une présentation (sur les slots valides)
        print("\n4. Vérification des slots...")
        one_per_slot = True
        for e, student in enumerate(self.students):
            valid_slots = self.student_valid_slots[e]
            voeux_set = self.student_voeux_set[e]
            for t in valid_slots:
                count = sum(ae[e, p, t] for p in voeux_set)
                if abs(count - 1) > 1e-6:
                    one_per_slot = False
                    print(f"   ✗ Élève {e} a {int(count)} présentations au slot {t}")
                    break
            if not one_per_slot:
                break
        results['un_slot_une_presentation'] = one_per_slot
        print(f"   {'✓' if one_per_slot else '✗'} Un slot = une présentation: {one_per_slot}")

        # 5. Capacités respectées
        print("\n5. Vérification des capacités des salles...")
        capacity_ok = True
        for p in range(self.P):
            for t in range(self.T):
                students_at_slot = [e for e in range(self.E) if t in self.student_valid_slots[e]]
                n_students = sum(ae[e, p, t] for e in students_at_slot if p in self.student_voeux_set[e])
                total_capacity = sum(
                    self.rooms[s].capacity for s in range(self.S) if as_[p, s, t] > 0.5
                )
                if n_students > total_capacity + 1e-6:
                    capacity_ok = False
                    print(f"   ✗ Présentation {p}, slot {t}: {int(n_students)} élèves > {total_capacity} places")
                    break
            if not capacity_ok:
                break
        results['capacite_respectee'] = capacity_ok
        print(f"   {'✓' if capacity_ok else '✗'} Capacités respectées: {capacity_ok}")

        # 6. Une salle max par présentation par slot
        print("\n6. Vérification unicité salle/présentation...")
        one_room_per_pres = True
        for s in range(self.S):
            for t in range(self.T):
                count = sum(as_[p, s, t] for p in range(self.P))
                if count > 1 + 1e-6:
                    one_room_per_pres = False
                    break
            if not one_room_per_pres:
                break
        results['une_salle_par_presentation'] = one_room_per_pres
        print(f"   {'✓' if one_room_per_pres else '✗'} Une salle max par présentation: {one_room_per_pres}")

        # 7. Maximum 1 TR par élève
        print("\n7. Vérification max 1 Table Ronde par élève...")
        max_one_tr = True
        for e, student in enumerate(self.students):
            valid_slots = self.student_valid_slots[e]
            voeux_set = self.student_voeux_set[e]
            tr_count = sum(ae[e, p, t] for p in voeux_set if p in TR_INDICES for t in valid_slots)
            if tr_count > 1 + 1e-6:
                max_one_tr = False
                print(f"   ✗ Élève {e} a {int(tr_count)} Tables Rondes")
                break
        results['max_une_tr'] = max_one_tr
        print(f"   {'✓' if max_one_tr else '✗'} Max 1 TR par élève: {max_one_tr}")

        # 8. Maximum 1 FM par élève
        print("\n8. Vérification max 1 Flash Métier par élève...")
        max_one_fm = True
        for e, student in enumerate(self.students):
            valid_slots = self.student_valid_slots[e]
            voeux_set = self.student_voeux_set[e]
            fm_count = sum(ae[e, p, t] for p in voeux_set if p in FM_INDICES for t in valid_slots)
            if fm_count > 1 + 1e-6:
                max_one_fm = False
                print(f"   ✗ Élève {e} a {int(fm_count)} Flash Métiers")
                break
        results['max_un_fm'] = max_one_fm
        print(f"   {'✓' if max_one_fm else '✗'} Max 1 FM par élève: {max_one_fm}")

        # 9. Vérification des combinaisons valides
        print("\n9. Vérification des combinaisons (4c, 3c+1TR, 3c+1FM, 2c+1TR+1FM)...")
        valid_combos = True
        combo_counts = {"4c": 0, "3c+1TR": 0, "3c+1FM": 0, "2c+1TR+1FM": 0, "invalid": 0}
        for e, student in enumerate(self.students):
            valid_slots = self.student_valid_slots[e]
            voeux_set = self.student_voeux_set[e]

            n_conf = sum(ae[e, p, t] for p in voeux_set if p in CONF_INDICES for t in valid_slots)
            n_tr = sum(ae[e, p, t] for p in voeux_set if p in TR_INDICES for t in valid_slots)
            n_fm = sum(ae[e, p, t] for p in voeux_set if p in FM_INDICES for t in valid_slots)

            n_conf, n_tr, n_fm = int(n_conf + 0.5), int(n_tr + 0.5), int(n_fm + 0.5)

            if (n_conf, n_tr, n_fm) == (4, 0, 0):
                combo_counts["4c"] += 1
            elif (n_conf, n_tr, n_fm) == (3, 1, 0):
                combo_counts["3c+1TR"] += 1
            elif (n_conf, n_tr, n_fm) == (3, 0, 1):
                combo_counts["3c+1FM"] += 1
            elif (n_conf, n_tr, n_fm) == (2, 1, 1):
                combo_counts["2c+1TR+1FM"] += 1
            else:
                combo_counts["invalid"] += 1
                valid_combos = False
                if combo_counts["invalid"] <= 3:
                    print(f"   ✗ Élève {e}: {n_conf}c + {n_tr}TR + {n_fm}FM (invalide)")

        results['combinaisons_valides'] = valid_combos
        print(f"   Répartition des combinaisons:")
        for combo, count in combo_counts.items():
            if count > 0:
                pct = 100 * count / self.E
                print(f"     - {combo}: {count} élèves ({pct:.1f}%)")

        # Résumé
        print("\n" + "="*60)
        all_ok = all(results.values())
        if all_ok:
            print("TOUTES LES CONTRAINTES SONT RESPECTÉES")
        else:
            print("CERTAINES CONTRAINTES NE SONT PAS RESPECTÉES")
        print("="*60)

        return results


# Alias pour compatibilité
FESUPOptimizer = FESUPOptimizerORTools
