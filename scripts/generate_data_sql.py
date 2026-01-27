#!/usr/bin/env python
# -*- coding: utf-8 -*-
"""
Generate data.sql with H2 MERGE syntax for importing students from Excel files.
"""
import pandas as pd

def escape_sql(value):
    """Escape single quotes for SQL"""
    if pd.isna(value) or value is None:
        return None
    return str(value).replace("'", "''")

def determine_serie_bac(classe):
    """Determine série bac based on class name"""
    classe_upper = str(classe).upper()
    if 'STI' in classe_upper or 'STM' in classe_upper or 'ST2S' in classe_upper or 'STL' in classe_upper:
        return 'Technologique'
    return 'Générale'

def generate_matricule(index, lycee_prefix):
    """Generate unique matricule based on index and lycee"""
    return f"{lycee_prefix}{str(index).zfill(4)}"

def main():
    # Read Excel files
    print("Reading Excel files...")
    df_fauriel = pd.read_excel('../Inputs/LGT Fauriel FESUP 2026.xlsx')
    df_brassens = pd.read_excel('../Inputs/Lycée Georges Brassens 2026 - FESUP Saint-Etienne.xls')
    
    # Standardize column names
    df_fauriel.columns = ['etablissement', 'nom', 'prenom', 'ine', 'classe']
    df_brassens.columns = ['etablissement', 'nom', 'prenom', 'ine', 'classe']
    
    # Add lycee pattern for SQL lookup
    df_fauriel['lycee_pattern'] = 'Fauriel'
    df_brassens['lycee_pattern'] = 'Brassens'
    
    # Generate matricules
    df_fauriel['matricule'] = [generate_matricule(i+1, 'FAU') for i in range(len(df_fauriel))]
    df_brassens['matricule'] = [generate_matricule(i+1, 'BRA') for i in range(len(df_brassens))]
    
    # Combine
    df_all = pd.concat([df_fauriel, df_brassens], ignore_index=True)
    
    # Determine série bac
    df_all['serie_bac'] = df_all['classe'].apply(determine_serie_bac)
    
    # Assign demi-journée (1-4 based on class distribution)
    df_all['demi_journee'] = (df_all.index % 4) + 1
    
    print(f"Total students: {len(df_all)}")
    print(f"  - Fauriel: {len(df_fauriel)}")
    print(f"  - Brassens: {len(df_brassens)}")
    
    # Generate SQL with H2 MERGE syntax
    sql_lines = []
    sql_lines.append("-- ===========================================")
    sql_lines.append("-- Import des étudiants depuis les fichiers Excel")
    sql_lines.append("-- Total: {} étudiants ({} Fauriel + {} Brassens)".format(len(df_all), len(df_fauriel), len(df_brassens)))
    sql_lines.append("-- Généré automatiquement par scripts/generate_data_sql.py")
    sql_lines.append("-- ===========================================")
    sql_lines.append("")
    sql_lines.append("-- Note: Ce fichier est exécuté au démarrage par Spring Boot")
    sql_lines.append("-- Les lycées sont créés par DataInitializer avant l'exécution de ce script")
    sql_lines.append("")
    
    for _, row in df_all.iterrows():
        nom = escape_sql(row['nom'])
        prenom = escape_sql(row['prenom'])
        ine = escape_sql(row['ine'])
        classe = escape_sql(row['classe'])
        matricule = row['matricule']
        serie_bac = row['serie_bac']
        demi_journee = row['demi_journee']
        lycee_pattern = row['lycee_pattern']
        
        # Use MERGE INTO with KEY for upsert behavior in H2
        sql = f"MERGE INTO etudiant (matricule_csv, nom, prenom, ine, classe, serie_bac, demi_journee, lycee_id) KEY (matricule_csv) SELECT '{matricule}', '{nom}', '{prenom}', '{ine}', '{classe}', '{serie_bac}', '{demi_journee}', l.id FROM lycee l WHERE l.nom LIKE '%{lycee_pattern}%';"
        sql_lines.append(sql)
    
    # Write to data.sql
    output_file = '../backend/src/main/resources/data.sql'
    with open(output_file, 'w', encoding='utf-8') as f:
        f.write('\n'.join(sql_lines))
    
    print(f"\nSQL file generated: {output_file}")
    print(f"Total MERGE statements: {len(df_all)}")

if __name__ == '__main__':
    main()
