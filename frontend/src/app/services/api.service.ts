import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Observable } from 'rxjs';
import { tap } from 'rxjs/operators';
import { Lycee, Activite, Etudiant } from '../models/models';

@Injectable({
  providedIn: 'root'
})
export class ApiService {

  private apiUrl = 'http://localhost:8080/api';

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

  loginAdmin(username: string, password: string): Observable<any> {
    return this.http.post(`${this.apiUrl}/auth/login/admin`, { username, password }).pipe(
      tap((response: any) => {
        if (response && response.token) {
          localStorage.setItem('token', response.token);
          localStorage.setItem('admin', JSON.stringify({ username: response.username, role: response.role }));
          localStorage.removeItem('user');
          localStorage.removeItem('viewer');
        }
      })
    );
  }

  loginViewer(email: string, password: string): Observable<any> {
    return this.http.post(`${this.apiUrl}/auth/login/viewer`, { username: email, password }).pipe(
      tap((response: any) => {
        if (response && response.token) {
          localStorage.setItem('token', response.token);
          localStorage.setItem('viewer', JSON.stringify({
            email: response.email,
            nom: response.nom,
            prenom: response.prenom,
            lyceeId: response.lyceeId,
            lyceeName: response.lyceeName
          }));
          localStorage.removeItem('user');
          localStorage.removeItem('admin');
        }
      })
    );
  }

  isAdmin(): boolean {
    return !!localStorage.getItem('admin');
  }

  isViewer(): boolean {
    return !!localStorage.getItem('viewer');
  }

  getViewer(): any {
    const viewer = localStorage.getItem('viewer');
    return viewer ? JSON.parse(viewer) : null;
  }

  getAdminRole(): string | null {
    const admin = localStorage.getItem('admin');
    return admin ? JSON.parse(admin).role : null;
  }

  logout(): void {
    localStorage.removeItem('token');
    localStorage.removeItem('user');
    localStorage.removeItem('admin');
    localStorage.removeItem('viewer');
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

  // Stats & Exports
  getGlobalStats(): Observable<any> {
    const headers = this.getAuthHeaders();
    return this.http.get(`${this.apiUrl}/stats/global`, { headers });
  }

  getLyceeStats(): Observable<any[]> {
    const headers = this.getAuthHeaders();
    return this.http.get<any[]>(`${this.apiUrl}/stats/lycee`, { headers });
  }

  getClasseStats(): Observable<any[]> {
    const headers = this.getAuthHeaders();
    return this.http.get<any[]>(`${this.apiUrl}/stats/classe`, { headers });
  }

  exportWishes(): Observable<Blob> {
    const headers = this.getAuthHeaders();
    return this.http.get(`${this.apiUrl}/stats/export`, { headers, responseType: 'blob' });
  }

  // Generic methods for database admin
  get<T>(endpoint: string): Observable<T> {
    const headers = this.getAuthHeaders();
    const fullUrl = endpoint.startsWith('/') ? endpoint : `/${endpoint}`;
    return this.http.get<T>(fullUrl, { headers });
  }

  delete(endpoint: string): Observable<any> {
    const headers = this.getAuthHeaders();
    const fullUrl = endpoint.startsWith('/') ? endpoint : `/${endpoint}`;
    return this.http.delete(fullUrl, { headers, responseType: 'text' });
  }

  getViewerEtudiants(lyceeId: number): Observable<Etudiant[]> {
    const headers = this.getAuthHeaders();
    return this.http.get<Etudiant[]>(`${this.apiUrl}/viewer/etudiants/${lyceeId}`, { headers });
  }

  getViewerVoeux(lyceeId: number): Observable<any[]> {
    const headers = this.getAuthHeaders();
    return this.http.get<any[]>(`${this.apiUrl}/viewer/voeux/${lyceeId}`, { headers });
  }

  getViewerStats(lyceeId: number): Observable<any> {
    const headers = this.getAuthHeaders();
    return this.http.get<any>(`${this.apiUrl}/viewer/stats/${lyceeId}`, { headers });
  }
}
