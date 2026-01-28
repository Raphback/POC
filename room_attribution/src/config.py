"""
Configuration centralisee pour FESUP 2026 Optimizer.

Ce module contient toutes les constantes et parametres configurables
du systeme d'attribution des salles.
"""

# Nombre de presentations par type
N_CONFERENCES = 19  # Conf1-Conf19
N_TR = 6            # TR1-TR6 (Tables Rondes)
N_FM = 6            # FM1-FM6 (Flash Metiers)
N_PRESENTATIONS = N_CONFERENCES + N_TR + N_FM

# Indices des types de presentations
CONF_INDICES = set(range(0, N_CONFERENCES))
TR_INDICES = set(range(N_CONFERENCES, N_CONFERENCES + N_TR))
FM_INDICES = set(range(N_CONFERENCES + N_TR, N_PRESENTATIONS))
TR_FM_INDICES = TR_INDICES | FM_INDICES

# Noms des presentations
PRESENTATION_NAMES = (
    [f"Conf{i}" for i in range(1, N_CONFERENCES + 1)] +
    [f"TR{i}" for i in range(1, N_TR + 1)] +
    [f"FM{i}" for i in range(1, N_FM + 1)]
)

# Mapping nom -> index
PRESENTATION_MAPPING = {name: idx for idx, name in enumerate(PRESENTATION_NAMES)}

# Parametres d'optimisation
CAPACITY_BUFFER = 1.20  # Marge de 20% pour compenser les absences
N_TIME_SLOTS = 5        # Nombre de slots par demi-journee
N_PRESENTATIONS_PER_STUDENT = 4  # Chaque eleve assiste a 4 presentations

# Poids de l'objectif (penalites pour les voeux non prioritaires)
OBJECTIVE_WEIGHTS = {
    3: 1,   # Voeu 3: penalite faible
    4: 5,   # Voeu 4: penalite moyenne
    5: 10,  # Voeu 5: penalite forte (a eviter)
}

# Parametres du solveur
SOLVER_WORKERS = 8
SOLVER_LINEARIZATION_LEVEL = 2

# Noms des demi-journees
HALF_DAY_NAMES = {
    0: "Jour 1 - Matin (08h30-12h00)",
    1: "Jour 1 - Apres-midi (13h30-17h00)",
    2: "Jour 2 - Matin (08h30-12h00)",
    3: "Jour 2 - Apres-midi (13h30-17h00)"
}

# Horaires des slots
SLOT_TIMES_MATIN = {
    0: "08h30-09h15", 1: "09h15-10h00", 2: "10h00-10h45",
    3: "10h45-11h30", 4: "11h30-12h15"
}

SLOT_TIMES_APREM = {
    0: "13h30-14h15", 1: "14h15-15h00", 2: "15h00-15h45",
    3: "15h45-16h30", 4: "16h30-17h15"
}

# Mapping date+heure vers creneau
DATETIME_TO_TIMESLOT = {
    ("26/03/2026", "08:30"): 0, ("26/03/2026", "8:30"): 0,
    ("26/03/2026", "09:15"): 1, ("26/03/2026", "9:15"): 1,
    ("26/03/2026", "13:30"): 2, ("26/03/2026", "14:15"): 3,
    ("27/03/2026", "08:30"): 4, ("27/03/2026", "8:30"): 4,
    ("27/03/2026", "09:15"): 5, ("27/03/2026", "9:15"): 5,
    ("27/03/2026", "13:30"): 6, ("27/03/2026", "14:15"): 7,
}


def get_slot_time(half_day: int, slot: int) -> str:
    """Retourne l'horaire d'un slot selon la demi-journee."""
    times = SLOT_TIMES_MATIN if half_day % 2 == 0 else SLOT_TIMES_APREM
    return times.get(slot, f"Slot {slot}")


def get_valid_slots(timeslot: int) -> list:
    """Retourne les 4 slots valides pour un creneau donne."""
    if timeslot < 0:
        return []
    return [0, 1, 2, 3] if timeslot % 2 == 0 else [1, 2, 3, 4]


def get_timeslot(date: str, heure: str) -> int:
    """Retourne le creneau horaire pour une date et heure."""
    if not date or not heure:
        return -1
    key = (date.strip(), heure.strip())
    return DATETIME_TO_TIMESLOT.get(key, -1)


def get_presentation_type(pres_idx: int) -> str:
    """Retourne le type de presentation."""
    if pres_idx in CONF_INDICES:
        return "Conference"
    elif pres_idx in TR_INDICES:
        return "Table Ronde"
    return "Flash Metier"
