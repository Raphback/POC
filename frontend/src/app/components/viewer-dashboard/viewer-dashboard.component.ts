import { Component, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { ApiService } from '../../services/api.service';
import { Etudiant } from '../../models/models';

@Component({
    selector: 'app-viewer-dashboard',
    templateUrl: './viewer-dashboard.component.html',
    styleUrls: ['./viewer-dashboard.component.css']
})
export class ViewerDashboardComponent implements OnInit {

    viewer: any;
    etudiants: Etudiant[] = [];
    voeux: any[] = [];
    stats: any = null;
    loading = true;
    error = '';

    constructor(
        private apiService: ApiService,
        private router: Router
    ) { }

    ngOnInit(): void {
        if (!this.apiService.isViewer()) {
            this.router.navigate(['/login']);
            return;
        }

        this.viewer = this.apiService.getViewer();
        this.loadData();
    }

    loadData(): void {
        if (!this.viewer || !this.viewer.lyceeId) return;

        this.loading = true;
        const lyceeId = this.viewer.lyceeId;

        this.apiService.getViewerEtudiants(lyceeId).subscribe({
            next: (data) => {
                this.etudiants = data;
                this.loadVoeux(lyceeId);
            },
            error: (err) => {
                this.error = 'Erreur lors du chargement des étudiants';
                this.loading = false;
                console.error(err);
            }
        });

        this.apiService.getViewerStats(lyceeId).subscribe({
            next: (data) => this.stats = data,
            error: (err) => console.error('Erreur chargement stats', err)
        });
    }

    loadVoeux(lyceeId: number): void {
        this.apiService.getViewerVoeux(lyceeId).subscribe({
            next: (data) => {
                this.voeux = data;
                this.loading = false;
            },
            error: (err) => {
                console.error('Erreur chargement voeux', err);
                this.loading = false;
            }
        });
    }

    getStudentVoeuxCount(etudiantId: number): number {
        return this.voeux.filter(v => v.etudiant.id === etudiantId).length;
    }

    hasVoeux(etudiantId: number): boolean {
        return this.getStudentVoeuxCount(etudiantId) > 0;
    }

    logout(): void {
        this.apiService.logout();
        this.router.navigate(['/login']);
    }
}
