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

  constructor(private api: ApiService, private router: Router) {}

  ngOnInit(): void {
    if (!this.api.isViewer()) { this.router.navigate(['/login']); return; }
    this.viewer = this.api.getViewer();
    this.loadData();
  }

  loadData(): void {
    if (!this.viewer?.lyceeId) return;
    const id = this.viewer.lyceeId;
    this.loading = true;

    this.api.getViewerEtudiants(id).subscribe({
      next: d => { this.etudiants = d; this.loadVoeux(id); },
      error: () => { this.error = 'Erreur chargement etudiants'; this.loading = false; }
    });
    this.api.getViewerStats(id).subscribe({ next: d => this.stats = d });
  }

  loadVoeux(id: number): void {
    this.api.getViewerVoeux(id).subscribe({
      next: d => { this.voeux = d; this.loading = false; },
      error: () => this.loading = false
    });
  }

  getStudentVoeuxCount(id: number): number { return this.voeux.filter(v => v.etudiant.id === id).length; }
  hasVoeux(id: number): boolean { return this.getStudentVoeuxCount(id) > 0; }
  logout(): void { this.api.logout(); this.router.navigate(['/login']); }
}
