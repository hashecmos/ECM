import {ViewChild, Component, ChangeDetectionStrategy, OnInit} from '@angular/core';
import {ReportService} from '../../services/reports.service';
import 'rxjs/add/observable/of';
import {IMyInputFieldChanged, IMyOptions} from 'mydatepicker';
import { saveAs } from 'file-saver';
declare var $: any;

export let dateObj = new Date();
dateObj.setDate(dateObj.getDate() - 1);
export let years = dateObj.getFullYear();
export let months = dateObj.getMonth() + 1;
export let days = dateObj.getDate();

@Component({
  selector: 'app-blank-page',
  changeDetection: ChangeDetectionStrategy.OnPush,
  templateUrl: './blank-page.component.html',
  styleUrls: ['./blankPage.component.css']
})

export class BlankPageComponent implements OnInit {
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
  public desc = '';
  public orgCode = '';
  public reportOrgCode: any;
  public fromDate: any;
  public toDate: any;
  public container: any;
  public wStatus: any;
  public myDatePickerOptions: IMyOptions;
  public myDatePickerDisableOptions: IMyOptions;
  public status: any;
  public selectedFromDate: any;
  public selectedToDate: any;
  constructor(public us: ReportService) {
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

  alertPop() {

  }

  copyOrgCode(orgCode) {
    this.reportOrgCode = orgCode;
  }

  ngAfterViewInit() {
    setTimeout(() => {
      let el: HTMLElement = this.testTab.nativeElement as HTMLElement;
      el.click();
    }, 500);
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
        fileName = 'Inbox Count' + '.pdf';
      }else if(selectedOption =='excel'){
        mimeType = 'application/vnd.ms-excel';
        fileName = 'Inbox Count' + '.xlsx';
      }
      this.us.exportOrgWorkitemCounts(this.reportOrgCode, this.status, this.fromDate, this.toDate, selectedOption).subscribe(res => {
      const file = new Blob([res.blob()], { type: mimeType });
      saveAs(file, fileName);
    });
    }else {
      alert('Please select the export type...');
    }
  }
  activateViewTab() {

    for (let dateField of [].slice.call($('my-date-picker'))) {

      if (dateField.id != undefined && dateField.id == 'fromDt') {

        this.fromDate = dateField.childNodes[0].children[0].children[0].value;

      } else if (dateField.id != undefined && dateField.id == 'toDt') {

        this.toDate = dateField.childNodes[0].children[0].children[0].value;

      }
    }
    this.us.getOrgWorkitemCounts(this.reportOrgCode, this.status, this.fromDate, this.toDate).subscribe(data => this.workItemCount(data));
    this.selectTab = true;
  }
  workItemCount(data) {
    this.disableTab = false;
    this.unselectTab = false;
    this.selectTab = true;

    this.reportData = JSON.parse(data._body);
    this.chartData = [];
    let barChartData = [];
    for (let rpt of this.reportData) {
      this.chartData.push(rpt.orgCode);
      barChartData.push(rpt.count);
    }
    if (this.status != null && this.status != undefined) {
      if(this.status === 'PENDING'){
        this.wStatus = 'Pending';
      } else if(this.status === 'COMPLETE'){
        this.wStatus = 'Completed';
      } else if(this.status === 'ARCHIVE'){
        this.wStatus = 'Archived';
      } else if(this.status === 'ALL'){
        this.wStatus = 'All';
      }
    }
    this.container = $('#container');
    this.container.highcharts({
      chart: {
        type: 'bar'
      },
      title: {
        // text: 'Workflow item inbox count'
        text:   'Inbox ' + this.wStatus + ' Workflow Item Count'
      },
      xAxis: {
        categories: this.chartData,
        title: {
          enabled: false
        }
      },
      yAxis: {
        title: {
          text: 'Count'
        }
      },
      tooltip: {
        pointFormat: '<span style="color:{series.color}">{series.name}</span>' +
        ': <b>{point.total}</b> ({point.y})<br/>',
        shared: true
      },
      plotOptions: {
        bar: {
          dataLabels: {
            enabled: true
          }
        }
      },
      legend: {
        layout: 'vertical',
        align: 'right',
        verticalAlign: 'top',
        x: -40,
        y: 100,
        floating: true,
        borderWidth: 1,
        backgroundColor: '#FFFFFF',
        shadow: true
      },
      credits: {
        enabled: false
      },
      series: [{
        name: 'Inbox Count',
        data: barChartData
      }]
    });
    $('#image').html('');
    $('#image').html('<img class="logo-navbar" src="assets/img/ecm.png">');
   // $('#image').html('<table style="width : 100%;"><tr style="width : 100%;"><td><img class="logo-navbar" src="assets/img/ecm.png"><div style="background: #00518d;">DIMS Reports</div><img class="logo-navbar" style="background-size:contain;" src="assets/img/koc-login-small.png"></td></tr></table>');
    if (this.container != undefined) {
      setTimeout(() => {
        if (this.container != undefined) {
          $("#container").highcharts().reflow();
        }
      }, 100);
    }
    this.tabset.tabs[1].disabled = false;
    this.tabset.tabs[1].active = true;
    
  }
  reverseString(str) {
    let splitString = str.split('/');
    let joinArray = splitString[2] + '/' + splitString[1] + '/' + splitString[0];
    return joinArray;
  }
  activateSearchTab() {
    this.selectTab = false;
    this.unselectTab = true;
    this.reportOrgCode = undefined;
    this.status = undefined;
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
    console.log('activateSearchTab' + this.selectTab);
  }

  changeFrom() {
    let fromVal = document.getElementById('getValFrom');

    if (fromVal) {
      let fromdate = document.getElementById('fromDt');
    }
    else {
      document.getElementById('fromDt');
    }
  }

  disableToDate(event: IMyInputFieldChanged) {
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
  handleOrgCodeUpdated(event) {
    this.reportOrgCode = event;
  }
  viewReportClickEvent() {
  }
  
  activateResultTab() {
  }
}
