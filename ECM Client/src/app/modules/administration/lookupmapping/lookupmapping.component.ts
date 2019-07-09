import {Component, OnDestroy, OnInit} from '@angular/core';
import {AdminService} from '../../../services/admin.service';
import {BreadcrumbService} from '../../../services/breadcrumb.service';
import {Subscription} from 'rxjs/Rx';
import {ContentService} from '../../../services/content-service.service';
import {GrowlService} from '../../../services/growl.service';
import {ConfirmationService, SelectItem} from 'primeng/primeng';
import {CoreService} from "../../../services/core.service";
import {UserService} from "../../../services/user.service";

@Component({
  selector: 'app-lookupmapping',
  templateUrl: './lookupmapping.component.html',
  styleUrls: ['./lookupmapping.component.css']
})
export class LookupmappingComponent
  implements OnInit, OnDestroy {
  private subscription: Subscription[] = [];
  public lookupMappingList: any[];
  public colHeaders: any[] = [];
  public showNewLookupMapping = false;
  suggestionsResults: any[];
  public lookupMapping = {
    orgUnit: {label: '', value: ''},
    entryTemp: {id: '', vsId: ''},
    prop: '',
    lookup: ''
  };
  public entryTemplates: any[] = [];
  public properties: any[] = [];
  public lookups: any[] = [];
  public editMode = false;
  public orgSelected = false;
  public itemsPerPage: any = 10;
  public busyModal: Subscription;
  public suggestionsResultsOrg: any[] = [];
  public selectedOrgCode: any;
  public selectedTemplate: any;
  etList: any[] = [];
  orgName: any;

  constructor(private adminService: AdminService,
              private cs: ContentService, private us: UserService,
              private growlService: GrowlService, private breadcrumbService: BreadcrumbService,
              private confirmationService: ConfirmationService, private coreService: CoreService) {
  }

  refresh() {
    if (this.selectedOrgCode && this.selectedTemplate) {
      this.searchLookupMapping();
    }
    else {
      this.getAllLookupMapping();
      //   this.growlService.showGrowl({
      //   severity: 'error',
      //   summary: 'Select Required',
      //   detail: 'Select Org Unit and Entry template'
      // });
    }

  }

  ngOnInit() {
    this.us.getUserSettings().subscribe(val => {
      const res: any = val;
      this.assignPagination(res);
    });
    this.colHeaders = [
      {field: 'id', header: 'Id', hidden: true},
      {field: 'orgUId', header: 'OrgUId', hidden: true},
      {field: 'lkUp', header: 'LkUp', hidden: true},
      {field: 'tmpId', header: 'TmpId', hidden: true},
      {field: 'prop', header: 'Property', hidden: false},
      {field: 'orgUName', header: 'Organization Unit', hidden: false},
      {field: 'tmpName', header: 'Entry Template', hidden: false}
    ];
    this.getAllLookupMapping();
    this.breadcrumbService.setItems([
      {label: 'Admin'},
      {label: 'Lookup Mapping'}
    ]);

    const subscription = this.adminService.getLookups().subscribe(lookups => {
      lookups.map((lookup, index) => {
        this.lookups.push({label: lookup.name, value: lookup.id});
      });
    });
    this.coreService.progress = {busy: subscription, message: '', backdrop: true};
    this.addToSubscriptions(subscription);
  }

  assignPagination(val) {
    if (val !== undefined) {
      val.map((d, i) => {
        if (d.key === 'Page Size') {
          if (d.val) {
            this.itemsPerPage = parseInt(d.val, 10);
          } else {
            this.itemsPerPage = 10;
          }
        }
      });
    }
  }

  getAllLookupMapping() {
    const subscription = this.adminService.getLookupMappings().subscribe(lookupMap => {
      this.lookupMappingList = lookupMap;
    });
    this.coreService.progress = {busy: subscription, message: '', backdrop: true};
    this.addToSubscriptions(subscription);
  }

  newLookupMapping() {
    this.showNewLookupMapping = true;
  }

  search(event) {
    this.busyModal = this.adminService.searchOrgUnits(event.query).subscribe(data => {
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

  confirmClear() {
    this.orgName = undefined;
    this.etList = [];
    this.lookupMappingList = [];
  }

  orgUnitSelected(selected) {
    this.orgSelected = true;
    this.busyModal = this.cs.getEntryTemplatesByOrgId(selected.value).subscribe(template => {
      this.entryTemplates = [];
      template.map((temp, i) => {
        this.entryTemplates.push({label: temp.symName, value: {id: temp.id, vsId: temp.vsid}});
      });
    });
    this.addToSubscriptions(this.busyModal);
  }

  orgUnitSelectedForSearch(e) {
    this.selectedOrgCode = e.id;
    this.busyModal = this.cs.getEntryTemplatesByOrgId(e.id).subscribe(template => this.assignTemplates(template));

  }

  assignTemplates(data) {
    this.etList = [];
    data.map((temp, i) => {
      this.etList.push({label: temp.symName, value: temp.vsid});
    });
    this.addToSubscriptions(this.busyModal);
  }

  searchLookupMapping() {
    if (this.selectedOrgCode && this.selectedTemplate) {
      const subscription = this.adminService.getLookupMappingsByOrg(this.selectedOrgCode, this.selectedTemplate).subscribe(data => this.assignMappings(data));
      this.coreService.progress = {busy: subscription, message: '', backdrop: true};
      this.addToSubscriptions(subscription);
    }
    else {
      this.getAllLookupMapping();
    }

  }

  assignMappings(data) {
    this.lookupMappingList = data;
  }

  changeTemplateSelection(event) {
    this.assignTempProp(event.value.id);
  }

  assignTempProp(temId) {
    this.properties = [];
    this.busyModal = this.cs.getEntryTemplate(temId).subscribe(template => {
      template.props.map((prop, index) => {
        if (prop.dtype !== 'DATE') {
          this.properties.push({label: prop.symName, value: prop.symName});
        }
      });
    });
    this.addToSubscriptions(this.busyModal);
  }

  addNewLookupMapping() {
    const subscription = this.adminService
      .addLookupMapping(
        this.lookupMapping.orgUnit.value,
        this.lookupMapping.entryTemp.vsId,
        this.lookupMapping.prop,
        this.lookupMapping.lookup
      )
      .subscribe(data => this.success(data), Error => this.fail(Error));
    this.coreService.progress = {busy: subscription, message: '', backdrop: true};
    this.addToSubscriptions(subscription);
  }

  success(data) {
    this.showNewLookupMapping = false;
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
    this.editMode = false;
    this.searchLookupMapping();
  }

  fail(data) {
    this.growlService.showGrowl({
      severity: 'error',
      summary: 'Failure',
      detail: 'Failed'
    });
    this.editMode = false;
  }

  modifyLookupMapping(row) {
    this.editMode = true;
    this.properties = [];
    this.properties.push({label: row.prop, value: row.prop});
    this.lookupMapping = {
      orgUnit: {label: row.orgUName, value: row.orgUId},
      entryTemp: {id: row.tmpName, vsId: row.tmpId},
      prop: row.prop,
      lookup: row.lkUp
    };
    this.showNewLookupMapping = true;
  }

  deleteLookupMapping(row) {
    const subscription = this.adminService
      .removeLookupMapping(row.orgUId, row.tmpId, row.prop)
      .subscribe(
        res => {
          this.growlService.showGrowl({
            severity: 'info',
            summary: 'Success',
            detail: 'Deleted Successfully'
          });
          if (this.selectedOrgCode && this.selectedTemplate) {
            const subscription = this.adminService.getLookupMappingsByOrg(this.selectedOrgCode, this.selectedTemplate).subscribe(data => this.assignMappings(data));
            this.coreService.progress = {busy: subscription, message: '', backdrop: true};
            this.addToSubscriptions(subscription);
          }
          else {
            this.getAllLookupMapping();
          }

        },
        Error => {
          this.growlService.showGrowl({
            severity: 'error',
            summary: 'Failure',
            detail: 'Failed'
          });
        }
      );
    this.coreService.progress = {busy: subscription, message: '', backdrop: true};
    this.addToSubscriptions(subscription);
  }

  confirmdeleteLookupMapping(row) {
    this.confirmationService.confirm({
      message: 'Are you sure that you want to Delete?',
      header: 'Delete Confirmation',
      icon: 'ui-icon-help',
      accept: () => {
        this.deleteLookupMapping(row);
      },
      reject: () => {
      }
    });
  }

  closeModel() {
    this.showNewLookupMapping = false;
    this.editMode = false;
    this.entryTemplates = [];
    this.orgSelected = false;
    this.lookupMapping = {
      orgUnit: {label: '', value: ''},
      entryTemp: {id: '', vsId: ''},
      prop: '',
      lookup: ''
    };
  }

  addToSubscriptions(subscription) {
    this.subscription.push(subscription);
  }

  exportToExcel() {
    let array = [];
    this.colHeaders.map(d => {
      if (d.hidden === false) {
        array.push(d.field);
      }
    });
    this.coreService.exportToExcel(this.lookupMappingList, 'Lookup_Mapping.xlsx', array)
  }

  searchOrg(event) {
    const subscription = this.adminService.searchOrgUnits(event.query).subscribe(data => {
      this.suggestionsResultsOrg = data;
    });
    this.coreService.progress = {busy: subscription, message: '', backdrop: true};
    this.addToSubscriptions(subscription);
  }

  ngOnDestroy() {
    for (const subs of this.subscription) {
      subs.unsubscribe();
    }
    this.subscription = [];
    this.lookupMappingList = [];
    this.colHeaders = [];
    this.suggestionsResults = [];
    this.lookupMapping = {
      orgUnit: {label: '', value: ''},
      entryTemp: {id: '', vsId: ''},
      prop: '',
      lookup: ''
    };
    this.entryTemplates = [];
    this.properties = [];
    this.lookups = [];
  }
}
