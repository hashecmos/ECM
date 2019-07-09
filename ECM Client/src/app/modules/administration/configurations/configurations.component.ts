import {BreadcrumbService} from '../../../services/breadcrumb.service';
import {AfterViewInit, Component, OnDestroy, OnInit} from '@angular/core';
import {Subscription} from 'rxjs/Rx';
import {ConfigurationService} from '../../../services/configuration.service';
import {GrowlService} from '../../../services/growl.service';
import {CoreService} from '../../../services/core.service';
import {UserService} from "../../../services/user.service";
import {User} from "../../../models/user/user.model";
import {ContentService} from "../../../services/content-service.service";
import {selector} from "rxjs/operator/publish";

@Component({
  selector: 'app-configurations',
  templateUrl: './configurations.component.html',
  styleUrls: ['./configurations.component.css']
})
export class ConfigurationsComponent implements OnInit, OnDestroy {
  private subscriptions: Subscription[] = [];
  private configurationList: any[] = [];
  public updatedRow: any = {};
  public showEditKeyValue = false;
  public itemsPerPage: any = 10;
  public colHeaders: any[] = [];
  public user = new User();
  public entryTemplates:any[] = [];
  public logLevelOptions: any[] = [];
  constructor(private configService: ConfigurationService,private coreService:CoreService, private growlService: GrowlService,
              private breadcrumbService: BreadcrumbService, private us:UserService, private cs: ContentService) {
    this.colHeaders = [{field: 'keyName', header: 'Key Name', hidden: false},{field: 'value', header: 'Value', hidden: false},
                        {field: 'appId', header: 'appId', hidden: true},{field: 'configScope', header: 'configScope', hidden: true},
                        {field: 'keyDesc', header: 'Description', hidden: false},{field: 'createdBy', header: 'Created By', hidden: true},
                        {field: 'modifiedBy', header: 'Modified By', hidden: false},{field: 'modifiedDate', header: 'Modified Date', hidden: false}
                        ];
    this.user = this.us.getCurrentUser();
    this.logLevelOptions = [{label: 'Debug', value: 1},{label: 'Info', value: 2},{label: 'Warning', value: 3},
                            {label: 'Error', value: 4},{label: 'Fatal', value: 5}];
  }
   refreshConfig(flag){
    if(flag==='SYSTEM'){
      this.getConfigurations('SYSTEM');
    }
    else if(flag==='LOG'){
      this.getConfigurations('LOG');
    }
     else if(flag==='APP'){
      this.getConfigurations('APP');
    }

  }

  ngOnInit() {
    this.us.getUserSettings().subscribe(val => {
      const res:any = val;
      this.assignPagination(res);
    });
    this.getConfigurations('SYSTEM');
    this.getEntryTemplates();
    this.breadcrumbService.setItems([
      {label: 'Admin'},
      {label: 'Configurations'}
    ]);
  }
  assignPagination(val) {
    if (val !== undefined) {
      val.map((d, i) => {
        if (d.key === 'Page Size') {
          if(d.val){
            this.itemsPerPage = parseInt(d.val,10);
          }else{
            this.itemsPerPage = 10;
          }

        }
      });

    }
  }
  getConfigurations(scope) {
   const subscription=  this.configService.getAllConfigurations(scope).subscribe(config => {
      this.configurationList[scope] = config;
    });
    this.coreService.progress = {busy: subscription, message: '', backdrop: true};
    this.addToSubscriptions(subscription);
  }

  getEntryTemplates(){
    const subscription = this.cs.getAllEntryTemplates().subscribe(template => {
      this.entryTemplates = [];
      template.map((temp, i) => {
        this.entryTemplates.push({label: temp.symName, value: temp.symName});
      });
    });
  }

  prepareEdit(row) {
    this.updatedRow = {
      'id': row.id,
      'name': row.keyName,
      'value': row.value,
      'appId': row.appId,
      'scope': row.configScope,
      'empName':this.user.fulName,
      'desc':row.keyDesc
    };
  }

  saveValue() {
    this.subscriptions.push(this.configService.updateConfigurationRow([this.updatedRow]).subscribe(res => {
      this.growlService.showGrowl({
        severity: 'info',
        summary: 'Success', detail: 'Saved Successfully'
      });
      this.showEditKeyValue = false;
      this.getConfigurations(this.updatedRow.scope);
    }, Error => {
      this.growlService.showGrowl({
        severity: 'error',
        summary: 'Failure', detail: 'Failed'
      });
    }));
  }

  onTabOpen(event){
    if(event.index === 1){
      this.getConfigurations('APP');
    } else if(event.index === 2){
      this.getConfigurations('LOG');
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
     this.subscriptions = [];
    this.configurationList = [];
    this.updatedRow = {};
    this.showEditKeyValue = false;
  }
}
