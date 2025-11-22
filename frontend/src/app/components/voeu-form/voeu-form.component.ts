import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { Router } from '@angular/router';
import { ApiService } from '../../services/api.service';
import { Activite, Etudiant, Lycee } from '../../models/models';

@Component({
  selector: 'app-voeu-form',
  templateUrl: './voeu-form.component.html',
  styleUrls: ['./voeu-form.component.css']
})
export class VoeuFormComponent implements OnInit {
  voeuForm: FormGroup;
  etudiant: Etudiant | null = null;
  lycees: Lycee[] = [];
  activites: Activite[] = [];

  // Filtered Lists
  conferences: Activite[] = [];
  tablesRondes: Activite[] = [];
  flashMetiers: Activite[] = [];

  // Options
  options = ['A', 'B', 'C', 'D'];
  selectedOption: string = '';

  errorMessage: string = '';
  successMessage: string = '';

  constructor(private fb: FormBuilder, private apiService: ApiService, private router: Router) {
    this.voeuForm = this.fb.group({
      matricule: ['', Validators.required],
      voeu1: ['', Validators.required],
      voeu2: ['', Validators.required],
      option: ['', Validators.required],
      // Dynamic fields for Voeux 3, 4, 5 will be handled manually or via form array
      voeu3: [''],
      voeu4: [''],
      voeu5: ['']
    });
  }

  ngOnInit(): void {
    this.loadReferentiel();

    // Auto-login if user is already authenticated
    const userJson = localStorage.getItem('user');
    if (userJson) {
      try {
        this.etudiant = JSON.parse(userJson);
        // Pre-fill matricule just in case
        if (this.etudiant && this.etudiant.matriculeCsv) {
          this.voeuForm.patchValue({ matricule: this.etudiant.matriculeCsv });
          // Refresh data from backend to ensure we have the latest (e.g. Lycee)
          this.rechercherEtudiant();
        }
      } catch (e) {
        console.error('Error parsing user from localStorage', e);
      }
    }
  }

  loadReferentiel(): void {
    this.apiService.getLycees().subscribe(data => this.lycees = data);
    this.apiService.getActivites().subscribe({
      next: (data) => {
        console.log('✅ Activités chargées:', data);
        this.activites = data;
        this.conferences = data.filter(a => a.type === 'CONFERENCE');
        this.tablesRondes = data.filter(a => a.type === 'TABLE_RONDE');
        this.flashMetiers = data.filter(a => a.type === 'FLASH_METIER');
        console.log('📊 Conférences:', this.conferences.length);
      },
      error: (err) => {
        console.error('❌ Erreur chargement activités:', err);
        this.errorMessage = 'Impossible de charger les activités. Vérifiez la connexion au serveur.';
      }
    });
  }

  rechercherEtudiant(): void {
    const matricule = this.voeuForm.get('matricule')?.value;
    if (matricule) {
      this.apiService.getEtudiant(matricule).subscribe({
        next: (data) => {
          this.etudiant = data;
          this.errorMessage = '';
        },
        error: (err) => {
          this.etudiant = null;
          this.errorMessage = 'Étudiant non trouvé.';
        }
      });
    }
  }

  onOptionChange(opt: string): void {
    this.selectedOption = opt;
    // Reset Voeux 3, 4, 5
    this.voeuForm.patchValue({ voeu3: '', voeu4: '', voeu5: '' });
  }

  onSubmit(): void {
    if (this.voeuForm.valid && this.etudiant) {
      // Basic validation: V1 != V2
      if (this.voeuForm.value.voeu1 === this.voeuForm.value.voeu2) {
        this.errorMessage = 'Les vœux 1 et 2 doivent être différents.';
        return;
      }
      const activitesIds = [
        this.voeuForm.value.voeu1,
        this.voeuForm.value.voeu2,
        this.voeuForm.value.voeu3,
        this.voeuForm.value.voeu4,
        this.voeuForm.value.voeu5
      ].filter(id => id); // Remove empty

      // Récupérer les objets activités complets pour l'affichage
      const selectedActivites = activitesIds.map(id => {
        return this.activites.find(a => a.id === parseInt(id));
      }).filter(a => a); // Retirer les undefined

      // Convertir les IDs en nombres pour l'API
      const numericIds = activitesIds.map(id => parseInt(id));

      this.apiService.saveVoeux(this.etudiant.id, numericIds).subscribe({
        next: () => {
          // Navigation vers la page de confirmation avec les données
          this.router.navigate(['/confirmation'], {
            state: {
              etudiant: this.etudiant,
              voeux: selectedActivites,
              option: this.selectedOption
            }
          });
        },
        error: (err) => {
          this.errorMessage = 'Erreur lors de l\'enregistrement.';
          this.successMessage = '';
        }
      });
    }
  }
}
