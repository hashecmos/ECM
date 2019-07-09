import {Component, Renderer} from '@angular/core';
import {TreeNode} from 'primeng/primeng';
import {AppComponent} from '../../app.component';
import {BrowserEvents} from '../../services/browser-events.service';
import {BreadcrumbService} from "../../services/breadcrumb.service";

@Component({
  selector: 'browse-component',
  templateUrl: './browse.component.html',
})
export class BrowseComponent {
 constructor(private bs:BrowserEvents) {
  this.bs.switchBackContentSearch.emit();
  }
}
