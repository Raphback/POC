import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { VoeuFormComponent } from './components/voeu-form/voeu-form.component';
import { LoginComponent } from './components/login/login.component';
import { AdminDashboardComponent } from './components/admin-dashboard/admin-dashboard.component';
import { DatabaseAdminComponent } from './components/database-admin/database-admin.component';
import { StatisticsDashboardComponent } from './components/statistics-dashboard/statistics-dashboard.component';
import { VoeuConfirmationComponent } from './components/voeu-confirmation/voeu-confirmation.component';
import { AuthGuard } from './auth.guard';
import { ViewerDashboardComponent } from './components/viewer-dashboard/viewer-dashboard.component';

const routes: Routes = [
  { path: 'login', component: LoginComponent },
  { path: 'voeux', component: VoeuFormComponent, canActivate: [AuthGuard] },
  { path: 'confirmation', component: VoeuConfirmationComponent, canActivate: [AuthGuard] },
  { path: 'admin', component: AdminDashboardComponent, canActivate: [AuthGuard] },
  { path: 'database', component: DatabaseAdminComponent, canActivate: [AuthGuard] },
  { path: 'statistics', component: StatisticsDashboardComponent, canActivate: [AuthGuard] },
  { path: 'viewer', component: ViewerDashboardComponent, canActivate: [AuthGuard] },
  { path: '', redirectTo: '/login', pathMatch: 'full' }
];

@NgModule({
  imports: [RouterModule.forRoot(routes)],
  exports: [RouterModule]
})
export class AppRoutingModule { }
