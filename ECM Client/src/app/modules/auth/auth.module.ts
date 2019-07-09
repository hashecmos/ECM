import {NgModule} from '@angular/core';
import {FormsModule} from '@angular/forms';
import {CommonModule} from '@angular/common';
import {AuthRoute} from './auth.routes';
import {AuthComponent} from './auth-component';
import {SessionTimeoutComponent} from './session-timeout/session-timeout.component';
import {AuthFailureComponent} from './auth-failure/auth-failure.component';
import {LoginComponent} from './login/login.component';
import {UserService} from '../../services/user.service';
import {AuthGuardLoginService} from '../../services/auth-guard-login.service';

@NgModule({
  declarations: [
    AuthComponent,
    SessionTimeoutComponent,
    AuthFailureComponent,
    LoginComponent
  ],
  imports: [
    CommonModule,
    FormsModule,
    AuthRoute
  ],
  providers: [
    AuthGuardLoginService
  ]
})
export class AuthModule {
}
