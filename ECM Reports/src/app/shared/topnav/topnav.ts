import { Component} from '@angular/core';
import { Router } from '@angular/router';
import * as $ from 'jquery';
import { UserService } from '../../services/user.service';

@Component({
    selector: 'app-top-nav',
    templateUrl: 'topnav.html',
    styleUrls : ['./topnav.css']
})

export class TopNavComponent {
  public currentUser;

  constructor(private _router: Router, private userService: UserService) {
    this.currentUser = this.userService.getCurrentUser();
    }
  changeTheme(color: string): void {
    let link: any = $('<link>');
    link
      .appendTo('head')
      .attr({type : 'text/css', rel : 'stylesheet'})
      .attr('href', 'themes/app-' + color + '.css');
  }

  rtl(): void {
    let body: any = $('body');
    body.toggleClass('rtl');
  }

  sidebarToggler(): void  {
    let sidebar: any = $('#sidebar');
    let mainContainer: any = $('.main-container');
    sidebar.toggleClass('sidebar-left-zero');
    mainContainer.toggleClass('main-container-ml-zero');
  }
}
