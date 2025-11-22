import { Component, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { ApiService } from '../../services/api.service';
import { Etudiant, Activite } from '../../models/models';

@Component({
    selector: 'app-voeu-confirmation',
    templateUrl: './voeu-confirmation.component.html',
    styleUrls: ['./voeu-confirmation.component.css']
})
export class VoeuConfirmationComponent implements OnInit {
    etudiant: Etudiant | null = null;
    voeux: Activite[] = [];
    option: string = '';

    constructor(
        private router: Router,
        private apiService: ApiService
    ) { }

    ngOnInit(): void {
        // Récupérer les données depuis l'historique de navigation
        const state = window.history.state;
        console.log('📦 State reçu:', state);

        if (state && state.etudiant) {
            this.etudiant = state.etudiant;
            this.voeux = state.voeux || [];
            this.option = state.option || '';
            console.log('✅ Données chargées:', this.etudiant, this.voeux, this.option);
        } else {
            // Si pas de données, essayer de récupérer depuis localStorage
            const userJson = localStorage.getItem('user');
            if (userJson) {
                this.etudiant = JSON.parse(userJson);
            }

            // Si toujours pas de données complètes, rediriger
            if (!this.voeux || this.voeux.length === 0) {
                console.warn('⚠️ Pas de données de vœux, redirection vers /voeux');
                this.router.navigate(['/voeux']);
            }
        }
    }

    retourAccueil(): void {
        this.router.navigate(['/voeux']);
    }

    seDeconnecter(): void {
        this.apiService.logout();
        this.router.navigate(['/login']);
    }
}
