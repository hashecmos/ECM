import {UserService} from '../../../services/user.service';
import {
  Component, ElementRef, HostListener, Renderer, ViewChild, AfterViewInit, OnDestroy,
  OnInit, AfterContentInit
} from '@angular/core';
import * as global from "../../../global.variables";
import {
  Router, NavigationStart, NavigationCancel, NavigationEnd
} from '@angular/router';
import {CoreService} from "../../../services/core.service";

@Component({
  selector: 'app-home',
  templateUrl: './home.component.html',
  styleUrls: ['./home.component.scss']
})
export class HomeComponent implements  OnDestroy, OnInit, AfterContentInit {
  menuClick: boolean;

  menuButtonClick: boolean;

  topbarMenuButtonClick: boolean;

  topbarMenuClick: boolean;

  topbarMenuActive: boolean;

  activeTopbarItem: Element;

  // layoutStatic: boolean;
  // 10th-dec
  layoutStatic = true;

  sidebarActive: boolean;

  mobileMenuActive: boolean;

  darkMenu: boolean;

  isRTL: boolean;

  timeout: any;
  username: any;
  timeLimit = 900000;

  constructor(public renderer: Renderer, private router: Router, private us: UserService,
              private coreService: CoreService) {
  }

  ngAfterContentInit() {
    this.setSessionTimeout();
  }

  ngOnInit() {
    this.username = global.username;
    // this.us.logIn(this.username, 'def').subscribe(data => {
    //   localStorage.setItem('user', JSON.stringify(data));
    // })
  }


  setSessionTimeout() {
    this.timeout = setTimeout(() => {
      localStorage.removeItem('user');
      this.router.navigate(['/auth/session-timeout']);
    }, this.timeLimit);
  }

  resetSessionTimeout() {
    this.clearSessionTimeout();
    this.setSessionTimeout();
  }

  clearSessionTimeout() {
    clearTimeout(this.timeout);
  }

  ngOnDestroy() {
    this.clearSessionTimeout();
  }

  @HostListener('click', ['$event'])
  uiClick(event: MouseEvent) {
    this.resetSessionTimeout();

  }

  onWrapperClick() {
    if (!this.menuClick && !this.menuButtonClick) {
      this.mobileMenuActive = false;
    }

    if (!this.topbarMenuClick && !this.topbarMenuButtonClick) {
      this.topbarMenuActive = false;
      this.activeTopbarItem = null;
    }

    this.menuClick = false;
    this.menuButtonClick = false;
    this.topbarMenuClick = false;
    this.topbarMenuButtonClick = false;
  }

  onMenuButtonClick(event: Event) {
    this.menuButtonClick = true;

    if (this.isMobile()) {
      this.mobileMenuActive = !this.mobileMenuActive;
    }

    event.preventDefault();
  }

  onTopbarMobileMenuButtonClick(event: Event) {
    this.topbarMenuButtonClick = true;
    this.topbarMenuActive = !this.topbarMenuActive;
    event.preventDefault();
  }

  onTopbarRootItemClick(event: Event, item: Element) {
    if (this.activeTopbarItem === item) {
      this.activeTopbarItem = null;
    } else {
      this.activeTopbarItem = item;
    }

    event.preventDefault();
  }

  onTopbarMenuClick(event: Event) {
    this.topbarMenuClick = true;
  }

  onSidebarClick(event: Event) {
    this.menuClick = true;
  }

  onToggleMenuClick(event: Event) {
    this.layoutStatic = !this.layoutStatic;
  }

  isMobile() {
    return window.innerWidth <= 1024;
  }
}
