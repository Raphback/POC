import { ComponentFixture, TestBed } from '@angular/core/testing';
import { LoginComponent } from './login.component';
import { ReactiveFormsModule, FormsModule } from '@angular/forms';
import { HttpClientTestingModule } from '@angular/common/http/testing';
import { RouterTestingModule } from '@angular/router/testing';
import { ApiService } from '../../services/api.service';
import { of } from 'rxjs';

describe('LoginComponent', () => {
    let component: LoginComponent;
    let fixture: ComponentFixture<LoginComponent>;
    let apiService: ApiService;

    beforeEach(async () => {
        await TestBed.configureTestingModule({
            declarations: [LoginComponent],
            imports: [
                ReactiveFormsModule,
                FormsModule,
                HttpClientTestingModule,
                RouterTestingModule
            ],
            providers: [ApiService]
        })
            .compileComponents();

        fixture = TestBed.createComponent(LoginComponent);
        component = fixture.componentInstance;
        apiService = TestBed.inject(ApiService);
        fixture.detectChanges();
    });

    it('should create', () => {
        expect(component).toBeTruthy();
    });

    it('should have invalid form when empty', () => {
        expect(component.matricule).toBe('');
        expect(component.nom).toBe('');
    });

    it('should call apiService.login on submit', () => {
        const loginSpy = spyOn(apiService, 'login').and.returnValue(of({ token: 'abc' }));
        component.matricule = 'M123';
        component.nom = 'Dupont';
        component.onLogin();
        expect(loginSpy).toHaveBeenCalledWith('M123', 'Dupont');
    });
});
