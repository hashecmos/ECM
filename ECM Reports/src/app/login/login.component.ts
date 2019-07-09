import { Component, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { FormBuilder, ReactiveFormsModule } from '@angular/forms';
import * as $ from 'jquery';
import { UserService } from '../services/user.service';
import { Login } from '../models/login.model';
/**
*  This class represents the lazy loaded LoginComponent.
*/

@Component({
  selector: 'app-login-cmp',
  templateUrl: 'login.component.html',
  styleUrls : ['./login.component.css']
})

export class LoginComponent implements OnInit {
  private user: any = localStorage.getItem('user');
  private loggedIn = false;
  private loginMsg = 'Logging In. Please Wait...';
  private intLogin = false;
  public login = new Login();

  constructor(private fb: FormBuilder, private us: UserService, private router: Router, private rfm: ReactiveFormsModule) {
    if (this.user) {
      this.loggedIn = true;
    } else {

    }
  }
  loginUser() {
     this.us.logIn($('#username').val(), $('#pasword').val()).subscribe(data => {
     this.user = data;
      localStorage.setItem('user', JSON.stringify(this.user));
      this.router.navigateByUrl(`/dashboard/blankpage`);
      // window.location.reload();
     },
     err => {
       this.loginMsg = 'Error logging in! Please try later';
       console.log(err);
     });
  }

  ngOnInit() {}

  ngAfterViewInit() {
    if (this.intLogin) {
      this.us.logIn('abc', 'def').subscribe(data => {
        this.user = data;
        localStorage.setItem('user', JSON.stringify(this.user));
        this.router.navigateByUrl(`/dashboard/blankpage`);
      },
      err => { this.loginMsg = 'Error logging in! Please try later'; });
    }
  }

}
