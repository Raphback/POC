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
  errorMessage = '';
  loginMode: 'student' | 'admin' | 'viewer' = 'student';

  private routes = { student: '/voeux', admin: '/database', viewer: '/viewer' };
  private errors = { student: 'Identifiant incorrect.', admin: 'Identifiants Admin incorrects.', viewer: 'Identifiants Professeur incorrects.' };

  constructor(private fb: FormBuilder, private api: ApiService, private router: Router) {
    this.loginForm = this.fb.group({ matricule: [''], username: [''], password: [''] });
    this.setLoginMode('student');
  }

  setLoginMode(mode: 'student' | 'admin' | 'viewer'): void {
    this.loginMode = mode;
    this.errorMessage = '';
    ['matricule', 'username', 'password'].forEach(f => this.loginForm.get(f)?.clearValidators());

    if (mode === 'student') {
      this.loginForm.get('matricule')?.setValidators(Validators.required);
    } else {
      this.loginForm.get('username')?.setValidators(Validators.required);
      this.loginForm.get('password')?.setValidators(Validators.required);
    }
    ['matricule', 'username', 'password'].forEach(f => this.loginForm.get(f)?.updateValueAndValidity());
  }

  onSubmit(): void {
    if (!this.loginForm.valid) return;
    const { matricule, username, password } = this.loginForm.value;

    const login$ = this.loginMode === 'student' ? this.api.login(matricule)
                 : this.loginMode === 'admin' ? this.api.loginAdmin(username, password)
                 : this.api.loginViewer(username, password);

    login$.subscribe({
      next: () => this.router.navigate([this.routes[this.loginMode]]),
      error: () => this.errorMessage = this.errors[this.loginMode]
    });
  }
}
