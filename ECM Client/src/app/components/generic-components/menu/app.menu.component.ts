import {Component, Input, OnInit, AfterViewInit, OnDestroy, ElementRef, Renderer, ViewChild} from '@angular/core';
import {trigger, state, style, transition, animate} from '@angular/animations';
import {Location} from '@angular/common';
import {Router} from '@angular/router';
import {MenuItem} from 'primeng/primeng';
import {AppComponent} from '../../../app.component';

declare var jQuery: any;
// services
import {UserService} from '../../../services/user.service';
import {BrowserEvents} from '../../../services/browser-events.service';
// models
import {User} from '../../../models/user/user.model';
import * as global from '../../../global.variables';
import {WorkflowService} from "../../../services/workflow.service";
import {Subscription} from "rxjs/Subscription";
import {CoreService} from "../../../services/core.service";

@Component({
  selector: 'app-menu',
  providers: [BrowserEvents],
  templateUrl: './app.menu.component.html',
  styleUrls: ['./app.menu.component.css']
})
export class AppMenuComponent implements OnInit, AfterViewInit, OnDestroy {

  @Input() reset: boolean;
  private integration_url: string;
  private entry_app_url:string;
  model: any[];

  layoutMenuScroller: HTMLDivElement;

  @ViewChild('layoutMenuScroller') layoutMenuScrollerViewChild: ElementRef;

  public user = new User();


  constructor(public app: AppComponent, public us: UserService, public bs: BrowserEvents, public ws: WorkflowService, private router: Router,
              public cs: CoreService) {
  }

  ngOnInit() {
    // this.bs.sideNavChange.subscribe(data => this.assignIsBrowse(data));
    this.integration_url = global.integration_url;
    this.entry_app_url = global.entry_app_url;
    this.user = this.us.getCurrentUser();
    this.model = [
      {label: 'Dashboard', icon: 'dashboard', routerLink: ['/']},
      {
        label: 'Workflow', icon: 'timeline',
        items: [
          this.ws.inboxMenu ,
          {label: 'Sent Items', icon: 'send', routerLink: ['/workflow/sent'], routerLinkActiveOptions: {exact: true}},
          this.ws.draftMenu,
          {
            label: 'Archive',
            icon: 'archive',
            routerLink: ['/workflow/archive'],
            routerLinkActiveOptions: {exact: true}
          },
          {label: 'Launch', icon: 'launch', routerLink: ['/workflow/launch'], routerLinkActiveOptions: {exact: true}}
        ]
      },
      {
        label: 'Folders', icon: 'folder',
        items: [
          {label: 'Public Folders', icon: 'folder_open', routerLink: ['/browse/browse-folders']},
          // {
          //   label: 'Group Folders',
          //   icon: 'folder_shared',
          //   routerLink: ['/browse/group-folders'],
          //   routerLinkActiveOptions: {exact: true}
          // },
          {
            label: 'Favourite Folders',
            icon: 'folder_special',
            routerLink: ['/browse/favourite-folders'],
            routerLinkActiveOptions: {exact: true}
          },

        ]
      },

      {
        label: 'Shortcuts', icon: 'class',
        items: [
          {label: 'Favourite Docs', icon: 'local_activity', routerLink: ['/favourites']},
          {label: 'Recents', icon: 'restore', routerLink: ['/recents']},
          {label: 'Team Shared Docs', icon: 'group', routerLink: ['/teamshared']},
          {label: 'Search', icon: 'search',  command: (event) => {
              this.openSearch();
            }},
          {
            label: 'Entry Application', icon: 'scanner', command: (event) => {
              this.openEntryApp();
            }
          },

        ]
      }

    ];
    if(this.user && this.user.isAdmin === 'Y'){
      this.model.push(
        {
        label: 'Admin', icon: 'account_circle',
        items: [
          {label: 'Configuration', icon: 'build', routerLink: ['/administration/configurations']},
          {label: 'Access Policies', icon: 'security', routerLink: ['/administration/access-policies']},
          {label: 'Access Policy Mapping', icon: 'beenhere', routerLink: ['/administration/access-policy-mapping']},
          {label: 'Lookup', icon: 'arrow_drop_down_circle', routerLink: ['/administration/lookups']},
          {label: 'Lookup Mapping', icon: 'swap_horiz', routerLink: ['/administration/lookup-mapping']},
          {label: 'News', icon: 'record_voice_over', routerLink: ['/administration/news']},
          {label: 'Role Management', icon: 'supervisor_account', routerLink: ['/administration/role-management']},
          {label: 'Entry Template Mapping', icon: 'input', routerLink: ['/administration/entry-template-mapping']},
          {label: 'Integration', icon: 'vertical_align_center', routerLink: ['/administration/integration']},
          {label: 'ECM Error Logs', icon: 'assignment_late', routerLink: ['/administration/errorlog-management']},
          {label: 'ECM Admin Logs', icon: 'assignment_ind', routerLink: ['/administration/ecm-admin-logs']},
          {label: 'ECM Users', icon: 'people_outline', routerLink: ['/administration/ecm-users']},
          {label: 'ECM Report Users', icon: 'contacts', routerLink: ['/administration/ecm-report-user']},
          {label: 'ECM Administrators', icon: 'account_circle', routerLink: ['/administration/ecm-admin-user']},
           {label: 'ECM Exclude Operator', icon: 'event_busy', routerLink: ['/administration/ecm-exclude-users']},
          {label: 'ECM Global List', icon: 'view_stream', routerLink: ['/administration/ecm-global-list']},
        ]
      });
    }
    if(this.user && this.user.isReport === 'Y') {
     this.model.push(
       {
         label: 'Reports', icon: 'library_books', routerLink: ['/report'] /* items: [
          {label: 'Inbox Workflow', icon: 'work', routerLink: ['/report/inbox-workflow']},
          {label: 'Sent Workflow', icon: 'send', routerLink: ['/report/sent-workflow']},
          /!*{label: 'Inbox WorkItems', icon: 'inbox', routerLink: ['/report/inbox-workItems']},
          {label: 'SentItems', icon: 'send', routerLink: ['/report/sentItems']},*!/
          {label: 'Documents ', icon: 'insert_drive_file', routerLink: ['/report/documents']},
          {label: 'eSign Documents', icon: 'insert_drive_file', routerLink: ['/report/eSign-doc']},
         ]*/
       });
    }
    this.model.push({label: 'Help', icon: 'help_outline'});
  }
  openSearch(){
    if(this.cs.isAdvanced==='Y'){
      this.router.navigate(['/search/advance-search']);
    }else{
      this.router.navigate(['/search/simple-search']);
    }
  }

  openEntryApp() {
    window.open(this.entry_app_url);
  }

  ngAfterViewInit() {
    this.layoutMenuScroller = <HTMLDivElement> this.layoutMenuScrollerViewChild.nativeElement;

    setTimeout(() => {
      jQuery(this.layoutMenuScroller).nanoScroller({flash: true});
    }, 10);


  }


  updateNanoScroll() {
    setTimeout(() => {
      jQuery(this.layoutMenuScroller).nanoScroller();
    }, 500);
  }

  doNavigate() {
    this.router.navigate(['/workflow/launch']);
  }

  ngOnDestroy() {
    jQuery(this.layoutMenuScroller).nanoScroller({flash: true});
  }
}

@Component({
  /* tslint:disable:component-selector */
  selector: '[app-submenu]',
  /* tslint:enable:component-selector */
  template: `
    <ng-template ngFor let-child let-i="index" [ngForOf]="(root ? item : item.items)">
      <li routerLinkActive="active-menuitem"
          [routerLinkActiveOptions]="{exact: true}" [class]="child.badgeStyleClass">
        <a [href]="child.url||'#'" (click)="itemClick($event,child,i)" *ngIf="!child.routerLink"
           [attr.tabindex]="!visible ? '-1' : null" [attr.target]="child.target"
           (mouseenter)="hover=true" (mouseleave)="hover=false" class="ripplelink">
          <i class="material-icons">{{child.icon}}</i>
          <span class="menuitem-text">{{child.label}}</span>
          <i class="material-icons layout-submenu-toggler" *ngIf="child.items">keyboard_arrow_down</i>
          <span class="menuitem-badge" *ngIf="child.badge">{{child.badge}}</span>
        </a>

        <a (click)="itemClick($event,child,i)" *ngIf="child.routerLink"
           [routerLink]="child.routerLink" [attr.tabindex]="!visible ? '-1' : null" [attr.target]="child.target"
           (mouseenter)="hover=true" (mouseleave)="hover=false" class="ripplelink">
          <i class="material-icons">{{child.icon}}</i>
          <span class="menuitem-text">{{child.label}}</span>
          <i class="material-icons layout-submenu-toggler" *ngIf="child.items">>keyboard_arrow_down</i>
          <span class="menuitem-badge r-34" *ngIf="child.badge">{{child.badge}}</span>
        </a>
        <ul app-submenu [item]="child" *ngIf="child.items" [visible]="isActive(i)" [reset]="reset"
            [@children]="isActive(i) ? 'visible' : 'hidden'"></ul>
      </li>
    </ng-template>
  `,
  animations: [
    trigger('children', [
      state('visible', style({
        height: '*'
      })),
      state('hidden', style({
        height: '0px'
      })),
      transition('visible => hidden', animate('400ms cubic-bezier(0.86, 0, 0.07, 1)')),
      transition('hidden => visible', animate('400ms cubic-bezier(0.86, 0, 0.07, 1)'))
    ])
  ]
})
export class AppSubMenuComponent implements OnInit,OnDestroy{

  @Input() item: any;

  @Input() root: boolean;

  @Input() visible: boolean;

  _reset: boolean;

  activeIndex: number;

  hover: boolean;
  private user = new User();
  private subscriptions: Subscription[]=[];

  constructor(public app: AppComponent, public router: Router, public location: Location, public bs: BrowserEvents,
              public us: UserService,private ws:WorkflowService) {
    if (this.us.defaultViewSubMenuExpanded) {
      this.activeIndex = this.us.defaultViewSubMenuExpanded;
      this.us.defaultViewSubMenuExpanded = undefined;
    }
  }

  ngOnInit(){
    this.user = this.us.getCurrentUser();
  }



  getInboxCount(){
    this.ws.updateInboxCount();
    this.ws.updateDraftsCount();

  }

  itemClick(event: Event, item: MenuItem, index: number) {
    this.bs.sideNavChange.emit(item.label);
    if(item.label==='Workflow'){
        this.getInboxCount();
    }
    // avoid processing disabled items
    if (item.disabled) {
      event.preventDefault();
      return true;
    }

    // activate current item and deactivate active sibling if any
    if (item.routerLink || item.items || item.command || item.url) {
      this.activeIndex = (this.activeIndex === index) ? null : index;
    }

    // execute command
    if (item.command) {
      item.command({originalEvent: event, item: item});
    }

    // prevent hash change
    if (item.items || (!item.url && !item.routerLink)) {
      event.preventDefault();
    }

    // hide menu
    if (!item.items) {
      if (this.app.isMobile()) {
        this.app.sidebarActive = false;
        this.app.mobileMenuActive = false;
      }
    }
  }

  isActive(index: number): boolean {
    return this.activeIndex === index;
  }

  @Input() get reset(): boolean {
    return this._reset;
  }

  set reset(val: boolean) {
    this._reset = val;
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
