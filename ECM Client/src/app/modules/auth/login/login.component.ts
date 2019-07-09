import {Component, OnInit} from '@angular/core';
import {Router} from '@angular/router';
import {UserService} from '../../../services/user.service';
import * as global from '../../../global.variables';

@Component({
  templateUrl: './login.component.html',
})
export class LoginComponent implements OnInit {
  username: any;

  constructor(private  us: UserService, private router: Router) {

  }

  ngOnInit() {
    this.login();
  }

  login() {
    console.log('login called');
    this.username = global.username;
    this.us.logIn(this.username, 'def').subscribe(data => {
        console.log('data01 ' + JSON.stringify(data));
        localStorage.setItem('user', JSON.stringify(data));
        this.us.assignGeneralSettings( () =>{
            if (this.us.defaultView === 'dashboard') {
              this.router.navigate(['/']);
            } else if (this.us.defaultView === 'workflow') {
              this.router.navigate(['/workflow']);
              this.us.defaultViewSubMenuExpanded = 1;
            } else if (this.us.defaultView === 'folders') {
              this.router.navigate(['/browse/browse-folders']);
              this.us.defaultViewSubMenuExpanded = 2;
            }
          });
      },
      err => {
        console.log('error01 ' + JSON.stringify(err));
        localStorage.removeItem('user');
        this.router.navigate(['/auth/auth-failure']);
      });
  }
}
