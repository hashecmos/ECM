import {Component} from '@angular/core';
import {Router} from '@angular/router';
import {CoreService} from '../../../services/core.service';


@Component({
  templateUrl: './auth-failure.component.html',
})
export class AuthFailureComponent {
  constructor(private router: Router, private coreService: CoreService) {

  }

  login() {
    this.router.navigate(['/']);
  }
}
