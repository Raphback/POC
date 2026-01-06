import { Component, OnInit } from '@angular/core';
import { ApiService } from '../../services/api.service';

interface TabData {
    name: string;
    data: any[];
    columns: string[];
}

@Component({
    selector: 'app-database-admin',
    templateUrl: './database-admin.component.html',
    styleUrls: ['./database-admin.component.css']
})
export class DatabaseAdminComponent implements OnInit {
    activeTab: string = 'etudiants';
    loading: boolean = false;
    message: string = '';

    viewMode: 'data' | 'stats' = 'data';
    statsGlobal: any = null;
    statsLycee: any[] = [];
    statsClasse: any[] = [];
    affectationsList: any[] = [];

    tabs: TabData[] = [
        { name: 'etudiants', data: [], columns: ['id', 'matriculeCsv', 'nom', 'prenom', 'lycee', 'classe', 'serieBac', 'demiJournee'] },
        { name: 'activites', data: [], columns: ['id', 'titre', 'type', 'nbPlaces', 'salle'] },
        { name: 'lycees', data: [], columns: ['id', 'nom'] },
        { name: 'voeux', data: [], columns: ['id', 'etudiant', 'activite', 'rang'] },
        { name: 'algo', data: [], columns: [] },
        { name: 'affectations', data: [], columns: ['id', 'etudiant', 'activite', 'rangVoeu'] }
    ];

    constructor(private apiService: ApiService) { }

    ngOnInit(): void {
        this.loadData();
    }

    switchView(mode: 'data' | 'stats'): void {
        this.viewMode = mode;
        if (mode === 'data') {
            this.loadData();
        } else {
            this.loadStats();
        }
    }

    switchTab(tabName: string): void {
        this.activeTab = tabName;
        if (tabName === 'algo') {
            this.loadAffectationsForAlgo();
        } else {
            this.loadData();
        }
    }

    loadAffectationsForAlgo(): void {
        this.loading = true;
        this.apiService.getAffectations().subscribe({
            next: (data) => {
                this.affectationsList = data;
                this.loading = false;
            },
            error: (err) => {
                console.error('Erreur chargement affectations', err);
                this.loading = false;
            }
        });
    }

    runAssignment(): void {
        this.loading = true;
        this.message = 'Algorithme en cours...';
        this.apiService.runAssignment().subscribe({
            next: (res) => {
                this.message = 'Affectation terminée avec succès !';
                this.loadAffectationsForAlgo(); // Reload list
            },
            error: (err) => {
                this.message = 'Erreur : ' + err.message;
                this.loading = false;
            }
        });
    }

    downloadPdf(): void {
        this.apiService.exportPdf().subscribe({
            next: (blob) => {
                const url = window.URL.createObjectURL(blob);
                const a = document.createElement('a');
                a.href = url;
                a.download = 'convocations.pdf';
                a.click();
                window.URL.revokeObjectURL(url);
            },
            error: (err) => {
                this.message = 'Erreur téléchargement PDF';
            }
        });
    }

    loadStats(): void {
        this.loading = true;
        this.apiService.getGlobalStats().subscribe(data => this.statsGlobal = data);
        this.apiService.getLyceeStats().subscribe(data => this.statsLycee = data);
        this.apiService.getClasseStats().subscribe({
            next: (data) => {
                this.statsClasse = data;
                this.loading = false;
            },
            error: (err) => {
                this.message = 'Erreur chargement stats';
                this.loading = false;
            }
        });
    }

    exportWishes(): void {
        this.apiService.exportWishes().subscribe({
            next: (blob) => {
                const link = document.createElement('a');
                link.href = URL.createObjectURL(blob);
                link.download = `voeux_par_activite_${new Date().toISOString().split('T')[0]}.xlsx`;
                link.click();
            },
            error: (err) => {
                this.message = 'Erreur lors de l\'export Excel';
            }
        });
    }

    loadData(): void {
        this.loading = true;
        this.message = '';

        const endpoints: { [key: string]: string } = {
            'etudiants': '/api/admin/etudiants',
            'activites': '/api/referentiel/activites',
            'lycees': '/api/referentiel/lycees',
            'voeux': '/api/admin/voeux',
            'affectations': '/api/admin/affectations'
        };

        const endpoint = endpoints[this.activeTab];

        if (!endpoint) {
            this.message = `Endpoint introuvable pour ${this.activeTab}`;
            this.loading = false;
            return;
        }

        this.apiService.get(endpoint).subscribe({
            next: (data: any) => {
                const tab = this.tabs.find(t => t.name === this.activeTab);
                if (tab) {
                    tab.data = data as any[];
                }
                this.loading = false;
            },
            error: (err: any) => {
                this.message = `Erreur lors du chargement des données: ${err.message}`;
                this.loading = false;
            }
        });
    }

    getActiveTabData(): TabData {
        return this.tabs.find(t => t.name === this.activeTab) || this.tabs[0];
    }

    deleteItem(id: number): void {
        if (!confirm('Êtes-vous sûr de vouloir supprimer cet élément ?')) {
            return;
        }

        const endpoints: { [key: string]: string } = {
            'etudiants': `/api/admin/etudiants/${id}`,
            'activites': `/api/admin/activites/${id}`,
            'lycees': `/api/admin/lycees/${id}`
        };

        const endpoint = endpoints[this.activeTab];
        if (!endpoint) {
            this.message = 'Suppression non disponible pour cette table';
            return;
        }

        this.apiService.delete(endpoint).subscribe({
            next: () => {
                this.message = 'Élément supprimé avec succès';
                this.loadData();
            },
            error: (err: any) => {
                this.message = `Erreur lors de la suppression: ${err.message}`;
            }
        });
    }

    exportData(): void {
        const tab = this.getActiveTabData();
        const csv = this.convertToCSV(tab.data, tab.columns);
        // Add BOM for Excel UTF-8recognition
        const bom = '\uFEFF';
        const blob = new Blob([bom + csv], { type: 'text/csv;charset=utf-8;' });
        const link = document.createElement('a');
        link.href = URL.createObjectURL(blob);
        link.download = `${this.activeTab}_${new Date().toISOString().split('T')[0]}.csv`;
        link.click();
    }

    private convertToCSV(data: any[], columns: string[]): string {
        // Use semicolon delimiter for better Excel compatibility (French locale)
        const header = columns.join(';');
        const rows = data.map(item => {
            return columns.map(col => {
                let value = this.getNestedValue(item, col);

                // Handle objects (Etudiant, Activite, Lycee)
                if (value && typeof value === 'object') {
                    if (value.nom && value.prenom) {
                        value = `${value.prenom} ${value.nom}`;
                    } else if (value.titre) {
                        value = value.titre;
                    } else if (value.nom) {
                        value = value.nom;
                    }
                }

                // Escape quotes and wrap in quotes if value contains delimiter or quotes
                const stringValue = String(value || '');
                if (stringValue.includes(';') || stringValue.includes('"') || stringValue.includes('\n')) {
                    return `"${stringValue.replace(/"/g, '""')}"`;
                }
                return stringValue;
            }).join(';');
        });
        return [header, ...rows].join('\n');
    }

    private getNestedValue(obj: any, path: string): any {
        return path.split('.').reduce((acc, part) => acc && acc[part], obj);
    }
}
