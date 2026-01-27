import { Component } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { Router } from '@angular/router';
import { ApiService } from '../../services/api.service';

@Component({
    selector: 'app-login',
    templateUrl: './login.component.html',
    styleUrls: ['./login.component.css']
})
export class LoginComponent {
    loginForm: FormGroup;
    errorMessage: string = '';

    loginMode: 'student' | 'admin' | 'viewer' = 'student';

    constructor(
        private fb: FormBuilder,
        private apiService: ApiService,
        private router: Router
    ) {
        this.loginForm = this.fb.group({
            matricule: ['', Validators.required],
            username: [''],
            password: ['']
        });
    }

    setLoginMode(mode: 'student' | 'admin' | 'viewer'): void {
        this.loginMode = mode;
        this.errorMessage = '';
        
        // Clear all validators first
        this.loginForm.get('matricule')?.clearValidators();
        this.loginForm.get('username')?.clearValidators();
        this.loginForm.get('password')?.clearValidators();

        if (mode === 'student') {
            this.loginForm.get('matricule')?.setValidators(Validators.required);
        } else {
            // Both admin and viewer need username/password
            this.loginForm.get('username')?.setValidators(Validators.required);
            this.loginForm.get('password')?.setValidators(Validators.required);
        }

        this.loginForm.get('matricule')?.updateValueAndValidity();
        this.loginForm.get('username')?.updateValueAndValidity();
        this.loginForm.get('password')?.updateValueAndValidity();
    }

    onSubmit(): void {
        if (this.loginForm.valid) {
            if (this.loginMode === 'admin') {
                const { username, password } = this.loginForm.value;
                this.apiService.loginAdmin(username, password).subscribe({
                    next: () => {
                        this.router.navigate(['/database']);
                    },
                    error: (err) => {
                        this.errorMessage = 'Identifiants Admin incorrects.';
                        console.error('Admin Login error', err);
                    }
                });
            } else if (this.loginMode === 'viewer') {
                const { username, password } = this.loginForm.value;
                this.apiService.loginViewer(username, password).subscribe({
                    next: () => {
                        this.router.navigate(['/viewer']);
                    },
                    error: (err) => {
                        this.errorMessage = 'Identifiants Professeur incorrects.';
                        console.error('Viewer Login error', err);
                    }
                });
            } else {
                const { matricule } = this.loginForm.value;
                this.apiService.login(matricule, '').subscribe({
                    next: () => {
                        this.router.navigate(['/voeux']);
                    },
                    error: (err) => {
                        this.errorMessage = 'Identifiant incorrect. Veuillez réessayer.';
                        console.error('Login error', err);
                    }
                });
            }
        }
    }
}
