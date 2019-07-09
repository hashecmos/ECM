import {Component} from '@angular/core';
import {BreadcrumbService} from "../../services/breadcrumb.service";
import {BrowserEvents} from "../../services/browser-events.service";


@Component({
  templateUrl: './workflow.component.html',
})
export class WorkflowComponent {
    constructor(private bs:BrowserEvents) {
    this.bs.switchBackContentSearch.emit();
  }
}
