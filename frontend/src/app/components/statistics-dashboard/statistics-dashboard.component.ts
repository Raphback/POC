import { Component, OnInit } from '@angular/core';
import { ApiService } from '../../services/api.service';

@Component({
    selector: 'app-statistics-dashboard',
    templateUrl: './statistics-dashboard.component.html',
    styleUrls: ['./statistics-dashboard.component.css']
})
export class StatisticsDashboardComponent implements OnInit {
    loading: boolean = false;
    statistics: any = null;
    error: string = '';

    constructor(private apiService: ApiService) { }

    ngOnInit(): void {
        this.loadStatistics();
    }

    loadStatistics(): void {
        this.loading = true;
        this.error = '';

        this.apiService.get('/api/admin/statistics').subscribe({
            next: (data: any) => {
                this.statistics = data;
                this.loading = false;
            },
            error: (err: any) => {
                this.error = `Erreur lors du chargement des statistiques: ${err.message}`;
                this.loading = false;
            }
        });
    }

    getObjectKeys(obj: any): string[] {
        return obj ? Object.keys(obj) : [];
    }

    getPercentageClass(percentage: number): string {
        if (percentage >= 80) return 'text-success';
        if (percentage >= 50) return 'text-warning';
        return 'text-danger';
    }
}
