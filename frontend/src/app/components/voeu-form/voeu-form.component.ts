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
      voeu3: ['', Validators.required],
      voeu4: ['', Validators.required],
      voeu5: ['', Validators.required]
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

  getAvailableConferences(currentField: string): Activite[] {
    const selectedIds = this.getSelectedIds(currentField);
    return this.conferences.filter(c => !selectedIds.includes(c.id));
  }

  getAvailableActivities(currentField: string): Activite[] {
    const selectedIds = this.getSelectedIds(currentField);
    return this.activites.filter(a => !selectedIds.includes(a.id));
  }

  private getSelectedIds(excludeField: string): number[] {
    const values: any = this.voeuForm.value;
    const ids: number[] = [];

    Object.keys(values).forEach(key => {
      if (key !== excludeField && key.startsWith('voeu') && values[key]) {
        ids.push(parseInt(values[key]));
      }
    });
    return ids;
  }

  onSubmit(): void {
    if (this.voeuForm.valid && this.etudiant) {
      const v1 = this.voeuForm.value.voeu1;
      const v2 = this.voeuForm.value.voeu2;
      const v3 = this.voeuForm.value.voeu3;
      const v4 = this.voeuForm.value.voeu4;
      const v5 = this.voeuForm.value.voeu5;

      const allWishes = [v1, v2, v3, v4, v5];

      // 1. Check for duplicates (Double check, though UI should prevent it)
      const uniqueWishes = new Set(allWishes);
      if (uniqueWishes.size !== allWishes.length) {
        alert('⚠️ Attention : Vous avez sélectionné plusieurs fois la même activité.\nVeuillez choisir des activités différentes pour chaque vœu.');
        return;
      }

      // 2. Check for at least one conference in 3, 4, 5
      // Retrieve activity objects to check types
      const act3 = this.activites.find(a => a.id == v3);
      const act4 = this.activites.find(a => a.id == v4);
      const act5 = this.activites.find(a => a.id == v5);

      const hasConfInDiscovery = (act3?.type === 'CONFERENCE') || (act4?.type === 'CONFERENCE') || (act5?.type === 'CONFERENCE');

      if (!hasConfInDiscovery) {
        alert('⚠️ Attention : Vous devez choisir au moins une CONFÉRENCE dans vos vœux 3, 4 ou 5.');
        return;
      }

      // Règle 3-4-5: appliquer la même logique que le backend pour éviter l'erreur
      const types = [act3?.type, act4?.type, act5?.type];
      const nbConf = types.filter(t => t === 'CONFERENCE').length;
      const nbTable = types.filter(t => t === 'TABLE_RONDE').length;
      const nbFlash = types.filter(t => t === 'FLASH_METIER').length;

      const valid = (nbConf === 3) ||
                    (nbConf === 2 && nbFlash === 1) ||
                    (nbConf === 2 && nbTable === 1) ||
                    (nbConf === 1 && nbTable === 1 && nbFlash === 1);

      if (!valid) {
        this.errorMessage = "La combinaison des vœux 3, 4 et 5 est invalide. Respectez la règle 3-4-5 (ex: 3 conférences, ou 2 conférences + 1 flash/table, ou 1 conférence + 1 table + 1 flash).";
        return;
      }

      // Prepare data for API
      const numericIds = allWishes.map(id => parseInt(id));

      // Retrieve full objects for confirmation page
      const selectedActivites = numericIds.map(id => this.activites.find(a => a.id === id)).filter(a => a);

      this.apiService.saveVoeux(this.etudiant.id, numericIds).subscribe({
        next: () => {
          // Navigation vers la page de confirmation
          this.router.navigate(['/confirmation'], {
            state: {
              etudiant: this.etudiant,
              voeux: selectedActivites
            }
          });
        },
        error: (err) => {
          // Afficher le message détaillé du backend s'il existe
          const serverMsg = err?.error ? (typeof err.error === 'string' ? err.error : JSON.stringify(err.error)) : null;
          this.errorMessage = serverMsg || 'Erreur lors de l\'enregistrement.';
          this.successMessage = '';
        }
      });
    }
  }
}
