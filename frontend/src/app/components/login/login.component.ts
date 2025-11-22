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

    constructor(
        private fb: FormBuilder,
        private apiService: ApiService,
        private router: Router
    ) {
        this.loginForm = this.fb.group({
            matricule: ['', Validators.required],
            nom: ['', Validators.required]
        });
    }

    onSubmit(): void {
        if (this.loginForm.valid) {
            const { matricule, nom } = this.loginForm.value;
            this.apiService.login(matricule, nom).subscribe({
                next: (response) => {
                    // Token is stored by ApiService
                    this.router.navigate(['/voeux']);
                },
                error: (err) => {
                    this.errorMessage = 'Identifiants incorrects. Veuillez réessayer.';
                    console.error('Login error', err);
                }
            });
        }
    }
}
