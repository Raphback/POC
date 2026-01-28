"""
Modeles de donnees pour FESUP 2026 Optimizer.
"""

from dataclasses import dataclass
from typing import List
import numpy as np

from config import get_valid_slots, CONF_INDICES, TR_INDICES, FM_INDICES


@dataclass
class Student:
    """Represente un eleve avec ses voeux de presentation."""
    id: int
    voeux: List[int]  # 5 voeux (indices de presentations 0-30)
    vague: int  # 1 ou 2 (deprecated)
    timeslot: int = 0  # Creneau horaire (0-7)

    def get_valid_slots(self) -> List[int]:
        return get_valid_slots(self.timeslot)

    def get_half_day(self) -> int:
        return self.timeslot // 2

    def get_conference_voeux(self) -> List[int]:
        return self.voeux[0:3]

    def get_trfm_voeux(self) -> List[int]:
        return self.voeux[3:5]


@dataclass
class Room:
    """Represente une salle avec sa capacite."""
    id: int
    name: str
    capacity: int


@dataclass
class Solution:
    """Resultat de l'optimisation."""
    ae: np.ndarray  # shape (E, P, T) - attributions eleves
    as_: np.ndarray  # shape (P, S, T) - attributions salles
    objective_value: float
    status: str
    computation_time: float
