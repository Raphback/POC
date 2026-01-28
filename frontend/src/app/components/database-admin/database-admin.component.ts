import { Component, OnInit } from '@angular/core';
import { ApiService } from '../../services/api.service';

interface Tab { name: string; data: any[]; columns: string[]; }

@Component({
  selector: 'app-database-admin',
  templateUrl: './database-admin.component.html',
  styleUrls: ['./database-admin.component.css']
})
export class DatabaseAdminComponent implements OnInit {
  activeTab = 'etudiants';
  loading = false;
  message = '';
  viewMode: 'data' | 'stats' = 'data';
  statsGlobal: any = null;
  statsLycee: any[] = [];
  statsClasse: any[] = [];
  affectationsList: any[] = [];

  tabs: Tab[] = [
    { name: 'etudiants', data: [], columns: ['id', 'matriculeCsv', 'nom', 'prenom', 'lycee', 'classe', 'serieBac', 'demiJournee'] },
    { name: 'activites', data: [], columns: ['id', 'titre', 'type', 'nbPlaces', 'salle'] },
    { name: 'lycees', data: [], columns: ['id', 'nom'] },
    { name: 'voeux', data: [], columns: ['id', 'etudiant', 'activite', 'rang'] },
    { name: 'algo', data: [], columns: [] },
    { name: 'affectations', data: [], columns: ['id', 'etudiant', 'activite', 'rangVoeu'] }
  ];

  private endpoints: Record<string, string> = {
    etudiants: '/api/admin/etudiants', activites: '/api/referentiel/activites',
    lycees: '/api/referentiel/lycees', voeux: '/api/admin/voeux', affectations: '/api/admin/affectations'
  };

  constructor(private api: ApiService) {}

  ngOnInit(): void { this.loadData(); }

  switchView(mode: 'data' | 'stats'): void {
    this.viewMode = mode;
    mode === 'data' ? this.loadData() : this.loadStats();
  }

  switchTab(tab: string): void {
    this.activeTab = tab;
    tab === 'algo' ? this.loadAffectations() : this.loadData();
  }

  loadAffectations(): void {
    this.loading = true;
    this.api.getAffectations().subscribe({
      next: d => { this.affectationsList = d; this.loading = false; },
      error: () => this.loading = false
    });
  }

  runAssignment(): void {
    this.loading = true;
    this.message = 'Algorithme en cours...';
    this.api.runAssignment().subscribe({
      next: () => { this.message = 'Affectation terminee !'; this.loadAffectations(); },
      error: e => { this.message = 'Erreur: ' + e.message; this.loading = false; }
    });
  }

  downloadPdf(): void {
    this.api.exportPdf().subscribe({
      next: blob => this.downloadBlob(blob, 'convocations.pdf'),
      error: () => this.message = 'Erreur telechargement PDF'
    });
  }

  loadStats(): void {
    this.loading = true;
    this.api.getGlobalStats().subscribe(d => this.statsGlobal = d);
    this.api.getLyceeStats().subscribe(d => this.statsLycee = d);
    this.api.getClasseStats().subscribe({
      next: d => { this.statsClasse = d; this.loading = false; },
      error: () => { this.message = 'Erreur stats'; this.loading = false; }
    });
  }

  exportWishes(): void {
    this.api.exportWishes().subscribe({
      next: blob => this.downloadBlob(blob, `voeux_${new Date().toISOString().split('T')[0]}.xlsx`),
      error: () => this.message = 'Erreur export Excel'
    });
  }

  loadData(): void {
    const endpoint = this.endpoints[this.activeTab];
    if (!endpoint) { this.message = `Endpoint introuvable`; return; }

    this.loading = true;
    this.message = '';
    this.api.get(endpoint).subscribe({
      next: (d: any) => { const t = this.tabs.find(t => t.name === this.activeTab); if (t) t.data = d; this.loading = false; },
      error: (e: any) => { this.message = `Erreur: ${e.message}`; this.loading = false; }
    });
  }

  getActiveTabData(): Tab { return this.tabs.find(t => t.name === this.activeTab) || this.tabs[0]; }

  deleteItem(id: number): void {
    if (!confirm('Supprimer cet element ?')) return;
    const endpoints: Record<string, string> = {
      etudiants: `/api/admin/etudiants/${id}`,
      activites: `/api/admin/activites/${id}`,
      lycees: `/api/admin/lycees/${id}`
    };
    const ep = endpoints[this.activeTab];
    if (!ep) { this.message = 'Suppression non disponible'; return; }

    this.api.delete(ep).subscribe({
      next: () => { this.message = 'Supprime'; this.loadData(); },
      error: (e: any) => this.message = `Erreur: ${e.message}`
    });
  }

  exportData(): void {
    const tab = this.getActiveTabData();
    const csv = this.toCSV(tab.data, tab.columns);
    this.downloadBlob(new Blob(['\uFEFF' + csv], { type: 'text/csv;charset=utf-8;' }),
      `${this.activeTab}_${new Date().toISOString().split('T')[0]}.csv`);
  }

  private toCSV(data: any[], cols: string[]): string {
    const row = (item: any) => cols.map(c => {
      let v = c.split('.').reduce((a, p) => a?.[p], item);
      if (v && typeof v === 'object') v = v.nom && v.prenom ? `${v.prenom} ${v.nom}` : v.titre || v.nom;
      const s = String(v || '');
      return s.includes(';') || s.includes('"') ? `"${s.replace(/"/g, '""')}"` : s;
    }).join(';');
    return [cols.join(';'), ...data.map(row)].join('\n');
  }

  private downloadBlob(blob: Blob, filename: string): void {
    const url = URL.createObjectURL(blob);
    const a = document.createElement('a');
    a.href = url; a.download = filename; a.click();
    URL.revokeObjectURL(url);
  }
}
