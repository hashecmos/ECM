import { Component, ChangeDetectionStrategy, OnInit, ViewChild } from '@angular/core';
import { ReportService } from '../../services/reports.service';
import 'rxjs/add/observable/of';
import { saveAs } from 'file-saver';
import {IMyOptions, IMyDateModel, IMyDate, MyDatePicker, IMyInputFieldChanged} from 'mydatepicker';
declare var $: any;
import { FormBuilder, FormGroup, Validators, FormControl } from '@angular/forms';

export let dateObj = new Date();
dateObj.setDate(dateObj.getDate() - 1)
export let years = dateObj.getFullYear();
export let months = dateObj.getMonth()+1;
export let days = dateObj.getDate();

@Component({
    selector: 'app-blank-page',
    changeDetection: ChangeDetectionStrategy.OnPush,
    templateUrl: './document.component.html',
    styleUrls : ['./document.component.css']
})

export class DocumentComponent implements OnInit {
  
  @ViewChild('tree') tree;
  @ViewChild('testTab') testTab;
  @ViewChild('viewReportTab') viewReportTab;
  @ViewChild('tabset') tabset;
  
  public disableTab = true;
  public selectTab = false;
  public unselectTab = true;
  public customClass = 'customClass';
  public isFirstOpen = true;
  public orgLevels: any;
  public subOrgUnit: any;
  public groups1: any;
  public teams: any;
  public unit: any;
  public list; any;
  public reportData: any;
  public chartData: any;
  public documentCount: any;
  public desc = '';
  public orgCode = '';
  public reportOrgCode = '';
  public fromDate: any;
  public toDate: any;
  public container: any;

  public wStatus: any;
  public myDatePickerOptions: IMyOptions;
  public myDatePickerDisableOptions: IMyOptions;
  public selectedFromDate: any;
  public selectedToDate: any;
  constructor(public us: ReportService, public fb: FormBuilder) {
    this.us.getTopLevelOrgUnit().subscribe(data => this.getTopLevelOrgUnit(data));
    this.myDatePickerOptions = {
      dateFormat: 'dd/mm/yyyy',
      showTodayBtn: false,
      editableDateField: false,
      markCurrentDay: true,
      showClearDateBtn: false,
      openSelectorOnInputClick: true,
      disableSince: {year: years, month: months, day: new Date().getDate() + 1}
    }
    this.myDatePickerDisableOptions = {
      dateFormat: 'dd/mm/yyyy',
      showTodayBtn: false,
      editableDateField: false,
      markCurrentDay: true,
      showClearDateBtn: false,
      openSelectorOnInputClick: true,
      componentDisabled: true
    };
  }

  public getTopLevelOrgUnit(data): void  {
    this.orgLevels = JSON.parse(data._body);
    this.desc = this.orgLevels.desc;
    this.desc = this.desc.slice();
    this.orgCode = this.orgLevels.orgCode;
    //this.us.getSubOrgUnits(this.orgLevels.orgCode).subscribe(data => this.getSubOrgUnits(data));
  }
  test(de) {

    this.us.getSubOrgUnits(de).subscribe(data => this.getSubOrgUnits(data));
  }
  testGroups(or) {
    this.us.getSubOrgUnits(or).subscribe(data => this.getGroups(data));
  }
  testTeam(or) {
    this.us.getSubOrgUnits(or).subscribe(data => this.getTeams(data));
  }
  testUnit(or) {
    this.us.getSubOrgUnits(or).subscribe(data => this.getUnit(data));
  }
  finalList(or) {
    this.us.getSubOrgUnits(or).subscribe(data => this.finally(data));
  }
  finally(data) {
    this.list = JSON.parse(data._body);
  }
  getUnit(data) {
    this.unit = JSON.parse(data._body);
  }
  getTeams(data) {
    this.teams = JSON.parse(data._body);
  }
  getGroups(data) {
    this.groups1 = JSON.parse(data._body);
  }
  getSubOrgUnits(data) {
    this.subOrgUnit = JSON.parse(data._body);
  }

  copyOrgCode(orgCode) {
    this.reportOrgCode = orgCode;
  }
  
  ngAfterViewInit(){
    setTimeout(() => {
      let el: HTMLElement = this.testTab.nativeElement as HTMLElement;
      el.click();
    },1000);
  }
  
  ngOnInit() {

          }
  
  exportData() {
      
    let startExport = true;
    let selectedOption = $('#exportType').val();
    if (selectedOption != 'pdf' && selectedOption != 'excel') {
      startExport = false;
    }
    if (startExport) {
      var mimeType = '';
      var fileName = '';
      if(selectedOption =='pdf'){
        mimeType = 'application/pdf';
        fileName = 'Document Count' + '.pdf';
      }else if(selectedOption =='excel'){
        mimeType = 'application/vnd.ms-excel';
        fileName = 'Document Count' + '.xlsx';
      }
      this.us.exportOrgDocumentCounts(this.reportOrgCode, 'ALL', this.fromDate, this.toDate, selectedOption).subscribe(res => {
      const file = new Blob([res.blob()], { type: mimeType });
      saveAs(file, fileName);
    });
    }else {
      alert('Please select the export type...');
    }
    //this.us.getReportInboxItems(this.reportOrgCode, $('#options').val(), this.fromDate, this.toDate).subscribe(data => this.workItemCount(data));
  }
  activateViewTab() {
    //http://localhost:9080/ECMService/resources/ReportService/getOrgWorkitemCounts?orgcode=TK310&status=Pending
     for (let dateField of [].slice.call($('my-date-picker'))) {

            if (dateField.id != undefined && dateField.id == 'fromDt' ) {

                    this.fromDate = dateField.childNodes[0].children[0].children[0].value;

            }else if (dateField.id != undefined && dateField.id == 'toDt' ) {

                    this.toDate = dateField.childNodes[0].children[0].children[0].value;

            }
    }

    this.us.getOrgDocumentCounts(this.reportOrgCode, this.fromDate, this.toDate).subscribe(data => this.documentCountReport(data));
    /*console.log(this.reverseString($('#fromDate').val()));
    console.log(this.reverseString($('#toDate').val()));
    console.log($('#options').val());
    console.log(this.reportOrgCode);*/
    this.selectTab = true;
  }
  documentCountReport(data) {
    this.disableTab = false;
    this.unselectTab = false;
    this.selectTab = true;
    this.reportData = JSON.parse(data._body);
    this.documentCount = [];
    this.documentCount = this.reportData;
    $('#image').html('');
    $('#image').html('<img class="logo-navbar" src="assets/img/ecm.png">');
    this.tabset.tabs[1].disabled = false;
    this.tabset.tabs[1].active = true;
  }
  reverseString(str) {
 
    var splitString = str.split("-"); 
   var joinArray = splitString[2] + '-' + splitString[1] + '-' + splitString[0];
    return joinArray; 
}
  activateSearchTab() {
    this.selectTab = false;
    this.unselectTab = true;
    console.log('activateSearchTab' + this.selectTab);
    this.reportOrgCode = undefined;
    this.selectedFromDate = undefined;
    this.selectedToDate = undefined;
    this.disableTab = true;
    this.myDatePickerDisableOptions = {
      dateFormat: 'dd/mm/yyyy',
      showTodayBtn: false,
      editableDateField: false,
      markCurrentDay: true,
      showClearDateBtn: false,
      openSelectorOnInputClick: true,
      componentDisabled: true
    };
  }
  
  disableToDate(event: IMyInputFieldChanged){
    let todayDate = new Date();
    let date = event.value.split('/');
    if (parseInt(date[0]) == 1) {
      var disableuntil = {year: parseInt(date[2]), month: parseInt(date[1]), day: parseInt(date[0])};
    } else {
      var disableuntil = {year: parseInt(date[2]), month: parseInt(date[1]), day: (parseInt(date[0]) - 1)};
    }
    this.myDatePickerDisableOptions = {
      dateFormat: 'dd/mm/yyyy',
      showTodayBtn: false,
      disableSince : {year: todayDate.getFullYear(), month: todayDate.getMonth() + 1, day: todayDate.getDate() + 1},
      disableUntil: disableuntil,
      editableDateField: false,
      markCurrentDay: true,
      showClearDateBtn: false,
      openSelectorOnInputClick: true,
      componentDisabled: false
    };
}

   handleOrgCodeUpdated(event){
    this.reportOrgCode = event;
  }
  alertPop() {
  }
  viewReportClickEvent() {
  }
  activateResultTab(){
  }
}
