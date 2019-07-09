import {Component, OnDestroy, OnInit, ViewChild} from '@angular/core';
import {AdminService} from '../../../services/admin.service';
import {ConfirmationService} from 'primeng/primeng';
import {GrowlService} from '../../../services/growl.service';
import {Subscription} from 'rxjs/Rx';
import {ContentService} from '../../../services/content-service.service';
import {BreadcrumbService} from '../../../services/breadcrumb.service';
import {CoreService} from '../../../services/core.service';
import {User} from "../../../models/user/user.model";
import {UserService} from "../../../services/user.service";


@Component({
  selector: 'app-integration',
  templateUrl: './integration.component.html',
  styleUrls: ['./integration.component.css']
})
export class IntegrationComponent implements OnInit, OnDestroy {
  private subscription: Subscription[] = [];
  public integrations: any[] = [];
  public colHeaders: any[] = [];
  public showIntegration = false;
  public editMode = false;
  suggestionsResults: any[];
  public integration = {
    id: 0,
    appId: '',
    orgUnit: {label: '', value: ''},
    entryTemp: {id: '', symName: ''},
    param1: '',
    param2: '',
    param3: '',
    param4: '',
    param5: '',
    type: '',
    description: '',
    createdBy: '',
    createdDate: '',
    coordinator: ''
  };
  public integrationParams = {
    appId: '',
    param1: '',
    param2: '',
    param3: '',
    param4: '',
    param5: '',
    type: '',
    createdDate: '',
    createdBy: ''
  };
  public entryTemplates: any[] = [];
  public params: any[] = [];
  public types: any[] = [];
  public orgSelected = false;
  public busyModal: Subscription;
  public itemsPerPage: any = 14;
  public user = new User();
  public viewIntigrationParams = false;
  public temp:any;

  constructor(private as: AdminService, private confirmationService: ConfirmationService, private coreService: CoreService, private growlService: GrowlService,
              private cs: ContentService, private breadcrumbService: BreadcrumbService, private us: UserService) {
    this.user = us.getCurrentUser();
  }

  refresh() {
    this.getIntegrations();
  }

  ngOnInit() {
    this.us.getUserSettings().subscribe(val => {
      const res: any = val;
      this.assignPagination(res);
    });
    this.colHeaders = [
      {field: 'id', header: 'Id', hidden: true}, {field: 'appId', header: 'Integration Name', hidden: false},
      {field: 'description', header: 'Description', hidden: false}, {
        field: 'coordinator',
        header: 'Integration Coordinator',
        hidden: false
      },
      // {field: 'empName', header: 'Created By', hidden: false}, {
      //   field: 'createdDate',
      //   header: 'Created Date',
      //   hidden: false
      // },
      {field: 'modifiedBy', header: 'Modified By', hidden: false}, {
        field: 'modifiedDate',
        header: 'Modified Date',
        hidden: false
      },
      {field: 'className', header: 'Class Name', hidden: true}, {field: 'template', header: 'Template', hidden: true},
      /*{field: 'param1', header: 'Param 1', hidden: false}, {field: 'param2', header: 'Param 2', hidden: false},
      {field: 'param3', header: 'Param 3', hidden: false}, {field: 'param4', header: 'Param 4', hidden: false},
      {field: 'param5', header: 'Param 5', hidden: false}, {field: 'type', header: 'Type', hidden: false}*/
    ];
    this.breadcrumbService.setItems([
      {label: 'Admin'},
      {label: 'Integration'}
    ]);
    this.getIntegrations();
    this.types = [{label: 'Single', value: 'SINGLE'}, {label: 'Multiple', value: 'MULTIPLE'}];
  }

  assignPagination(val) {
    if (val !== undefined) {
      val.map((d, i) => {
        if (d.key === 'Page Size') {
          if (d.val) {
            this.itemsPerPage = parseInt(d.val, 10);
          } else {
            this.itemsPerPage = 14;
          }
        }
      });
    }
  }

  getIntegrations() {
    const subscription = this.as.getIntegrations().subscribe(res => {
      this.integrations = res;
    });
    this.coreService.progress = {busy: subscription, message: '', backdrop: true};
    this.addToSubscriptions(subscription);
  }

  search(event) {
    this.busyModal = this.as.searchOrgUnits(event.query).subscribe(data => {
      this.suggestionsResults = [];
      for (const orgunit of data) {
        this.suggestionsResults.push({
          label: orgunit.desc,
          value: orgunit.id
        });
      }
    });
    this.addToSubscriptions(this.busyModal);
  }

  orgUnitSelected(selected) {
    this.orgSelected = true;
    this.busyModal = this.cs.getEntryTemplatesByOrgId(selected.value).subscribe(template => {
      this.entryTemplates = [];
      template.map((temp, i) => {
        this.entryTemplates.push({label: temp.symName, value: {'id': temp.id, 'symName': temp.symName}});
      });
    });
    this.addToSubscriptions(this.busyModal);
  }

  changeTemplateSelection(event) {
    this.assignTempProp(this.integration.entryTemp.id, null);
  }

  assignTempProp(temId, rowData) {
    this.params = [];
    this.params.push({label: '', value: ''});
    this.params.push({label:'Id', value:'Id'});
    this.params.push({label:'Date Created', value:'Date Created'});
    this.busyModal = this.cs.getEntryTemplate(temId).subscribe(template => {
      this.temp=template;
      if (!this.editMode) {
        this.integration.entryTemp.symName = template.symName;
      }
      template.props.map((prop, index) => {
        this.params.push({label: prop.name, value: prop.symName});
      });
      if (this.editMode && rowData) {
        this.integration = {
          id: rowData.id,
          appId: rowData.appId,
          orgUnit: {label: '', value: ''},
          entryTemp: {id: rowData.template, symName: rowData.className},
          param1: rowData.param1,
          param2: rowData.param2,
          param3: rowData.param3,
          param4: rowData.param4,
          param5: rowData.param5,
          type: rowData.type,
          description: rowData.description,
          createdBy: rowData.empName,
          createdDate: rowData.createdDate,
          coordinator: rowData.coordinator
        };
      }
    });
    this.addToSubscriptions(this.busyModal);
  }

  newIntegration() {
    const newIntegration = {
      'id': this.integration.id,
      'appId': this.integration.appId,
      'className': this.integration.entryTemp.symName,
      'template': this.integration.entryTemp.id,
      'param1': this.integration.param1,
      'param2': this.integration.param2,
      'param3': this.integration.param3,
      'param4': this.integration.param4,
      'param5': this.integration.param5,
      'type': this.integration.type,
      'description': this.integration.description,
      'empName': this.user.fulName,
      'coordinator': this.integration.coordinator
    };
    const subscription = this.as.saveIntegrations(newIntegration).subscribe(data => {
        if (this.editMode) {
          this.growlService.showGrowl({
            severity: 'info',
            summary: 'Success',
            detail: 'Saved Successfully'
          });
        } else {
          this.growlService.showGrowl({
            severity: 'info',
            summary: 'Success',
            detail: 'Added Successfully'
          });
        }
        this.closeModel();
        this.getIntegrations();
      }, Error => this.fail()
    );
    this.coreService.progress = {busy: subscription, message: '', backdrop: true};
    this.addToSubscriptions(subscription);
  }

  fail() {
    this.growlService.showGrowl({
      severity: 'error',
      summary: 'Failure',
      detail: 'Failed'
    });
  }

  modifyIntegration(row) {
    this.editMode = true;
    this.assignTempProp(row.template, row);
    this.showIntegration = true;
  }

  confirmDeleteIntegration(row) {
    this.confirmationService.confirm({
      message: 'Are you sure that you want to Delete ' + row.appId + '?',
      header: 'Delete Confirmation',
      icon: 'ui-icon-help',
      accept: () => {
        this.deleteIntegration(row);
      },
      reject: () => {
      }
    });
  }

  deleteIntegration(row) {
    const subscription = this.as.deleteIntegrations(row.id).subscribe(res => {
      this.growlService.showGrowl({
        severity: 'info',
        summary: 'Success',
        detail: 'Deleted Successfully'
      });
      this.getIntegrations();
    }, error => {
      this.growlService.showGrowl({
        severity: 'error',
        summary: 'Failure',
        detail: 'Failed'
      });
    });
    this.coreService.progress = {busy: subscription, message: '', backdrop: true};
    this.addToSubscriptions(subscription);
  }

  closeModel() {
    this.showIntegration = false;
    this.editMode = false;
    this.entryTemplates = [];
    this.orgSelected = false;
    this.integration = {
      id: 0,
      appId: '',
      orgUnit: {label: '', value: ''},
      entryTemp: {id: '', symName: ''},
      param1: '',
      param2: '',
      param3: '',
      param4: '',
      param5: '',
      type: '',
      description: '',
      createdBy: '',
      createdDate: '',
      coordinator: ''
    };
  }

  viewIntegration(rowData) {
    this.busyModal = this.cs.getEntryTemplate(rowData.template).subscribe(template => {
      this.temp = template;
    });
    this.integrationParams = {
      appId: rowData.appId,
      param1: rowData.param1,
      param2: rowData.param2,
      param3: rowData.param3,
      param4: rowData.param4,
      param5: rowData.param5,
      type: rowData.type,
      createdDate: rowData.createdDate,
      createdBy: rowData.empName
    };
    this.viewIntigrationParams = true;
  }

  addToSubscriptions(subscription) {
    this.subscription.push(subscription);
  }

  exportToExcel() {
    let array = [];
    this.colHeaders.map(d => {
      array.push(d.field);
    });
    this.coreService.exportToExcel(this.integrations, 'Integration.xlsx', array)
  }

  ngOnDestroy() {
    for (const subs of this.subscription) {
      subs.unsubscribe();
    }
    this.subscription = [];
    this.integrations = [];
    this.colHeaders = [];
    this.showIntegration = false;
    this.editMode = false;
    this.entryTemplates = [];
    this.orgSelected = false;
  }
}
