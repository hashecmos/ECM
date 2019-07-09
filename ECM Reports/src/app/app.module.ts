import { BrowserModule } from '@angular/platform-browser';
import { NgModule } from '@angular/core';
import { HttpModule } from '@angular/http';
import { RouterModule } from '@angular/router';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';
import { AppComponent } from './app.component';
import { routes } from './app.routes';
import { LoginModule } from './login/login.module';
import { SignupModule } from './signup/signup.module';
import { DashboardModule } from './dashboard/dashboard.module';
import {AccordionModule} from 'ngx-accordion';
import {UserService} from './services/user.service';
import { ReportService } from './services/reports.service';
import { CommonModule }  from '@angular/common';

@NgModule({
  declarations: [
    AppComponent
  ],
  imports: [
    BrowserModule,
    FormsModule,
    ReactiveFormsModule,
    CommonModule,
    HttpModule,
    RouterModule.forRoot(routes),
    LoginModule,
    SignupModule,
    DashboardModule,
    AccordionModule,
  ],
  providers: [UserService, ReportService],
  bootstrap: [AppComponent]
})
export class AppModule { }
