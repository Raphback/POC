import { NgModule } from '@angular/core';
import { BrowserModule } from '@angular/platform-browser';
import { CommonModule } from '@angular/common';

import { AppRoutingModule } from './app-routing.module';
import { AppComponent } from './app.component';
import { VoeuFormComponent } from './components/voeu-form/voeu-form.component';
import { LoginComponent } from './components/login/login.component';
import { DatabaseAdminComponent } from './components/database-admin/database-admin.component';
import { StatisticsDashboardComponent } from './components/statistics-dashboard/statistics-dashboard.component';
import { VoeuConfirmationComponent } from './components/voeu-confirmation/voeu-confirmation.component';

import { HttpClientModule } from '@angular/common/http';
import { AdminDashboardComponent } from './components/admin-dashboard/admin-dashboard.component';
import { ReactiveFormsModule } from '@angular/forms';

@NgModule({
  declarations: [
    AppComponent,
    VoeuFormComponent,
    LoginComponent,
    AdminDashboardComponent,
    DatabaseAdminComponent,
    StatisticsDashboardComponent,
    VoeuConfirmationComponent
  ],
  imports: [
    BrowserModule,
    CommonModule,
    AppRoutingModule,
    HttpClientModule,
    ReactiveFormsModule
  ],

  providers: [],
  bootstrap: [AppComponent]
})
export class AppModule { }
