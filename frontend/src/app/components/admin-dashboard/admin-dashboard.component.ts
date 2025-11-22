import { Component, OnInit } from '@angular/core';
import { ApiService } from '../../services/api.service';

@Component({
    selector: 'app-admin-dashboard',
    templateUrl: './admin-dashboard.component.html',
    styleUrls: ['./admin-dashboard.component.css']
})
export class AdminDashboardComponent implements OnInit {

    affectations: any[] = [];
    loading = false;
    message = '';

    constructor(private apiService: ApiService) { }

    ngOnInit(): void {
        this.loadAffectations();
    }

    loadAffectations(): void {
        this.apiService.getAffectations().subscribe({
            next: (data) => this.affectations = data,
            error: (err) => console.error('Erreur chargement affectations', err)
        });
    }

    runAssignment(): void {
        this.loading = true;
        this.message = 'Algorithme en cours...';
        this.apiService.runAssignment().subscribe({
            next: (res) => {
                this.message = 'Affectation terminée avec succès !';
                this.loading = false;
                this.loadAffectations();
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
            error: (err) => console.error('Erreur téléchargement PDF', err)
        });
    }
}
