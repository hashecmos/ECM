import {Injectable} from '@angular/core';
import {CanActivate, Router} from '@angular/router';
import {UserService} from './user.service';

@Injectable()
export class AuthGuardLoginService implements CanActivate {
  constructor(private router: Router) {

  }

  canActivate() {
    console.log('AuthGuard#canActivate Login called ' + localStorage.getItem('user'));


    if (localStorage.getItem('user') !== null) {
      console.log('not null found in login');
      this.router.navigate(['/']);
      return false;
    }

    return true;


  }
}
