import {Injectable} from '@angular/core';
import {Subject} from 'rxjs/Subject';
import {Observable} from 'rxjs/Observable';
import {MenuItem} from 'primeng/primeng';

@Injectable()
export class BreadcrumbService {

  private itemsSource = new Subject<MenuItem[]>();

  public dashboardFilterQuery: any;
  public dashboardTabSelected: any;
  public fromDashboard: boolean;

  itemsHandler = this.itemsSource.asObservable();

  setItems(items: MenuItem[]) {
    this.itemsSource.next(items);
  }

}
