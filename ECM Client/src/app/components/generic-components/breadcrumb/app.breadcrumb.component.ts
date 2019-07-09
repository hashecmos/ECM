import {Component, OnDestroy} from '@angular/core';
import {Subscription} from 'rxjs/Subscription';
import {MenuItem} from 'primeng/primeng';
import {Observable} from "rxjs/Observable";
import {BreadcrumbService} from "../../../services/breadcrumb.service";

@Component({
  selector: 'app-breadcrumb',
  templateUrl: './app.breadcrumb.component.html'
})
export class AppBreadcrumbComponent implements OnDestroy {

  subscription: Subscription;

  items: MenuItem[];

  public now = Observable.interval(1000).map(x => new Date()).share();

  constructor(private breadcrumbService: BreadcrumbService) {
    this.subscription = breadcrumbService.itemsHandler.subscribe(response => {
      this.items = response;
    });
  }

  ngOnDestroy() {
    if (this.subscription) {
      this.subscription.unsubscribe();
    }
  }
}
