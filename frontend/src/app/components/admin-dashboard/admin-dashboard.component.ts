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

  constructor(private api: ApiService) {}

  ngOnInit(): void { this.loadAffectations(); }

  loadAffectations(): void {
    this.api.getAffectations().subscribe({
      next: d => this.affectations = d,
      error: e => console.error('Erreur affectations', e)
    });
  }

  runAssignment(): void {
    this.loading = true;
    this.message = 'Algorithme en cours...';
    this.api.runAssignment().subscribe({
      next: () => { this.message = 'Affectation terminee !'; this.loading = false; this.loadAffectations(); },
      error: e => { this.message = 'Erreur: ' + e.message; this.loading = false; }
    });
  }

  downloadPdf(): void {
    this.api.exportPdf().subscribe({
      next: blob => {
        const a = document.createElement('a');
        a.href = URL.createObjectURL(blob);
        a.download = 'convocations.pdf';
        a.click();
      },
      error: e => console.error('Erreur PDF', e)
    });
  }
}
