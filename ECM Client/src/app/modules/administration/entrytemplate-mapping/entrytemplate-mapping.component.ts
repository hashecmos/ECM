import {EntryTemplate} from '../../../models/document/entry-template.model';
import {AdminService} from '../../../services/admin.service';
import {ContentService} from '../../../services/content-service.service';
import {GrowlService} from '../../../services/growl.service';
import {Component, OnInit} from '@angular/core';
import {ConfirmationService} from 'primeng/primeng';
import {BreadcrumbService} from '../../../services/breadcrumb.service';
import {CoreService} from '../../../services/core.service';

@Component({
  selector: 'app-entrytemplate-mapping',
  templateUrl: './entrytemplate-mapping.component.html',
  styleUrls: ['./entrytemplate-mapping.component.css']
})
export class EntrytemplateMappingComponent implements OnInit {

  constructor(private cs: ContentService,private coreService:CoreService, private adminService: AdminService, private growlService: GrowlService, private confirmationService: ConfirmationService,private breadcrumbService: BreadcrumbService) {
  }

  entryTemp: any;
  selectedEntryTemplate: any;
  private orgCodes: any[];
  mappedList: any[];
  selectedorgCode: any;
  isvisible = true;
  private subscriptions: any[] = [];
  ngOnInit() {
     const subscription = this.cs.getAllEntryTemplates().subscribe(data => this.assignEntrytemplate(data));
     this.breadcrumbService.setItems([
       {label: 'Admin'},
      {label: 'Entry Template Mapping'}
    ]);
    this.coreService.progress = {busy: subscription, message: '', backdrop: true};
    this.addToSubscriptions(subscription);
  }

  assignEntrytemplate(data) {
    this.entryTemp = data;
    if (this.entryTemp.length > 0) {
      this.selectedEntryTemplate = this.entryTemp[0];
       const subscription = this.adminService.getOrgUnitsByEntryTemplate(data[0].vsid).subscribe(val => this.assignMappedIds(val));
        this.coreService.progress = {busy: subscription, message: '', backdrop: true};
        this.addToSubscriptions(subscription);
    }
  }

  showTemplateMapping(data) {
    this.selectedorgCode = undefined;
    const subscription =this.adminService.getOrgUnitsByEntryTemplate(data.vsid).subscribe(val => this.assignMappedIds(val));
     this.coreService.progress = {busy: subscription, message: '', backdrop: true};
     this.addToSubscriptions(subscription);
  }

  getOrgCodes(event) {
    this.adminService.searchOrgUnits(event.query).subscribe(res => {
      this.orgCodes = res;
    }, err => {

    });
  }

  assignMappedIds(data) {
    this.mappedList = data;
  }

  mapEntryTemplate() {
    let isExist = false;
    this.mappedList.map((d, i) => {
      if (d.id === this.selectedorgCode.id) {
        isExist = true;
      }
    });
    if (isExist) {
      this.growlService.showGrowl({
        severity: 'error',
        summary: 'Already Exist', detail: 'Mapping Already Exist'
      });
    }
    else {
      this.adminService.addEntryTemplateMapping(this.selectedorgCode.id, this.selectedEntryTemplate.id, this.isvisible, this.selectedEntryTemplate.vsid).subscribe(data => this.mapSuccess(), err => this.mapFailed());
    }
  }

  mapSuccess() {
    this.growlService.showGrowl({
      severity: 'info',
      summary: 'Success', detail: 'Mapped To Entry Template'
    });
    this.selectedorgCode = undefined;
    this.adminService.getOrgUnitsByEntryTemplate(this.selectedEntryTemplate.vsid).subscribe(val => this.assignMappedIds(val));
  }

  mapFailed() {
    this.growlService.showGrowl({
      severity: 'error',
      summary: 'Failure', detail: 'Failed To Map To Entry Template'
    });
  }

  removeMapping(dat) {
    this.adminService.removeEntryTemplateMapping(dat.id, this.selectedEntryTemplate.id).subscribe(data => this.removeSuccess(), err => this.removeFailed());
  }

  removeSuccess() {
    this.growlService.showGrowl({
      severity: 'info',
      summary: 'Success', detail: 'Removed From Mapping'
    });
    this.adminService.getOrgUnitsByEntryTemplate(this.selectedEntryTemplate.vsid).subscribe(val => this.assignMappedIds(val));
  }

  removeFailed() {
    this.growlService.showGrowl({
      severity: 'error',
      summary: 'Failure', detail: 'Fail To Remove From Mapping'
    });
  }

  confirm(event) {
    this.confirmationService.confirm({
      message: 'Are you sure that you want to remove' + ' ' + event.desc + ' ' + 'from Mapping?',
      accept: () => {
        //Actual logic to perform a confirmation
        this.removeMapping(event);
      }
    });
    this.adminService.getOrgUnitsByEntryTemplate(this.selectedEntryTemplate.vsid).subscribe(val => this.assignMappedIds(val));
  }

  checkInvisible(event) {
    this.isvisible = event;

  }
  clearSubscriptions() {
    this.subscriptions.map(s => {
      s.unsubscribe();
    });
  }

  addToSubscriptions(subscription) {
    this.subscriptions.push(subscription);
  }

  ngOnDestroy() {
    this.clearSubscriptions();
  }
}
