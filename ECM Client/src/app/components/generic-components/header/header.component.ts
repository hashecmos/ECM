import {Component, OnDestroy, OnInit, ViewChild, ElementRef} from '@angular/core';
import {Observable} from 'rxjs/Observable';
import {AppComponent} from '../../../app.component';
import {Router} from '@angular/router';
// services
import {UserService} from '../../../services/user.service';
// models
import {User} from '../../../models/user/user.model';
import {DocumentService} from '../../../services/document.service';
import {Subscription} from 'rxjs/Subscription';
import * as global from '../../../global.variables';
import {GrowlService} from '../../../services/growl.service';
import {CoreService} from "../../../services/core.service";
import {BrowserEvents} from "../../../services/browser-events.service";

@Component({
  selector: 'app-header',
  templateUrl: './header.component.html',
  styleUrls: ['./header.component.css']
})
export class HeaderComponent implements OnInit, OnDestroy {
  public isAdvanced = false;
  public user = new User();
  private subscriptions: Subscription[] = [];
  username: any;
  searchObj: any = {oper: 'EXACT'};
  showCart = false;
  @ViewChild('searchQuery') public searchQuery: ElementRef;

  constructor(public app: AppComponent, public router: Router, public us: UserService,
              private documentService: DocumentService, private growlService: GrowlService, private coreService: CoreService, private bs: BrowserEvents) {
  }

  ngOnInit() {
    this.username = global.username;
    this.us.logIn(this.username, 'def').subscribe(data => {
      localStorage.setItem('user', JSON.stringify(data));
      this.user = this.us.getCurrentUser();
      this.getDocumentCart();
    }, Error => {
      localStorage.removeItem('user');
      this.router.navigate(['/auth/auth-failure']);
    });

    this.bs.switchBackContentSearch.subscribe(data => this.switchBackToContentSearch(data));
  }

  switchBackToContentSearch(data) {
    this.isAdvanced = false;

  }


  showCartItems($event, messages) {
    if ($event && messages) {
      this.app.onTopbarRootItemClick($event, messages);
    }
    if (this.app.activeTopbarItem === messages) {
      this.getDocumentCart();
    }

  }

  getDocumentCart() {
    const subscription = this.documentService.getCart(this.user.EmpNo).subscribe((data) => {
      this.documentService.refreshCart(data);

    }, (err) => {
      console.log('Error ' + JSON.stringify(err));
    });
    this.coreService.progress = {busy: subscription, message: '', backdrop: true};
    this.addToSubscriptions(subscription);
  }


  switchSearch(event: Event) {
    this.isAdvanced = !this.isAdvanced;
    if (this.isAdvanced) {
      this.router.navigate(['/search/advance-search']);
      this.searchObj.query = undefined;
      this.coreService.isAdvanced='Y'
    } else {
      this.router.navigate(['/search/simple-search']);
      this.searchObj.query = undefined;
      this.coreService.isAdvanced='N'
    }
  }

  setSearchOper(oper) {
    this.searchObj.oper = oper;
    this.searchQuery.nativeElement.focus();
  }

  headerScreen(screen) {
    if (screen === 'launch') {
      this.router.navigate(['workflow/launch']);
    } else if (screen === 'add-doc') {
      this.router.navigateByUrl('browse/add-doc');
    } else if (screen === 'advanceSearch') {
      this.router.navigateByUrl('/search');
    }
  }

  search(event) {
    if (!this.isAdvanced && (!this.searchObj.query || this.searchObj.query.length < 3)) {
      this.growlService.showGrowl({
        severity: 'error',
        summary: 'Search', detail: 'Please enter minimum 3 characters'
      });
      return;
    }
    if (this.isAdvanced) {
      this.router.navigate(['/search/advance-search']);
    } else {
      this.router.navigate(['/search/simple-search', {
        query: this.searchObj.query,
        oper: this.searchObj.oper
      }]);

    }
    this.searchObj.query = undefined;
  }


  navToSettings() {
    this.router.navigate(['/settings']);
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
