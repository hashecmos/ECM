import {Component, OnInit, AfterViewInit, EventEmitter} from '@angular/core';
import {Router, ActivatedRoute} from '@angular/router';
// service
import {BreadcrumbService} from '../../../services/breadcrumb.service';
import {Subscription} from 'rxjs/Rx';
import {GrowlService} from '../../../services/growl.service';

@Component({
  templateUrl: './add-document.component.html',
  selector: 'add-document',
  styleUrls: ['./add-document.component.css'],
})
export class AddDocumentComponent implements OnInit {
  constructor(private breadcrumbService: BreadcrumbService, private router: Router) {
    this.breadcrumbService.setItems([
      {label: 'Browse', routerLink: ['/browse']},
      {label: 'Add-Document', routerLink: ['/browse/add-doc']}
    ]);
  }

  ngOnInit() {
  }

  ngAfterContentInit() {
  }

  addDocSuccess() {
    this.router.navigateByUrl(`/recents`);
  }
}
