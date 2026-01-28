import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Observable } from 'rxjs';
import { tap } from 'rxjs/operators';
import { Lycee, Activite, Etudiant } from '../models/models';

@Injectable({ providedIn: 'root' })
export class ApiService {
  private api = 'http://localhost:8080/api';

  constructor(private http: HttpClient) {}

  private headers(): HttpHeaders {
    const token = localStorage.getItem('token');
    return token ? new HttpHeaders().set('Authorization', `Bearer ${token}`) : new HttpHeaders();
  }

  login(matricule: string): Observable<any> {
    return this.http.post<any>(`${this.api}/auth/login`, { matricule, nom: '' }).pipe(
      tap((r: any) => {
        if (r?.token) {
          localStorage.setItem('token', r.token);
          localStorage.setItem('user', JSON.stringify(r.etudiant));
        }
      })
    );
  }

  loginAdmin(username: string, password: string): Observable<any> {
    return this.http.post<any>(`${this.api}/auth/login/admin`, { username, password }).pipe(
      tap((r: any) => {
        if (r?.token) {
          localStorage.setItem('token', r.token);
          localStorage.setItem('admin', JSON.stringify({ username: r.username, role: r.role }));
          localStorage.removeItem('user');
          localStorage.removeItem('viewer');
        }
      })
    );
  }

  loginViewer(email: string, password: string): Observable<any> {
    return this.http.post<any>(`${this.api}/auth/login/viewer`, { username: email, password }).pipe(
      tap((r: any) => {
        if (r?.token) {
          localStorage.setItem('token', r.token);
          localStorage.setItem('viewer', JSON.stringify({
            email: r.email, nom: r.nom, prenom: r.prenom,
            lyceeId: r.lyceeId, lyceeName: r.lyceeName
          }));
          localStorage.removeItem('user');
          localStorage.removeItem('admin');
        }
      })
    );
  }

  logout(): void { ['token', 'user', 'admin', 'viewer'].forEach(k => localStorage.removeItem(k)); }
  isAdmin(): boolean { return !!localStorage.getItem('admin'); }
  isViewer(): boolean { return !!localStorage.getItem('viewer'); }
  getToken(): string | null { return localStorage.getItem('token'); }

  getViewer(): any {
    const v = localStorage.getItem('viewer');
    return v ? JSON.parse(v) : null;
  }

  getAdminRole(): string | null {
    const a = localStorage.getItem('admin');
    return a ? JSON.parse(a).role : null;
  }

  // Referentiel
  getLycees(): Observable<Lycee[]> { return this.http.get<Lycee[]>(`${this.api}/referentiel/lycees`); }
  getActivites(): Observable<Activite[]> { return this.http.get<Activite[]>(`${this.api}/referentiel/activites`); }

  // Voeux
  getEtudiant(matricule: string): Observable<Etudiant> {
    return this.http.get<Etudiant>(`${this.api}/voeux/etudiant/${matricule}`, { headers: this.headers() });
  }

  saveVoeux(etudiantId: number, activitesIds: number[]): Observable<any> {
    return this.http.post(`${this.api}/voeux`, { etudiantId, activitesIds }, { headers: this.headers(), responseType: 'text' });
  }

  // Admin
  importCsv(file: File): Observable<any> {
    const fd = new FormData();
    fd.append('file', file);
    return this.http.post(`${this.api}/admin/import`, fd, { headers: this.headers(), responseType: 'text' });
  }

  runAssignment(): Observable<any> {
    return this.http.post(`${this.api}/admin/assign`, {}, { headers: this.headers(), responseType: 'text' });
  }

  getAffectations(): Observable<any[]> {
    return this.http.get<any[]>(`${this.api}/admin/affectations`, { headers: this.headers() });
  }

  exportPdf(): Observable<Blob> {
    return this.http.get(`${this.api}/admin/export/pdf`, { headers: this.headers(), responseType: 'blob' });
  }

  // Stats
  getGlobalStats(): Observable<any> { return this.http.get(`${this.api}/stats/global`, { headers: this.headers() }); }
  getLyceeStats(): Observable<any[]> { return this.http.get<any[]>(`${this.api}/stats/lycee`, { headers: this.headers() }); }
  getClasseStats(): Observable<any[]> { return this.http.get<any[]>(`${this.api}/stats/classe`, { headers: this.headers() }); }
  exportWishes(): Observable<Blob> { return this.http.get(`${this.api}/stats/export`, { headers: this.headers(), responseType: 'blob' }); }

  // Generic
  get<T>(endpoint: string): Observable<T> {
    const url = endpoint.startsWith('/') ? endpoint : `/${endpoint}`;
    return this.http.get<T>(url, { headers: this.headers() });
  }

  delete(endpoint: string): Observable<any> {
    const url = endpoint.startsWith('/') ? endpoint : `/${endpoint}`;
    return this.http.delete(url, { headers: this.headers(), responseType: 'text' });
  }

  // Viewer
  getViewerEtudiants(lyceeId: number): Observable<Etudiant[]> {
    return this.http.get<Etudiant[]>(`${this.api}/viewer/etudiants/${lyceeId}`, { headers: this.headers() });
  }

  getViewerVoeux(lyceeId: number): Observable<any[]> {
    return this.http.get<any[]>(`${this.api}/viewer/voeux/${lyceeId}`, { headers: this.headers() });
  }

  getViewerStats(lyceeId: number): Observable<any> {
    return this.http.get<any>(`${this.api}/viewer/stats/${lyceeId}`, { headers: this.headers() });
  }
}
