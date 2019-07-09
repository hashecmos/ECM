import {
  AfterViewInit, Component, ElementRef, HostListener, OnDestroy, OnInit, Renderer,
  ViewChild
} from '@angular/core';
import {GrowlService} from './services/growl.service';
import {Message} from 'primeng/primeng';
import {CoreService} from "./services/core.service";
import {NavigationCancel, NavigationEnd, NavigationStart, Router} from "@angular/router";

import * as $ from 'jquery';

@Component({
  selector: 'app-root',
  templateUrl: './app.component.html',
  styleUrls: ['./app.component.scss']
})
export class AppComponent implements OnInit, AfterViewInit, OnDestroy {

  menuClick: boolean;

  menuButtonClick: boolean;

  topbarMenuButtonClick: boolean;

  topbarMenuClick: boolean;

  topbarMenuActive: boolean;

  activeTopbarItem: Element;

  layoutStatic = true;

  sidebarActive: boolean;

  mobileMenuActive: boolean;

  darkMenu: boolean;

  isRTL: boolean;
  msgs: Message[] = [];
  loading = {loading: false};
  private subscriptions: any[]=[];
  @ViewChild('globalProgressBar')
  spinnerElement: ElementRef;

  constructor(public renderer: Renderer, private growlService: GrowlService, private coreService: CoreService,
              private router: Router) {
  }


  ngOnInit() {
    var preventBackspace = require('prevent-backspace');
    preventBackspace() ;
    this.growlService.growl$.subscribe((msg: Message) => {
      this.msgs = [];
      this.msgs.push(msg);
    });


    $(window).resize(function () {
      if (window.innerWidth < 800) {
        window.resizeTo(800, 800);
      }


    })


  }

  ngAfterViewInit() {
    this.router.events
      .subscribe((event) => {
        if (event instanceof NavigationStart) {
           this.coreService.progress = {busy: {}, message: '', backdrop: true};


        }
        else if (
          event instanceof NavigationEnd ||
          event instanceof NavigationCancel
        ) {


            this.loading.loading = false;
            this.coreService.progress = {busy: undefined, message: '', backdrop: true};

        }
      });
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

  addToSubscriptions(subscription) {
    this.subscriptions.push(subscription);
  }

  clearSubscriptions() {
    this.subscriptions.map(s => {
      s.unsubscribe();
    });
  }


  ngOnDestroy() {
    this.clearSubscriptions();
  }

}
