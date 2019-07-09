import {Lookup} from '../../../models/admin/lookup.model';
import {LookupValue} from '../../../models/admin/lookupvalue.model';
import {AdminService} from '../../../services/admin.service';
import {BreadcrumbService} from '../../../services/breadcrumb.service';
import {Component, OnDestroy, OnInit} from '@angular/core';
import {Subscription} from 'rxjs';
import {GrowlService} from '../../../services/growl.service';
import {CoreService} from '../../../services/core.service';
import {ConfirmationService} from 'primeng/primeng';
import * as global from '../../../global.variables';
import {UserService} from '../../../services/user.service';

@Component({
  selector: 'app-lookups',
  templateUrl: './lookups.component.html',
  styleUrls: ['./lookups.component.css']
})
export class LookupsComponent implements OnInit, OnDestroy {
  public lookupList = [];
  lookupValues: any[];
  headerTitleLookup: any;
  selectedLookup: any;
  selectedindex: any;
  dat = new LookupValue();
  lookup = new Lookup();
  newLookUp = new LookupValue();
  indexValue = undefined;
  headerTitle: any;
  showEdit = false;
  busy: Subscription;
  query: any;
  queryValue: any;
  emptyMessage: any;
  private subscriptions: any[] = [];
  private roleData: any = {roles: {model: {}}};
  roleTreeExpandedIcon = 'ui-icon-people-outline';
  roleTreeCollapsedIcon = 'ui-icon-people';
  private tmpRoleTree: any[];
  private currentUser: any;
  public suggestionsResults: any[] = [];
  public selectedOrgUnit: any;
  orgName:any;

  constructor(private as: AdminService, private userService: UserService, private confirmationService: ConfirmationService, private coreService: CoreService, private growlService: GrowlService, private breadcrumbService: BreadcrumbService) {
    this.lookupValues = [];
  }

  refreshtable() {
    this.refreshLookupTable();
  }

  ngOnInit() {
    this.currentUser = this.userService.getCurrentUser();
    this.selectedindex = 1;
    this.emptyMessage = global.no_workitem_found;
    this.breadcrumbService.setItems([
      {label: 'Admin'},
      {label: 'Lookups'}
    ]);
    this.roleData.roles = {
      selectCriterions: [
        {label: 'Email', value: 'EMAIL'},
        {label: 'Name', value: 'NAME'},
        {label: 'Designation', value: 'TITLE'},
        {label: 'Phone', value: 'PHONE'},
        {label: 'Org Code', value: 'ORGCODE'},
        {label: 'Koc No', value: 'KOCNO'}],
      result: undefined, model: {selectedCriterion: 'NAME'}
    };
    this.getOrgRole();
    const subscription = this.as.getLookups().subscribe(data => this.assignLookUpNames(data));
    this.coreService.progress = {busy: subscription, message: '', backdrop: true};
    this.addToSubscriptions(subscription);
  }

  getOrgRole() {
    const subscription = this.userService.getTopRolesList().subscribe(res => {
      const response = res;
      this.tmpRoleTree = [];
      this.tmpRoleTree.push({
        label: res.headRoleName,
        data: res,
        expandedIcon: this.roleTreeExpandedIcon,
        collapsedIcon: this.roleTreeCollapsedIcon,
        leaf: false,
        expanded: true
      });
      this.roleData.roles.roleTree = this.tmpRoleTree;
      this.getSubOrgRoles(this.tmpRoleTree[0], true);
    }, err => {
    });
    this.coreService.progress = {busy: subscription, message: '', backdrop: true};
    this.addToSubscriptions(subscription);
  }

  getSubOrgRoles(parent, init) {
    const subscription = this.userService.getSubRolesList(parent.data.id).subscribe((res: any) => {
      parent.children = [];
      res.map(d => {
        parent.children.push({
          label: d.headRoleName, data: d,
          expandedIcon: this.roleTreeExpandedIcon,
          collapsedIcon: this.roleTreeCollapsedIcon,
          leaf: false
        });
      });
      if (init) {
        this.getUserSupervisorTree();
      }


    }, err => {

    });
    this.coreService.progress = {busy: subscription, message: '', backdrop: true};
    this.addToSubscriptions(subscription);
  }

  getUserSupervisorTree() {
    const subscription = this.userService.getUserSupervisorTree(this.currentUser.EmpNo).subscribe((res: any) => {
      if (res.length > 1) {
        this.setChildren(this.tmpRoleTree[0], res, 1);
      }
      else {
        this.roleData.roles.roleTree = this.tmpRoleTree;
      }
    }, err => {

    });
    this.coreService.progress = {busy: subscription, message: '', backdrop: true};
    this.addToSubscriptions(subscription);
  }

  refreshLookupTable() {
    const subscription = this.as.getLookups().subscribe(data => this.assignLookUpNamesAfterEdit(data));
    this.coreService.progress = {busy: subscription, message: '', backdrop: true};
    this.addToSubscriptions(subscription);
  }

  filterLookup() {
    if (this.query.length > 0) {
      this.lookupValues = [];
      this.selectedLookup = undefined;
    }
    else {
      const subscription = this.as.getLookups().subscribe(data => this.assignLookUpNames(data));
      this.coreService.progress = {busy: subscription, message: '', backdrop: true};
      this.addToSubscriptions(subscription);
    }

  }

  confirmClear() {
    const subscription = this.as.getLookups().subscribe(data => this.assignLookUpNames(data));
    this.coreService.progress = {busy: subscription, message: '', backdrop: true};
    this.addToSubscriptions(subscription);
    this.orgName=undefined;
  }

  assignLookUpNamesAfterEdit(data) {
    this.lookupList = data;
  }

  assignLookUpNames(data) {
    this.lookupList = data;
    if (data.length > 0) {
      this.selectedindex = data[0].id;
      this.selectedLookup = this.lookupList[0];
      const subscription = this.as.getLookupValues(this.lookupList[0].id).subscribe(val => this.assignLookUpValues(val));
      this.coreService.progress = {busy: subscription, message: '', backdrop: true};
      this.addToSubscriptions(subscription);
    } else {
      this.lookupValues = [];
    }
  }

  assignLookUpValues(val) {
    this.lookupValues = val;
  }

  showLookUpValues(data) {
    this.selectedindex = data.id;
    this.busy = this.as.getLookupValues(this.selectedindex).subscribe(val => {
      this.lookupValues = val;
    });

    this.newLookUp = {
      id: undefined,
      label: undefined,
      value: undefined
    };
  }

  setChildren(parent, response, index) {
    let newParent;
    if (!parent.children) {
      parent.children = [];
      parent.children.push({
        label: response[index].headRoleName, data: response[index], expandedIcon: this.roleTreeExpandedIcon,
        collapsedIcon: this.roleTreeCollapsedIcon, leaf: false, expanded: true
      });
      newParent = parent.children[0];
    } else {
      parent.children.map(c => {
        if (c.data.id === response[index].id) {
          c.expanded = true;
          newParent = c;
        }
      });
    }
  }

  save() {
    if (this.indexValue !== undefined) {
      this.lookupValues[this.indexValue].label = this.dat.label;
      this.lookupValues[this.indexValue].value = this.dat.value;
    }
    else {
      this.dat.id = undefined;
      this.dat.label = this.dat.label;
      this.dat.value = this.dat.value;
      this.lookupValues.push(this.dat);
    }
    let post = this.selectedLookup;
    post.values = this.lookupValues;
    this.as.updateLookupValues(post).subscribe(data => this.addSuccess(data), err => this.addFailed());
    this.showEdit = true;

  }

  clickInput(data, i) {
    this.headerTitle = 'Edit Lookup Values';
    this.dat.label = data.label;
    this.dat.value = data.value;
    this.indexValue = i;

  }

  addSuccess(dat) {
    if (dat === 'Already Exists') {
      this.growlService.showGrowl({
        severity: 'error',
        summary: 'Already Exist', detail: 'Lookup Value Already Exist'
      });
      this.lookupValues.pop();
    }
    else {
      this.growlService.showGrowl({
        severity: 'info',
        summary: 'Success', detail: 'Saved Lookup Value Successfully'
      });

      this.showEdit = false;
      this.refresh();
    }
  }

  addFailed() {
    this.growlService.showGrowl({
      severity: 'error',
      summary: 'Failure', detail: 'Failed To Save Lookup Value'
    });
    this.showEdit = true;
    this.refresh();
  }

  add() {
    this.dat = new LookupValue();
    this.headerTitle = 'Add Lookup Values';
    this.indexValue = undefined;
  }

  refresh() {
    this.as.getLookupValues(this.selectedindex).subscribe(val => {
      this.lookupValues = val;
    });
  }

  clearSubscriptions() {
    this.subscriptions.map(s => {
      s.unsubscribe();
    });
  }

  addToSubscriptions(subscription) {
    this.subscriptions.push(subscription);
  }

  confirmdeleteLookupValue(rowdat) {
    this.confirmationService.confirm({
      message: 'Are you sure that you want to Delete?',
      header: 'Delete Confirmation',
      icon: 'ui-icon-help',
      accept: () => {
        this.deleteLookupValue(rowdat);
      },
      reject: () => {
      }
    });
  }

  confirmdeleteLookup(rowdat) {
    this.confirmationService.confirm({
      message: 'Are you sure that you want to Delete this Lookup?',
      header: 'Delete Confirmation',
      icon: 'ui-icon-help',
      accept: () => {
        this.deleteLookup(rowdat);
      },
      reject: () => {
      }
    });
  }

  deleteLookup(data) {
    this.as.removeLookup(data.id).subscribe(data => this.removelookupsuccess(data), err => this.removelookupfailed());
  }

  removelookupsuccess(data) {
    if (data === 'Mapping Exists') {
      this.growlService.showGrowl({
        severity: 'error',
        summary: 'Cannot Remove', detail: 'Mapping Exist'
      });
    }
    else {
      this.growlService.showGrowl({
        severity: 'info',
        summary: 'Success', detail: 'Removed Lookup Successfully'
      });
    }


    const subscription = this.as.getLookups().subscribe(data => this.assignLookUpNamesAfterDelete(data));
    this.coreService.progress = {busy: subscription, message: '', backdrop: true};
    this.addToSubscriptions(subscription);

  }

  assignLookUpNamesAfterDelete(data) {
    this.lookupList = data;
    this.lookupValues = [];

  }

  removelookupfailed() {
    this.growlService.showGrowl({
      severity: 'error',
      summary: 'Failure', detail: 'Remove Lookup Failed'
    });
  }

  deleteLookupValue(rowdat) {
    let deletelookup = this.selectedLookup;
    this.as.removeLookupValue(rowdat.id, deletelookup.id).subscribe(data => this.removesuccess(), err => this.removefailed());

  }

  removesuccess() {
    this.growlService.showGrowl({
      severity: 'info',
      summary: 'Success', detail: 'Removed Lookup Value'
    });
    this.refresh();
  }

  removefailed() {
    this.growlService.showGrowl({
      severity: 'error',
      summary: 'Failure', detail: 'Failed to Remove Lookup Value'
    });
  }

  addEditLookup(dat, i) {
    this.lookup = new Lookup();
    if (dat === undefined) {
      this.headerTitleLookup = 'Add Lookup';
      this.lookup.id = 0;
    }
    else {
      this.headerTitleLookup = 'Edit Lookup';
      this.lookup.id = dat.id;
      this.lookup.name = dat.name;
    }

  }

  saveLookup() {
    this.as.saveLookup(encodeURIComponent(this.lookup.name), this.lookup.id).subscribe(data => this.savelookupsuccess(data), err => this.savelookupfailed())
  }

  savelookupsuccess(data) {
    if (data === 'Lookup Exists') {
      this.growlService.showGrowl({
        severity: 'error',
        summary: 'Already Exist', detail: 'Lookup Already Exist'
      });
    }
    else {
      this.growlService.showGrowl({
        severity: 'info',
        summary: 'Success', detail: 'Saved Lookup Successfully'
      });
      this.refreshLookupTable();
      if (this.headerTitleLookup === 'Edit Lookup') {
        this.selectedLookup = this.lookup;
        this.as.getLookupValues(this.lookup.id).subscribe(val => {
          this.lookupValues = val;
        });
      }
    }
  }

  savelookupfailed() {
    this.growlService.showGrowl({
      severity: 'error',
      summary: 'Failure', detail: 'Failed to Save Lookup'
    });
  }

  search(event) {
    const subscription = this.as.searchOrgUnits(event.query).subscribe(data => {
      this.suggestionsResults = data;
    });
    this.coreService.progress = {busy: subscription, message: '', backdrop: true};
    this.addToSubscriptions(subscription);
  }

  orgUnitSelected(selected) {
    this.selectedOrgUnit = selected;
    const subscription = this.as.getLookupsByOrgId(this.selectedOrgUnit.id).subscribe(data => this.assignLookUpNames(data));
    this.coreService.progress = {busy: subscription, message: '', backdrop: true};
    this.addToSubscriptions(subscription);
  }

  ngOnDestroy() {
    this.clearSubscriptions();
  }

}
