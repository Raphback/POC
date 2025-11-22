import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Observable } from 'rxjs';
import { tap } from 'rxjs/operators';
import { Lycee, Activite, Etudiant } from '../models/models';

@Injectable({
  providedIn: 'root'
})
export class ApiService {

  private apiUrl = '/api';

  constructor(private http: HttpClient) { }

  login(matricule: string, nom: string): Observable<any> {
    return this.http.post(`${this.apiUrl}/auth/login`, { matricule, nom }).pipe(
      tap((response: any) => {
        if (response && response.token) {
          localStorage.setItem('token', response.token);
          localStorage.setItem('user', JSON.stringify(response.etudiant));
        }
      })
    );
  }

  logout(): void {
    localStorage.removeItem('token');
    localStorage.removeItem('user');
  }

  getToken(): string | null {
    return localStorage.getItem('token');
  }

  getEtudiant(matricule: string): Observable<Etudiant> {
    const headers = this.getAuthHeaders();
    return this.http.get<Etudiant>(`${this.apiUrl}/voeux/etudiant/${matricule}`, { headers });
  }

  private getAuthHeaders(): HttpHeaders {
    const token = this.getToken();
    let headers = new HttpHeaders();
    if (token) {
      headers = headers.set('Authorization', `Bearer ${token}`);
    }
    return headers;
  }

  getLycees(): Observable<Lycee[]> {
    return this.http.get<Lycee[]>(`${this.apiUrl}/referentiel/lycees`);
  }

  getActivites(): Observable<Activite[]> {
    return this.http.get<Activite[]>(`${this.apiUrl}/referentiel/activites`);
  }

  saveVoeux(etudiantId: number, activitesIds: number[]): Observable<any> {
    const headers = this.getAuthHeaders();
    return this.http.post(`${this.apiUrl}/voeux`, { etudiantId, activitesIds }, { headers, responseType: 'text' });
  }

  importCsv(file: File): Observable<any> {
    const headers = this.getAuthHeaders();
    const formData = new FormData();
    formData.append('file', file);
    return this.http.post(`${this.apiUrl}/admin/import`, formData, { headers, responseType: 'text' });
  }

  runAssignment(): Observable<any> {
    const headers = this.getAuthHeaders();
    return this.http.post(`${this.apiUrl}/admin/assign`, {}, { headers, responseType: 'text' });
  }

  getAffectations(): Observable<any[]> {
    const headers = this.getAuthHeaders();
    return this.http.get<any[]>(`${this.apiUrl}/admin/affectations`, { headers });
  }

  exportPdf(): Observable<Blob> {
    const headers = this.getAuthHeaders();
    return this.http.get(`${this.apiUrl}/admin/export/pdf`, { headers, responseType: 'blob' });
  }

  // Generic methods for database admin
  get<T>(endpoint: string): Observable<T> {
    const headers = this.getAuthHeaders();
    return this.http.get<T>(`${endpoint}`, { headers });
  }

  delete(endpoint: string): Observable<any> {
    const headers = this.getAuthHeaders();
    return this.http.delete(endpoint, { headers, responseType: 'text' });
  }
}
