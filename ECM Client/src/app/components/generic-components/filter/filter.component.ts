import {Component, OnInit, Input, Output, EventEmitter, OnChanges, OnDestroy, ViewChild} from '@angular/core';
import {Router} from '@angular/router';
import {Subscription} from 'rxjs/Rx';
import {MenuItem, SelectItem} from 'primeng/primeng';
import {saveAs} from 'file-saver';
//services
import {WorkflowService} from '../../../services/workflow.service';
import * as $ from 'jquery';
import {CoreService} from "../../../services/core.service";

@Component({
  selector: 'app-filter',
  templateUrl: './filter.component.html',
  styleUrls: ['./filter.component.css']
})
export class FilterComponent implements OnInit, OnDestroy {
  @Input() public activePage: any;
  @Output() filterData = new EventEmitter();
  @Output() clearFilter = new EventEmitter();
  public status: SelectItem[] = [];
  public type: SelectItem[] = [];
  public priority: SelectItem[] = [];
  @Input() public filterSenderoptions: any;
  public senderName: string[] = [];
  @Input() public filterQuery: any = {};
  @Input() public filteredData: any;
  @Input() public filterCount = {
    total: 0,
    to: 0,
    cc: 0,
    reply: 0,
    replyto: 0,
    replycc: 0,
    new: 0,
    read: 0,
    forwarded: 0
  };
  @Input() public searchFromDashboard: any;
  @Input() public id: any;
  selectedReceivedDate: Date;
  selectedDeadline: Date;
  maxDate: Date;
  public disableReceivedAndDuedate = false;
  private subscriptions: Subscription[] = [];
  private pageUrl: any;
  private currentPage: any;
  public filterNewToday = false;
  @ViewChild('filterForm') filterForm;
  public exportBtnItems: MenuItem[] = [];
  @Input() public forOptions: any;
  constructor(private workflowService: WorkflowService, private router: Router,private coreService:CoreService) {

    this.pageUrl = router.url;
    this.currentPage = (this.pageUrl.slice(this.pageUrl.indexOf('workflow/') + 9)).split('/');
  }

  ngOnInit() {
    this.status = [{label: 'Not Actioned', value: ''}, {label: 'New', value: 'New'},
      {label: 'Read', value: 'Read'}, {label: 'Overdue', value: 'overdue'}];
    this.type = [{label: 'To', value: 'TO'}, {label: 'CC', value: 'CC'}];
    if (this.activePage === 'sent') {
      this.type.push({label: 'Reply-To', value: 'Reply-TO'});
      this.type.push({label: 'Reply-CC', value: 'Reply-CC'});
    }
    this.priority = [{label: 'Low', value: 1}, {label: 'Normal', value: 2}, {label: 'High', value: 3}];

    this.maxDate = new Date();
    if (this.activePage === 'inbox') {
      this.status.push({label: 'New Today', value: 'New'});
    }
    if (this.searchFromDashboard) {
      this.filterFromDashBoard(this.searchFromDashboard);
    }
    this.exportBtnItems.push({
      'label': 'PDF', command: event => {
        this.exportToPdf();
      }
    },{
      'label': 'Excel', command: event => {
        this.exportToExcel();
      }
    });
  }


  filter() {
    if(!this.searchFromDashboard){
      this.setReceivedDate();
      this.setDueDate();
    }
    if(this.filterQuery.status === 'Forward'){
      this.filterQuery.actionId = 1;
    } else {
      this.filterQuery.actionId = 0;
    }
    if(this.filterQuery.status === 'overdue'){
       // const today = new Date();
       // const nextDay = new Date(today);
       // nextDay.setDate(today.getDate()+1);
       this.filterQuery.deadline = this.maxDate.getDate() + '/' + (this.maxDate.getMonth() + 1) + '/' + this.maxDate.getFullYear();
       this.filterQuery.receivedDate = undefined;
    } else if(this.filterNewToday) {
       this.filterQuery.receivedDate = this.maxDate.getDate() + '/' + (this.maxDate.getMonth() + 1) + '/' + this.maxDate.getFullYear();
       this.filterQuery.deadline = undefined;
    }
    this.filterData.emit();
  }

  filterFromDashBoard(searchQueryFromDashboard) {
    if (searchQueryFromDashboard.filterStatus === 'Read') {
      this.filterQuery.status = 'Read';
    } else if (searchQueryFromDashboard.filterStatus === 'Unread') {
      this.filterQuery.status = 'New';
    } else if (searchQueryFromDashboard.filterStatus === 'Pending') {
      this.filterQuery.sysStatus = 'Read';
    } else if (searchQueryFromDashboard.filterStatus === 'New') {
      this.filterQuery.sysStatus = 'New';
    }
    if (searchQueryFromDashboard.filterReceivedDay === 'Today') {
      this.filterQuery.receivedDate = this.maxDate.getDate() + '/' + (this.maxDate.getMonth() + 1) + '/' + this.maxDate.getFullYear();
    } else if (searchQueryFromDashboard.filterReceivedDay === 'Total') {
      this.filterQuery.receivedDate = '';
    } else if (searchQueryFromDashboard.filterReceivedDay === 'deadline') {
      this.filterQuery.status = 'overdue';
    }
    if (this.searchFromDashboard.filterWIType) {
      this.filterQuery.type = this.searchFromDashboard.filterWIType;
    }
    if (this.id === searchQueryFromDashboard.filterUserId) {
      this.filter();
    }
  }

  setReceivedDate() {
    if (!this.selectedReceivedDate) {
      this.filterQuery.receivedDate = undefined;
      return;
    }
    if (this.selectedReceivedDate[0]) {
      const fromDate = new Date(this.selectedReceivedDate[0]);
      this.filterQuery.receivedDate = fromDate.getDate() + '/' + (fromDate.getMonth() + 1) + '/' + fromDate.getFullYear();
    }
    if (this.selectedReceivedDate[1]) {
      const date = new Date(this.selectedReceivedDate[1]);
      this.filterQuery.receivedDate = this.filterQuery.receivedDate + ';' + date.getDate() + '/' + (date.getMonth() + 1) + '/' + date.getFullYear();

    }

  }

  setDueDate() {
    if (!this.selectedDeadline) {
      this.filterQuery.deadline = undefined;
      return;
    }
    if (this.selectedDeadline[0]) {
      const fromDate = new Date(this.selectedDeadline[0]);
      this.filterQuery.deadline = fromDate.getDate() + '/' + (fromDate.getMonth() + 1) + '/' + fromDate.getFullYear();
    }
    if (this.selectedDeadline[1]) {
      const date = new Date(this.selectedDeadline[1]);
      this.filterQuery.deadline = this.filterQuery.deadline + ';' + date.getDate() + '/' + (date.getMonth() + 1) + '/' + date.getFullYear();
    }
  }

  clearFilterResults() {
    this.resetFilter();
    this.clearFilter.emit({'bool': true, 'id': this.filterQuery.userId});
  }
  resetFilter() {
    const temp={userType:this.filterQuery.userType,userId:this.filterQuery.userId,recipientName:this.filterQuery.recipientName,
    empNo:this.filterQuery.empNo,pageNo:1};
    Object.keys(this.filterQuery).map(k=>{
      this.filterQuery[k]=undefined;
    });
    this.filterQuery = Object.assign(this.filterQuery,
      temp);
    this.resetReceivedDatePicker(null);
    this.resetDueDatePicker(null);
    this.senderName = [];
    this.disableReceivedAndDuedate = false;
  }

  senderChange(event) {
    if (event.value.length) {
      let roles = 'ROLE:';
      let users = 'USER:';
      let roleCount = 0;
      let userCount = 0;
      for (const role of event.value) {
        const r = role.split(':');
        if (r[0] === 'ROLE') {
          roleCount++;
          roles = roles + r[1] + ';';
        }
      }
      for (const user of event.value) {
        const u = user.split(':');
        if (u[0] === 'USER') {
          userCount++;
          users = users + u[1] + ';';
        }
      }
      roles = roles.slice(0, -1);
      users = users.slice(0, -1);
      if (roleCount > 0 && userCount > 0) {
        if (this.activePage === 'inbox') {
          this.filterQuery.senderName = roles + '@' + users;
        } else if (this.activePage === 'sent') {
          this.filterQuery.recipientName = roles + '@' + users;
        }

      } else if (roleCount > 0) {
        if (this.activePage === 'inbox') {
          this.filterQuery.senderName = roles;
        } else if (this.activePage === 'sent') {
          this.filterQuery.recipientName = roles;
        }
      } else if (userCount > 0) {
        if (this.activePage === 'inbox') {
          this.filterQuery.senderName = users;
        } else if (this.activePage === 'sent') {
          this.filterQuery.recipientName = users;
        }
      }

    }

  }

  statusChange(event) {
    if (event.originalEvent.srcElement.firstElementChild.innerText == 'New Today') {
      this.filterNewToday = true;
      this.disableReceivedAndDuedate = true;
    } else if (event.value === 'overdue') {
       this.disableReceivedAndDuedate = true;
    } else {
       this.filterNewToday = false;
       this.disableReceivedAndDuedate = false;
    }
  }

  exportToExcel() {
    this.filterQuery.exportFormat = 'xls';
    this.filterQuery.exportFilter = true;
    if (this.activePage === 'inbox') {
      let fileName;
      if(this.activePage === 'inbox' && this.currentPage[0] === 'archive'){
        fileName = 'Archived_Inbox_Report' + '.xlsx';
      } else {
        fileName = 'Inbox_Report' + '.xlsx';
      }
      const subscription=this.workflowService.exportInbox(this.filterQuery).subscribe(res => {
        const file = new Blob([res], {type: 'application/vnd.ms-excel'});
        saveAs(file, fileName);
      });
      this.coreService.progress={busy:subscription,message:'',backdrop:true};
      this.addToSubscriptions(subscription);
    } else if (this.activePage === 'sent' && this.currentPage[0] !== 'actioned') {
        let fileName;
        if(this.activePage === 'sent' && this.currentPage[0] === 'archive'){
          fileName = 'Archived_Sent_Report' + '.xlsx';
        } else {
          fileName = 'Sent_Report' + '.xlsx';
        }
        const subscription=this.workflowService.exportSent(this.filterQuery).subscribe(res => {
          const file = new Blob([res], {type: 'application/vnd.ms-excel'});
          saveAs(file, fileName);
        });
        this.coreService.progress={busy:subscription,message:'',backdrop:true};
        this.addToSubscriptions(subscription);
    } else if (this.activePage === 'sent' && this.currentPage[0] === 'actioned') {
      const subscription=this.workflowService.exportActioned(this.filterQuery).subscribe(res => {
        const file = new Blob([res], {type: 'application/vnd.ms-excel'});
        const fileName = 'Actioned Report' + '.xlsx';
        saveAs(file, fileName);
      });
      this.coreService.progress={busy:subscription,message:'',backdrop:true};
      this.addToSubscriptions(subscription);
    }
  }

  exportToPdf() {
    this.filterQuery.exportFormat = 'pdf';
    this.filterQuery.exportFilter = true;
    if (this.activePage === 'inbox') {
      let fileName;
      if(this.activePage === 'inbox' && this.currentPage[0] === 'archive'){
        fileName = 'Archived_Inbox_Report' + '.pdf';
      } else {
        fileName = 'Inbox_Report' + '.pdf';
      }
      const subscription=this.workflowService.exportInbox(this.filterQuery).subscribe(res => {
        const file = new Blob([res], {type: 'application/pdf'});
        saveAs(file, fileName);
      });
      this.coreService.progress={busy:subscription,message:'',backdrop:true};
    this.addToSubscriptions(subscription);
    } else if (this.activePage === 'sent' && this.currentPage[0] !== 'actioned') {
      let fileName;
        if(this.activePage === 'sent' && this.currentPage[0] === 'archive'){
          fileName = 'Archived_Sent_Report' + '.pdf';
        } else {
          fileName = 'Sent_Report' + '.pdf';
        }
      const subscription=this.workflowService.exportSent(this.filterQuery).subscribe(res => {
        const file = new Blob([res], {type: 'application/pdf'});
        saveAs(file, fileName);
      });
      this.coreService.progress={busy:subscription,message:'',backdrop:true};
    this.addToSubscriptions(subscription);
    } else if (this.activePage === 'sent' && this.currentPage[0] === 'actioned') {
      const subscription=this.workflowService.exportActioned(this.filterQuery).subscribe(res => {
        const file = new Blob([res], {type: 'application/pdf'});
        const fileName = 'Actioned Report' + '.pdf';
        saveAs(file, fileName);
      });
      this.coreService.progress={busy:subscription,message:'',backdrop:true};
    this.addToSubscriptions(subscription);
    }
  }
  resetReceivedDatePicker(event) {
    this.filterQuery.receivedDate = undefined;
    this.selectedReceivedDate=undefined;
  }

  resetDueDatePicker(event) {
    this.filterQuery.deadline = undefined;
    this.selectedDeadline=undefined;
  }

  collapse(event) {
    $('.filter').slideUp();
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
    this.subscriptions = undefined;
    this.status = [];
    this.type = [];
    this.priority = [];
    this.senderName = [];
    this.selectedReceivedDate = undefined;
    this.selectedDeadline = undefined;
  }
}
