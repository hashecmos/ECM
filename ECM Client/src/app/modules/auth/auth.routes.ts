import {Routes, RouterModule} from '@angular/router';
import {ModuleWithProviders} from '@angular/core';
import {AuthFailureComponent} from './auth-failure/auth-failure.component';
import {SessionTimeoutComponent} from './session-timeout/session-timeout.component';
import {AuthComponent} from './auth-component';
import {LoginComponent} from './login/login.component';
import {AuthGuardLoginService} from '../../services/auth-guard-login.service';

export const routes: Routes = [
  {
    path: '', component: AuthComponent,
    children: [
      {path: 'login', component: LoginComponent, canActivate: [AuthGuardLoginService]},
      {path: 'auth-failure', component: AuthFailureComponent},
      {path: 'session-timeout', component: SessionTimeoutComponent},
    ]
  },
];

export const AuthRoute: ModuleWithProviders = RouterModule.forChild(routes);
