export interface Lycee {
    id: number;
    nom: string;
}

export interface Activite {
    id: number;
    titre: string;
    type: 'CONFERENCE' | 'TABLE_RONDE' | 'FLASH_METIER';
    salle?: string;
}

export interface Etudiant {
    id: number;
    matriculeCsv: string;
    nom: string;
    prenom: string;
    lycee: Lycee;
    serieBac: string;
    classe: string;
    demiJournee?: string;
}
