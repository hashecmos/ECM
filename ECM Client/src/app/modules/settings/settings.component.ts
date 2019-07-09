import {Component} from '@angular/core';
import {BreadcrumbService} from "../../services/breadcrumb.service";
import {BrowserEvents} from "../../services/browser-events.service";

@Component({
  selector: 'settings-component',
  templateUrl: './settings.component.html',
  styleUrls: ['./settings.component.css']
})
export class SettingsComponent {
  public activeTab = 0;
  constructor(private breadcrumbService: BreadcrumbService,private bs:BrowserEvents) {
    this.breadcrumbService.setItems([
        {label: 'Settings'}
      ]);
    this.bs.switchBackContentSearch.emit();
  }
  tabChange(event) {
    this.activeTab = event.index;
  }
}
