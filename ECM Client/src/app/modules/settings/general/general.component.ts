import {Component, OnDestroy, OnInit} from '@angular/core';
import {SelectItem} from 'primeng/primeng';
import {Subscription} from 'rxjs/Subscription';
import {WorkflowService} from '../../../services/workflow.service';
import {UserService} from '../../../services/user.service';
import {EntryTemplate} from '../../../models/document/entry-template.model';
import {GeneralSettings} from '../../../models/general/general-settings.model';
import {GrowlService} from '../../../services/growl.service';
import {CoreService} from '../../../services/core.service';
import {BreadcrumbService} from '../../../services/breadcrumb.service';

@Component({
  selector: 'app-general',
  templateUrl: './general.component.html',
  styleUrls: ['./general.component.css']
})
export class GeneralComponent implements OnInit, OnDestroy {
  templates: SelectItem[];
  actions: SelectItem[];
  pageSize: SelectItem[];
  datasettings: any = {};
  private subscriptions: Subscription[] = [];
  private user: any;
  public entryTemplates = [];
  generalSettings: GeneralSettings[];
  defaultNo: any;
  selectedActions: string;
  selectedTemplate: string;
  themes: any[] = [];
  isTheme = false;
  isDefaultView = false;
  defaultViews: any[] = [];

  constructor(private wfs: WorkflowService, public userService: UserService, private growlService: GrowlService,
              private coreService: CoreService, private breadcrumbService: BreadcrumbService) {
    this.templates = [];
    this.actions = [];
    this.pageSize = [];
    this.user = this.userService.getCurrentUser();
  }

  ngOnInit() {
    //this.templates.push({label:"", value:""});
    this.pageSize.push({label: '5', value: '5'});
    this.pageSize.push({label: '10', value: '10'});
    this.pageSize.push({label: '15', value: '15'});
    this.pageSize.push({label: '20', value: '20'});
    this.themes = [
      // {label: 'Pink - Amber',value: 'pink:cityscape'},
      {label: 'Deep Purple - Orange', value: 'deeppurple:storm'},
      {label: 'Blue - Reflection', value: 'blue:reflection'},
      {label: 'BlueGrey - Flatiron', value: 'bluegrey:flatiron'},
      {label: 'BlueGrey - Moody', value: 'bluegrey:moody'},
      {label: 'Cyan - Palm', value: 'cyan:palm'},
      {label: 'Teal - Cloudy', value: 'teal:cloudy'},
      {label: 'Teal - Moody', value: 'teal:moody'},
      {label: 'Teal - Flatiron', value: 'teal:flatiron'},
      {label: 'Orange - CityScape', value: 'orange:cityscape'}
    ];
    this.defaultViews = [{label: 'Dashboard', value: 'dashboard'}, {label: 'Workflow', value: 'workflow'},
      {label: 'Folders', value: 'folders'}];
    const subscription = this.wfs.getEntryTemplates()
      .subscribe(data => this.assignTemplates(data));
    this.addToSubscriptions(subscription);
    const subscription1 = this.wfs.getActions(this.user.EmpNo)
      .subscribe(data => this.assignActions(data));
    this.addToSubscriptions(subscription1);
  }

  assignActions(data) {
    this.actions.push({label:"", value:""});
    for (let i = 0; i < data.length; i++) {
      if (data[i].name !== 'Signature') {
        this.actions.push({label: data[i].name, value: data[i].name});
      }
    }
  }

  assignTemplates(data) {
    this.entryTemplates = data;
    this.templates.push({label:"", value:""});
    for (let i = 0; i < data.length; i++) {
      this.templates.push({label: data[i].symName, value: data[i].symName});
    }
    const subscription = this.userService.getUserSettings().subscribe(val => this.assignGeneralSettings(val));
    this.addToSubscriptions(subscription);
  }

  updateGeneralSetting() {
    for (const setting of this.generalSettings) {
      if (setting.key === 'Default Action') {
        setting.val = this.selectedActions;
      }
      else if (setting.key === 'Page Size') {
        setting.val = this.defaultNo;
      }
      else if (setting.key === 'Default Template') {
        setting.val = this.selectedTemplate;
      }
      else if (setting.key === 'Default Theme') {
        this.isTheme = true;
        setting.val = this.userService.selectedTheme;
      }
      else if (setting.key === 'Default View') {
        this.isDefaultView = true;
        setting.val = this.userService.defaultView;
      }
    }
    // this.generalSettings.map((d,i)=>{
    //   if(d.key)
    // });
    if (this.isTheme === false) {
      this.generalSettings.push({
        'id': null,
        'appId': 'ECM',
        'empNo': this.user.EmpNo,
        'key': 'Default Theme',
        'val': this.userService.selectedTheme
      });
    }
    if (this.isDefaultView === false) {
      this.generalSettings.push({
        'id': null,
        'appId': 'ECM',
        'empNo': this.user.EmpNo,
        'key': 'Default View',
        'val': this.userService.defaultView
      });
    }

    const subscription = this.userService.updateUserSettings(this.generalSettings).subscribe(val => this.updateSettings(val), err => this.updateFailed(err));
    this.coreService.progress = {busy: subscription, message: 'Updating...'};
    this.addToSubscriptions(subscription);
  }

  updateFailed(err) {
    this.growlService.showGrowl({
      severity: 'error',
      summary: 'Failure', detail: 'Failed To Update'
    });
  }

  updateSettings(val) {
    this.growlService.showGrowl({
      severity: 'info',
      summary: 'Success', detail: 'Updated Successfully'
    });
    localStorage.removeItem('defaultView');
    localStorage.setItem('defaultView',this.userService.defaultView);
    this.userService.pageSize = this.defaultNo;
  }

  assignGeneralSettings(val) {
    this.generalSettings = val;
    for (const setting of val) {
      if (setting.key === 'Default Action') {
        this.selectedActions = setting.val;
      }
      if (setting.key === 'Page Size') {
        this.defaultNo = setting.val;
      }
      if (setting.key === 'Default Template') {
        this.selectedTemplate = setting.val;
      }
      if (setting.key === 'Default Theme') {
        this.userService.selectedTheme = setting.val;
      }
      if (setting.key === 'Default View') {
        this.userService.defaultView = setting.val;
      }
    }
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
    this.generalSettings = [];
    this.entryTemplates = [];
    this.pageSize = [];
    this.actions = [];
    this.templates = [];
  }

}
