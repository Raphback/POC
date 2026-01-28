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
  conferences: Activite[] = [];
  tablesRondes: Activite[] = [];
  flashMetiers: Activite[] = [];
  errorMessage = '';
  successMessage = '';

  constructor(private fb: FormBuilder, private api: ApiService, private router: Router) {
    this.voeuForm = this.fb.group({
      matricule: ['', Validators.required],
      voeu1: ['', Validators.required], voeu2: ['', Validators.required],
      voeu3: ['', Validators.required], voeu4: ['', Validators.required], voeu5: ['', Validators.required]
    });
  }

  ngOnInit(): void {
    this.loadReferentiel();
    const user = localStorage.getItem('user');
    if (user) {
      try {
        this.etudiant = JSON.parse(user);
        if (this.etudiant?.matriculeCsv) {
          this.voeuForm.patchValue({ matricule: this.etudiant.matriculeCsv });
          this.rechercherEtudiant();
        }
      } catch (e) {}
    }
  }

  loadReferentiel(): void {
    this.api.getLycees().subscribe(d => this.lycees = d);
    this.api.getActivites().subscribe({
      next: d => {
        this.activites = d;
        this.conferences = d.filter(a => a.type === 'CONFERENCE');
        this.tablesRondes = d.filter(a => a.type === 'TABLE_RONDE');
        this.flashMetiers = d.filter(a => a.type === 'FLASH_METIER');
      },
      error: () => this.errorMessage = 'Impossible de charger les activites.'
    });
  }

  rechercherEtudiant(): void {
    const m = this.voeuForm.get('matricule')?.value;
    if (!m) return;
    this.api.getEtudiant(m).subscribe({
      next: d => { this.etudiant = d; this.errorMessage = ''; },
      error: () => { this.etudiant = null; this.errorMessage = 'Etudiant non trouve.'; }
    });
  }

  getAvailableConferences(field: string): Activite[] {
    return this.conferences.filter(c => !this.getSelectedIds(field).includes(c.id));
  }

  getAvailableActivities(field: string): Activite[] {
    return this.activites.filter(a => !this.getSelectedIds(field).includes(a.id));
  }

  private getSelectedIds(exclude: string): number[] {
    return Object.entries(this.voeuForm.value)
      .filter(([k, v]) => k !== exclude && k.startsWith('voeu') && v)
      .map(([, v]) => parseInt(v as string));
  }

  onSubmit(): void {
    if (!this.voeuForm.valid || !this.etudiant) return;

    const ids = [1, 2, 3, 4, 5].map(i => this.voeuForm.value[`voeu${i}`]);
    if (new Set(ids).size !== 5) {
      alert('Vous avez selectionne plusieurs fois la meme activite.');
      return;
    }

    const acts = ids.slice(2).map(id => this.activites.find(a => a.id == id));
    const types = acts.map(a => a?.type);
    const [nConf, nTR, nFM] = ['CONFERENCE', 'TABLE_RONDE', 'FLASH_METIER'].map(t => types.filter(x => x === t).length);

    if (nConf === 0) {
      alert('Vous devez choisir au moins une CONFERENCE dans vos voeux 3, 4 ou 5.');
      return;
    }

    const valid = nConf === 3 || (nConf === 2 && (nFM === 1 || nTR === 1)) || (nConf === 1 && nTR === 1 && nFM === 1);
    if (!valid) {
      this.errorMessage = 'Combinaison invalide. Respectez la regle 3-4-5.';
      return;
    }

    const numIds = ids.map(id => parseInt(id));
    const selected = numIds.map(id => this.activites.find(a => a.id === id)).filter(Boolean);

    this.api.saveVoeux(this.etudiant.id, numIds).subscribe({
      next: () => this.router.navigate(['/confirmation'], { state: { etudiant: this.etudiant, voeux: selected } }),
      error: e => {
        this.errorMessage = e?.error ? (typeof e.error === 'string' ? e.error : JSON.stringify(e.error)) : 'Erreur enregistrement.';
        this.successMessage = '';
      }
    });
  }
}
