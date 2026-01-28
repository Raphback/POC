import { Component, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { ApiService } from '../../services/api.service';
import { Etudiant, Activite } from '../../models/models';

@Component({
  selector: 'app-voeu-confirmation',
  templateUrl: './voeu-confirmation.component.html',
  styleUrls: ['./voeu-confirmation.component.css']
})
export class VoeuConfirmationComponent implements OnInit {
  etudiant: Etudiant | null = null;
  voeux: Activite[] = [];

  constructor(private router: Router, private api: ApiService) {}

  ngOnInit(): void {
    const state = window.history.state;
    if (state?.etudiant) {
      this.etudiant = state.etudiant;
      this.voeux = state.voeux || [];
    } else {
      const user = localStorage.getItem('user');
      if (user) this.etudiant = JSON.parse(user);
      if (!this.voeux.length) this.router.navigate(['/voeux']);
    }
  }

  retourAccueil(): void { this.router.navigate(['/login']); }
  seDeconnecter(): void { this.api.logout(); this.router.navigate(['/login']); }
}
