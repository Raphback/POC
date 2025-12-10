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

    isAdminMode: boolean = false;

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

    toggleAdminMode(): void {
        this.isAdminMode = !this.isAdminMode;
        if (this.isAdminMode) {
            this.loginForm.get('matricule')?.clearValidators();
            this.loginForm.get('username')?.setValidators(Validators.required);
            this.loginForm.get('password')?.setValidators(Validators.required);
        } else {
            this.loginForm.get('matricule')?.setValidators(Validators.required);
            this.loginForm.get('username')?.clearValidators();
            this.loginForm.get('password')?.clearValidators();
        }
        this.loginForm.get('matricule')?.updateValueAndValidity();
        this.loginForm.get('username')?.updateValueAndValidity();
        this.loginForm.get('password')?.updateValueAndValidity();
    }

    onSubmit(): void {
        if (this.loginForm.valid) {
            if (this.isAdminMode) {
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
