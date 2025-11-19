-- nettoyage
DROP TABLE IF EXISTS affectation CASCADE;
DROP TABLE IF EXISTS voeu CASCADE;
DROP TABLE IF EXISTS session CASCADE;
DROP TABLE IF EXISTS activite CASCADE;
DROP TABLE IF EXISTS etudiant CASCADE;
DROP TABLE IF EXISTS lycee CASCADE;
DROP TYPE IF EXISTS type_activite;

-- création des tables

CREATE TYPE type_activite AS ENUM ('CONFERENCE', 'TABLE_RONDE', 'FLASH_METIER');

CREATE TABLE lycee (
    id SERIAL PRIMARY KEY,
    nom VARCHAR(255) UNIQUE NOT NULL
);

CREATE TABLE etudiant (
    id SERIAL PRIMARY KEY,
    matricule_csv VARCHAR(50) UNIQUE NOT NULL, -- id du fichier Excel
    nom VARCHAR(100) NOT NULL,
    prenom VARCHAR(100) NOT NULL,
    lycee_id INTEGER REFERENCES lycee(id),
    classe VARCHAR(50), 
    demi_journee INTEGER CHECK (demi_journee BETWEEN 1 AND 4) 
);

CREATE TABLE activite (
    id SERIAL PRIMARY KEY,
    titre VARCHAR(255) NOT NULL,
    type type_activite NOT NULL
);

CREATE TABLE session (
    id SERIAL PRIMARY KEY,
    activite_id INTEGER REFERENCES activite(id),
    salle VARCHAR(50),       
    capacite_max INTEGER NOT NULL,
    debut TIMESTAMP NOT NULL, 
    fin TIMESTAMP NOT NULL
);

CREATE TABLE voeu (
    id SERIAL PRIMARY KEY,
    etudiant_id INTEGER REFERENCES etudiant(id),
    activite_id INTEGER REFERENCES activite(id),
    priorite INTEGER CHECK (priorite BETWEEN 1 AND 5), 
    UNIQUE (etudiant_id, priorite) 
);

CREATE TABLE affectation (
    id SERIAL PRIMARY KEY,
    etudiant_id INTEGER REFERENCES etudiant(id),
    session_id INTEGER REFERENCES session(id),
    UNIQUE (etudiant_id, session_id) 
);
