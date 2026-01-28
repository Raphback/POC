import { Component, OnInit } from '@angular/core';
import { ApiService } from '../../services/api.service';

@Component({
  selector: 'app-statistics-dashboard',
  templateUrl: './statistics-dashboard.component.html',
  styleUrls: ['./statistics-dashboard.component.css']
})
export class StatisticsDashboardComponent implements OnInit {
  loading = false;
  statistics: any = null;
  error = '';

  constructor(private api: ApiService) {}

  ngOnInit(): void { this.loadStatistics(); }

  loadStatistics(): void {
    this.loading = true;
    this.error = '';
    this.api.get('/api/admin/statistics').subscribe({
      next: (d: any) => { this.statistics = d; this.loading = false; },
      error: (e: any) => { this.error = `Erreur: ${e.message}`; this.loading = false; }
    });
  }

  getObjectKeys(obj: any): string[] { return obj ? Object.keys(obj) : []; }

  getPercentageClass(p: number): string {
    return p >= 80 ? 'text-success' : p >= 50 ? 'text-warning' : 'text-danger';
  }
}
