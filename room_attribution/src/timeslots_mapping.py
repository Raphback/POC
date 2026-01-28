"""
Module de mapping des créneaux horaires FESUP 2026.

Structure des créneaux:
- Chaque demi-journée a 5 slots de 45 minutes
- 2 groupes par demi-journée avec chevauchement:
  - Groupe "tôt" : slots 0, 1, 2, 3 (ex: 8h30-11h15 le matin)
  - Groupe "tard" : slots 1, 2, 3, 4 (ex: 9h15-12h00 le matin)

Créneaux FESUP 2026:
- Créneau 0: 26/03/2026 08:30 -> demi-journée 0, slots 0-3
- Créneau 1: 26/03/2026 09:15 -> demi-journée 0, slots 1-4
- Créneau 2: 26/03/2026 13:30 -> demi-journée 1, slots 0-3
- Créneau 3: 26/03/2026 14:15 -> demi-journée 1, slots 1-4
- Créneau 4: 27/03/2026 08:30 -> demi-journée 2, slots 0-3
- Créneau 5: 27/03/2026 09:15 -> demi-journée 2, slots 1-4
- Créneau 6: 27/03/2026 13:30 -> demi-journée 3, slots 0-3
- Créneau 7: 27/03/2026 14:15 -> demi-journée 3, slots 1-4
"""

# Mapping date+heure vers créneau
# Format clé: (date, heure) ou juste heure pour fallback
DATETIME_TO_TIMESLOT = {
    # Jour 1 - 26/03/2026
    ("26/03/2026", "08:30"): 0,
    ("26/03/2026", "8:30"): 0,
    ("26/03/2026", "09:15"): 1,
    ("26/03/2026", "9:15"): 1,
    ("26/03/2026", "13:30"): 2,
    ("26/03/2026", "14:15"): 3,
    
    # Jour 2 - 27/03/2026
    ("27/03/2026", "08:30"): 4,
    ("27/03/2026", "8:30"): 4,
    ("27/03/2026", "09:15"): 5,
    ("27/03/2026", "9:15"): 5,
    ("27/03/2026", "13:30"): 6,
    ("27/03/2026", "14:15"): 7,
}

# Mapping heure seule vers type (tôt=0, tard=1) pour fallback
HEURE_TO_TYPE = {
    "08:30": 0, "8:30": 0,
    "09:15": 1, "9:15": 1,
    "13:30": 0,
    "14:15": 1,
}

# Mapping heure seule vers demi-journée relative (matin=0, aprem=1)
HEURE_TO_HALFDAY_RELATIVE = {
    "08:30": 0, "8:30": 0,
    "09:15": 0, "9:15": 0,
    "13:30": 1,
    "14:15": 1,
}


def get_timeslot_for_school(etablissement: str, date: str = None, heure: str = None) -> int:
    """
    Retourne le créneau horaire basé sur la date et l'heure.
    
    Note: Le nom de l'établissement n'est plus utilisé car certains établissements
    ont des élèves à plusieurs créneaux (ex: Lycée François Mauriac à 09:15 ET 14:15).
    
    Args:
        etablissement: Nom de l'établissement (non utilisé, gardé pour compatibilité)
        date: Date au format JJ/MM/AAAA (optionnel mais recommandé)
        heure: Heure au format HH:MM (optionnel mais recommandé)
    
    Returns:
        Le numéro de créneau (0-7), ou -1 si non trouvé
    """
    # Si on n'a pas de date/heure, on ne peut pas déterminer le créneau
    if not date or not heure:
        print(f"⚠️ Date/heure manquante pour {etablissement}")
        return -1
    
    # Nettoyer
    date = date.strip()
    heure = heure.strip()
    
    # Chercher dans le mapping direct
    key = (date, heure)
    if key in DATETIME_TO_TIMESLOT:
        return DATETIME_TO_TIMESLOT[key]
    
    # Essayer de parser la date différemment
    # Format possible: "26/03/2026" ou avec espaces
    date_clean = date.replace(" ", "")
    key = (date_clean, heure)
    if key in DATETIME_TO_TIMESLOT:
        return DATETIME_TO_TIMESLOT[key]
    
    print(f"⚠️ Créneau non trouvé pour {etablissement}: date={date}, heure={heure}")
    return -1


def get_timeslot_from_datetime(date: str, heure: str) -> int:
    """
    Retourne le créneau horaire directement depuis date et heure.
    
    Args:
        date: Date au format JJ/MM/AAAA
        heure: Heure au format HH:MM
    
    Returns:
        Le numéro de créneau (0-7), ou -1 si non trouvé
    """
    return get_timeslot_for_school("", date, heure)


def get_half_day_for_timeslot(timeslot: int) -> int:
    """
    Retourne la demi-journée (0-3) pour un créneau donné.
    
    - Demi-journée 0: Jour 1 matin (créneaux 0, 1)
    - Demi-journée 1: Jour 1 après-midi (créneaux 2, 3)
    - Demi-journée 2: Jour 2 matin (créneaux 4, 5)
    - Demi-journée 3: Jour 2 après-midi (créneaux 6, 7)
    """
    if timeslot < 0:
        return -1
    return timeslot // 2


def get_valid_slots_for_timeslot(timeslot: int) -> list:
    """
    Retourne les 4 slots valides (sur 5) pour un créneau donné.
    
    - Créneaux pairs (0, 2, 4, 6) = arrivée tôt -> slots 0, 1, 2, 3
    - Créneaux impairs (1, 3, 5, 7) = arrivée tard -> slots 1, 2, 3, 4
    """
    if timeslot < 0:
        return []
    if timeslot % 2 == 0:  # Arrivée tôt
        return [0, 1, 2, 3]
    else:  # Arrivée tard
        return [1, 2, 3, 4]


def is_early_arrival(timeslot: int) -> bool:
    """Retourne True si c'est un créneau d'arrivée tôt."""
    return timeslot >= 0 and timeslot % 2 == 0
