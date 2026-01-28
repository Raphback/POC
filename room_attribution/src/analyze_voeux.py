"""
Analyse des voeux pour comprendre le problème d'infaisabilité
"""

from pathlib import Path
from collections import defaultdict, Counter
from data_loader import load_data
from timeslots_mapping import get_timeslot_info

# Chemins des fichiers
project_root = Path(__file__).parent.parent
students_csv = project_root / "Inputs" / "Eleves_Voeux.csv"
rooms_excel = project_root / "Inputs" / "capacites.xlsx"

print("="*60)
print("ANALYSE DES VOEUX FESUP 2026")
print("="*60)

# Charger les données
students, rooms, n_presentations, pres_mapping, pres_names = load_data(
    str(students_csv),
    str(rooms_excel)
)

# Grouper par créneau
students_by_timeslot = defaultdict(list)
for student in students:
    if student.timeslot >= 0:
        students_by_timeslot[student.timeslot].append(student)

print(f"\nCapacité totale des salles: {sum(r.capacity for r in rooms)} places")
print(f"Répartition par créneau:")
for ts in sorted(students_by_timeslot.keys()):
    print(f"  Créneau {ts}: {len(students_by_timeslot[ts])} élèves")

# Analyser le créneau le plus problématique (créneau 1 - 713 élèves)
test_timeslot = 1
students_group = students_by_timeslot[test_timeslot]
info = get_timeslot_info(test_timeslot)

print(f"\n" + "="*60)
print(f"ANALYSE DÉTAILLÉE DU CRÉNEAU {test_timeslot}")
print(f"{info['date']} {info['hours']} - {len(students_group)} élèves")
print("="*60)

# Compter les voeux 1
voeu1_counter = Counter()
for student in students_group:
    voeu1_counter[student.voeux[0]] += 1

# Compter les voeux 2
voeu2_counter = Counter()
for student in students_group:
    voeu2_counter[student.voeux[1]] += 1

print(f"\n🔴 TOP 10 PRÉSENTATIONS DEMANDÉES EN VOEU 1:")
for pres_idx, count in voeu1_counter.most_common(10):
    pres_name = pres_names[pres_idx]
    print(f"  {pres_name:15s}: {count:3d} élèves")

print(f"\n🟡 TOP 10 PRÉSENTATIONS DEMANDÉES EN VOEU 2:")
for pres_idx, count in voeu2_counter.most_common(10):
    pres_name = pres_names[pres_idx]
    print(f"  {pres_name:15s}: {count:3d} élèves")

# Vérifier les conflits
total_capacity = sum(r.capacity for r in rooms)
print(f"\n⚠️  CONFLITS POTENTIELS:")
print(f"Capacité totale disponible: {total_capacity} places")

max_voeu1 = voeu1_counter.most_common(1)[0][1]
max_voeu2 = voeu2_counter.most_common(1)[0][1]

print(f"\nMax élèves voulant la même présentation en voeu 1: {max_voeu1}")
print(f"Max élèves voulant la même présentation en voeu 2: {max_voeu2}")

if max_voeu1 > total_capacity:
    print(f"\n❌ PROBLÈME: {max_voeu1} élèves veulent la même présentation mais seulement {total_capacity} places!")
elif max_voeu1 > total_capacity * 0.8:
    print(f"\n⚠️  ATTENTION: {max_voeu1} élèves veulent la même présentation, proche de la capacité totale")
else:
    print(f"\n✅ OK: Demande max ({max_voeu1}) < capacité totale ({total_capacity})")

# Analyser les combinaisons voeu1 + voeu2
print(f"\n" + "="*60)
print("COMBINAISONS VOEU 1 + VOEU 2")
print("="*60)

combined_demand = defaultdict(int)
for student in students_group:
    combined_demand[(student.voeux[0], student.voeux[1])] += 1

print(f"\nTop 10 combinaisons (voeu1, voeu2):")
for (v1, v2), count in sorted(combined_demand.items(), key=lambda x: -x[1])[:10]:
    print(f"  ({pres_names[v1]}, {pres_names[v2]}): {count} élèves")

# Calculer la demande totale par présentation (tous voeux confondus)
all_voeux_demand = Counter()
for student in students_group:
    for voeu in student.voeux:
        all_voeux_demand[voeu] += 1

print(f"\n" + "="*60)
print("DEMANDE TOTALE PAR PRÉSENTATION (TOUS VOEUX)")
print("="*60)

for pres_idx, count in all_voeux_demand.most_common(15):
    pres_name = pres_names[pres_idx]
    # Nombre de sessions nécessaires si on divise par capacité moyenne
    avg_room_capacity = total_capacity / len(rooms)
    sessions_needed = int(count / avg_room_capacity) + 1
    print(f"  {pres_name:15s}: {count:4d} demandes ({sessions_needed} sessions estimées)")

print("\n" + "="*60)
