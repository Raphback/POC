import { TestBed } from '@angular/core/testing';
import { HttpClientTestingModule, HttpTestingController } from '@angular/common/http/testing';
import { ApiService } from './api.service';

describe('ApiService', () => {
  let service: ApiService;
  let httpMock: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [HttpClientTestingModule],
      providers: [ApiService]
    });
    service = TestBed.inject(ApiService);
    httpMock = TestBed.inject(HttpTestingController);
    localStorage.clear();
  });

  afterEach(() => {
    httpMock.verify();
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });

  it('should login and store token', () => {
    const mockResponse = { token: 'fake-jwt-token', etudiant: { nom: 'Dupont' } };
    const matricule = 'M123';
    const nom = 'Dupont';

    service.login(matricule, nom).subscribe(response => {
      expect(response.token).toBe('fake-jwt-token');
      expect(localStorage.getItem('token')).toBe('fake-jwt-token');
    });

    const req = httpMock.expectOne('http://localhost:8080/api/auth/login');
    expect(req.request.method).toBe('POST');
    req.flush(mockResponse);
  });

  it('should logout and clear localStorage', () => {
    localStorage.setItem('token', 'some-token');
    service.logout();
    expect(localStorage.getItem('token')).toBeNull();
  });

  it('should return isAdmin true if admin in localStorage', () => {
    localStorage.setItem('admin', JSON.stringify({ username: 'admin', role: 'ROLE_ADMIN' }));
    expect(service.isAdmin()).toBeTrue();
  });
});
