"""
FESUP 2026 Optimizer - OR-Tools CP-SAT

Optimisation par demi-journee avec 5 slots absolus.
"""

from typing import List, Dict, Optional
import numpy as np
from ortools.sat.python import cp_model
import time

from models import Student, Room, Solution
from config import (
    CONF_INDICES, TR_INDICES, FM_INDICES, N_CONFERENCES, N_TR, N_FM,
    OBJECTIVE_WEIGHTS, SOLVER_WORKERS, SOLVER_LINEARIZATION_LEVEL
)


class FESUPOptimizer:
    """Optimiseur FESUP utilisant OR-Tools CP-SAT."""

    def __init__(self, students: List[Student], rooms: List[Room],
                 n_presentations: int, n_time_slots: int = 5):
        self.students = students
        self.rooms = rooms
        self.E = len(students)
        self.P = n_presentations
        self.S = len(rooms)
        self.T = n_time_slots

        # Pre-calculer les slots valides et voeux par eleve
        self.valid_slots = {e: set(s.get_valid_slots()) for e, s in enumerate(students)}
        self.voeux_set = {e: set(s.voeux) for e, s in enumerate(students)}

        self._print_dimensions()

    def _print_dimensions(self):
        early = sum(1 for s in self.students if s.timeslot % 2 == 0)
        late = self.E - early

        full_vars = self.E * self.P * 4
        opt_vars = sum(len(self.voeux_set[e]) * len(self.valid_slots[e]) for e in range(self.E))
        reduction = 100 * (1 - opt_vars / full_vars)

        print(f"Dimensions: {self.E} eleves ({early} tot/{late} tard), "
              f"{self.P} presentations, {self.S} salles")
        print(f"Variables: {opt_vars:,} (reduction {reduction:.0f}%)")

    def optimize(self, time_limit: Optional[int] = None, display: bool = True) -> Solution:
        start_time = time.time()
        model = cp_model.CpModel()

        # Variables ae[e,p,t] et as_[p,s,t]
        ae = {(e, p, t): model.NewBoolVar(f'ae_{e}_{p}_{t}')
              for e in range(self.E)
              for p in self.voeux_set[e]
              for t in self.valid_slots[e]}

        as_ = {(p, s, t): model.NewBoolVar(f'as_{p}_{s}_{t}')
               for p in range(self.P) for s in range(self.S) for t in range(self.T)}

        # Contraintes
        self._add_constraints(model, ae, as_)

        # Objectif: minimiser utilisation voeux 3-4-5
        obj = []
        for e, s in enumerate(self.students):
            for i, w in OBJECTIVE_WEIGHTS.items():
                voeu = s.voeux[i - 1]
                obj.extend(w * ae[e, voeu, t] for t in self.valid_slots[e])
        model.Minimize(sum(obj))

        # Resolution
        solver = cp_model.CpSolver()
        solver.parameters.num_workers = SOLVER_WORKERS
        solver.parameters.log_search_progress = display
        solver.parameters.linearization_level = SOLVER_LINEARIZATION_LEVEL
        solver.parameters.cp_model_presolve = True
        if time_limit:
            solver.parameters.max_time_in_seconds = time_limit

        status = solver.Solve(model)
        comp_time = time.time() - start_time

        status_str = {
            cp_model.OPTIMAL: "OPTIMAL", cp_model.FEASIBLE: "FEASIBLE",
            cp_model.INFEASIBLE: "INFEASIBLE", cp_model.MODEL_INVALID: "MODEL_INVALID"
        }.get(status, "UNKNOWN")

        if status in (cp_model.INFEASIBLE, cp_model.MODEL_INVALID):
            return Solution(np.zeros((self.E, self.P, self.T)),
                          np.zeros((self.P, self.S, self.T)),
                          float('inf'), status_str, comp_time)

        # Extraire solution
        ae_result = np.zeros((self.E, self.P, self.T))
        as_result = np.zeros((self.P, self.S, self.T))

        for (e, p, t), var in ae.items():
            ae_result[e, p, t] = solver.Value(var)
        for (p, s, t), var in as_.items():
            as_result[p, s, t] = solver.Value(var)

        print(f"Status: {status_str}, Objectif: {solver.ObjectiveValue()}, Temps: {comp_time:.2f}s")
        return Solution(ae_result, as_result, solver.ObjectiveValue(), status_str, comp_time)

    def _add_constraints(self, model, ae, as_):
        # C1-C2: Voeux 1 et 2 obligatoires
        for e, s in enumerate(self.students):
            for voeu in s.voeux[:2]:
                model.Add(sum(ae[e, voeu, t] for t in self.valid_slots[e]) == 1)

        # C3: Exactement 4 presentations
        for e in range(self.E):
            model.Add(sum(ae[e, p, t] for p in self.voeux_set[e]
                         for t in self.valid_slots[e]) == 4)

        # C4: Pas de repetition
        for e in range(self.E):
            for p in self.voeux_set[e]:
                model.Add(sum(ae[e, p, t] for t in self.valid_slots[e]) <= 1)

        # C5: Un slot = une presentation
        for e in range(self.E):
            for t in self.valid_slots[e]:
                model.Add(sum(ae[e, p, t] for p in self.voeux_set[e]) == 1)

        # C6: Une presentation max par salle par slot
        for s in range(self.S):
            for t in range(self.T):
                model.Add(sum(as_[p, s, t] for p in range(self.P)) <= 1)

        # C6b: Une salle max par presentation par slot
        for p in range(self.P):
            for t in range(self.T):
                model.Add(sum(as_[p, s, t] for s in range(self.S)) <= 1)

        # C7: Capacite des salles
        students_by_pres = {p: [] for p in range(self.P)}
        for e in range(self.E):
            for p in self.voeux_set[e]:
                students_by_pres[p].append(e)

        for p in range(self.P):
            for t in range(self.T):
                relevant = [e for e in students_by_pres[p] if t in self.valid_slots[e]]
                if relevant:
                    model.Add(
                        sum(ae[e, p, t] for e in relevant) <=
                        sum(self.rooms[s].capacity * as_[p, s, t] for s in range(self.S))
                    )

        # C8-C9: Max 1 TR et 1 FM par eleve
        for e in range(self.E):
            tr = [p for p in self.voeux_set[e] if p in TR_INDICES]
            fm = [p for p in self.voeux_set[e] if p in FM_INDICES]
            if tr:
                model.Add(sum(ae[e, p, t] for p in tr for t in self.valid_slots[e]) <= 1)
            if fm:
                model.Add(sum(ae[e, p, t] for p in fm for t in self.valid_slots[e]) <= 1)

    def verify_solution(self, solution: Solution) -> Dict[str, bool]:
        """Verifie les contraintes de la solution."""
        ae, as_ = solution.ae, solution.as_
        results = {}

        # Voeux 1-2
        results['voeu_1_ok'] = all(
            sum(ae[e, s.voeux[0], t] for t in self.valid_slots[e]) == 1
            for e, s in enumerate(self.students))
        results['voeu_2_ok'] = all(
            sum(ae[e, s.voeux[1], t] for t in self.valid_slots[e]) == 1
            for e, s in enumerate(self.students))

        # 4 presentations
        results['count_ok'] = all(
            sum(ae[e, p, t] for p in self.voeux_set[e] for t in self.valid_slots[e]) == 4
            for e in range(self.E))

        # Pas de repetition
        results['no_repeat'] = all(
            sum(ae[e, p, t] for t in self.valid_slots[e]) <= 1
            for e in range(self.E) for p in self.voeux_set[e])

        # Un slot = une presentation
        results['one_per_slot'] = all(
            sum(ae[e, p, t] for p in self.voeux_set[e]) == 1
            for e in range(self.E) for t in self.valid_slots[e])

        # Max 1 TR/FM
        results['max_1_tr'] = all(
            sum(ae[e, p, t] for p in self.voeux_set[e] if p in TR_INDICES
                for t in self.valid_slots[e]) <= 1
            for e in range(self.E))
        results['max_1_fm'] = all(
            sum(ae[e, p, t] for p in self.voeux_set[e] if p in FM_INDICES
                for t in self.valid_slots[e]) <= 1
            for e in range(self.E))

        all_ok = all(results.values())
        print(f"Verification: {'OK' if all_ok else 'ERREUR'} - {results}")
        return results
