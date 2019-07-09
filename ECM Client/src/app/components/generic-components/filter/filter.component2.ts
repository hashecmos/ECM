import {Component, OnInit, Input, Output, EventEmitter, OnChanges} from '@angular/core';
import {Router} from '@angular/router';
import {Subscription} from 'rxjs/Rx';
import {MenuItem, SelectItem} from 'primeng/primeng';
import {saveAs} from 'file-saver';
//models
import {WorkitemSet} from '../../../models/workflow/workitem-set.model';
//services
import {WorkflowService} from '../../../services/workflow.service';

@Component({
  selector: 'app-filter',
  templateUrl: './filter.component.html',
  styleUrls: ['./filter.component.css']
})
export class FilterComponent implements OnChanges,OnInit {
  @Input() public activePage: any;
  @Input() public userType: any;
  @Input() public id: any;
  @Input() public searchFromDashboard: any;
  @Input() public filterSenderoptions: any;
  @Input() public tieredClearFilter: any;
  @Output() sendData = new EventEmitter();
  @Output() collapseState = new EventEmitter();
  @Output() filterData = new EventEmitter();
  @Output() hasFilterResult = new EventEmitter();
  private subscription: Subscription[] = [];
  public status: SelectItem[] = [];
  public type: SelectItem[] = [];
  public priority: SelectItem[] = [];
  public senderName: string[] = [];
  public filterResultWorkitems: WorkitemSet[] = [];
  public filterQuery = {
    'priority': 0,
    'subject': '',
    'workitemId': 0,
    'status': '',
    'instructions': '',
    'type': '',
    'deadline': '',
    'receivedDate': '',
    'senderName': '',
    'recipientName': '',
    'comments': '',
    'userId': '',
    'userType': '',
    'repStatus': ''
  };
  public filterCount = {
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
  public hasFilterResults= false;
  selectedReceivedDate: Date;
  selectedDeadline: Date;
  maxDate: Date;
  private pageUrl: any;
  private currentPage: any;
  private fromDateSelected = false;
  private fromDeadlineDateSelected= false;
  public hasFilter = false;
  public previousRecSelectedDate: any;
  public previousDueSelectedDate: any;
  public busy: Subscription;

  constructor(private workflowService: WorkflowService, private router: Router) {
    this.pageUrl = router.url;
    this.currentPage = (this.pageUrl.slice(this.pageUrl.indexOf('workflow/') + 9)).split('/');
  }

  ngOnInit() {
    this.status = [{label: '', value: ''}, {label: 'New', value: 'New'}, {
      label: 'Read',
      value: 'Read'
    }, {label: 'Forward', value: 'Forward'}];

    this.type = [{label: '', value: ''}, {label: 'To', value: 'TO'}, {label: 'CC', value: 'CC'}, {
      label: 'Reply',
      value: 'REPLY'
    }];
    if (this.activePage === 'sent') {
      this.type.pop();
      this.type.push({label: 'Reply-To', value: 'Reply-TO'});
      this.type.push({label: 'Reply-CC', value: 'Reply-CC'});
    }
    this.priority = [{label: '', value: 0}, {label: 'Low', value: 1}, {label: 'Normal', value: 2}, {
      label: 'High',
      value: 3
    }];

    this.maxDate = new Date();
    if (this.searchFromDashboard) {
      this.filterFromDashBoard(this.searchFromDashboard);
    }
  }

  ngOnChanges() {
    setTimeout(() => {
      if (this.tieredClearFilter) {
        this.clearFilter(null);
      }
    }, 0)
  }

  senderSelectionChanged(event) {

  }

  filterFromDashBoard(searchQueryFromDashboard) {
    if (searchQueryFromDashboard.searchInboxStatus === 'Read') {
      this.filterQuery.status = 'Read';
    } else if (searchQueryFromDashboard.searchInboxStatus === 'Unread') {
      this.filterQuery.status = 'New';
    }
    if (searchQueryFromDashboard.searchInboxReceivedDay === 'Today') {
      this.filterQuery.receivedDate = this.maxDate.getDate() + '/' + (this.maxDate.getMonth() + 1) + '/' + this.maxDate.getFullYear();
    } else if (searchQueryFromDashboard.searchInboxReceivedDay === 'Total') {
      this.filterQuery.receivedDate = '';
    }
    if (this.searchFromDashboard.searchInboxWIType) {
      this.filterQuery.type = this.searchFromDashboard.searchInboxWIType;
    }
    // this.id = searchQueryFromDashboard.searchInboxId;
    // this.userType = searchQueryFromDashboard.searchInboxUserType;
    if (this.id === searchQueryFromDashboard.searchInboxId) {
      this.filter(null);
    }
  }

  clearFilter(event) {
    this.senderName = [];
    this.selectedReceivedDate = null;
    this.selectedDeadline = null;
    this.hasFilterResults = false;
    this.hasFilterResult.emit({'bool': false, 'id': this.id});
    this.filterQuery = {
      'priority': 0,
      'subject': '',
      'workitemId': 0,
      'status': '',
      'instructions': '',
      'type': '',
      'deadline': '',
      'receivedDate': '',
      'senderName': '',
      'recipientName': '',
      'comments': '',
      'userId': '',
      'userType': '',
      'repStatus': ''
    };
    this.filterQuery.userId = this.id;
    this.filterQuery.userType = this.userType;
    if (this.currentPage[0] === 'archive') {
      this.filterQuery.repStatus = 'archive';
      if (this.activePage === 'inbox') {
        this.filterQuery.recipientName = this.userType + ':' + this.id;
      } else if (this.activePage === 'sent') {
        this.filterQuery.senderName = this.userType + ':' + this.id;
      }
    } else if (this.currentPage[0] === 'inbox') {
      this.filterQuery.repStatus = 'active';
      this.filterQuery.recipientName = this.userType + ':' + this.id;
    } else if (this.currentPage[0] === 'sent') {
      this.filterQuery.repStatus = 'active';
      this.filterQuery.senderName = this.userType + ':' + this.id;
    }
    if (this.currentPage[0] === 'inbox' || this.currentPage[0] === 'archive') {
      if (this.activePage === 'inbox') {
        this.subscription.push(this.workflowService.searchInbox(this.filterQuery).subscribe(data => {
          this.sendData.emit(data);
        }));
      } else if (this.activePage === 'sent') {
        this.subscription.push(this.workflowService.searchSentUser(this.filterQuery).subscribe(data => {
          this.sendData.emit(data);
        }));
      }
    } else if (this.currentPage[0] === 'sent') {
      this.subscription.push(this.workflowService.searchSentUser(this.filterQuery).subscribe(data => {
        this.sendData.emit(data);
      }));
    }
    this.hasFilter = false;
    this.previousRecSelectedDate = undefined;
    this.previousDueSelectedDate = undefined;
  }

  receivedDateChanged(event) {
    const previousSelected = new Date(event);
    if (this.filterQuery.receivedDate !== '' && event >= this.previousRecSelectedDate && this.fromDateSelected) {
      let date = new Date(event);
      this.filterQuery.receivedDate = this.filterQuery.receivedDate + ';' + date.getDate() + '/' + (date.getMonth() + 1) + '/' + date.getFullYear();
      this.fromDateSelected = false;
    } else {
      let fromDate = new Date(event);
      this.fromDateSelected = true;
      this.previousRecSelectedDate = event;
      this.filterQuery.receivedDate = fromDate.getDate() + '/' + (fromDate.getMonth() + 1) + '/' + fromDate.getFullYear();
    }
  }

  dueDateDateChanged(event) {
    if (this.filterQuery.deadline !== '' && event >= this.previousDueSelectedDate && this.fromDeadlineDateSelected) {
      let date = new Date(event);
      this.filterQuery.deadline = this.filterQuery.deadline + ';' + date.getDate() + '/' + (date.getMonth() + 1) + '/' + date.getFullYear();
      this.fromDeadlineDateSelected = false;
    } else {
      let fromDate = new Date(event);
      this.fromDeadlineDateSelected = true;
      this.previousDueSelectedDate = event;
      this.filterQuery.deadline = fromDate.getDate() + '/' + (fromDate.getMonth() + 1) + '/' + fromDate.getFullYear();
    }
  }

  senderChange(event) {
    if (event.value.length) {
      let roles = 'ROLE:';
      let users = 'USER:';
      let roleCount = 0;
      let userCount = 0;
      for (const role of event.value) {
        let r = role.split(':');
        if (r[0] === 'ROLE') {
          roleCount++;
          roles = roles + r[1] + ';';
        }
      }
      for (const user of event.value) {
        let u = user.split(':');
        if (u[0] === 'USER') {
          userCount++;
          users = users + u[1] + ';';
        }
      }
      roles = roles.slice(0, -1);
      users = users.slice(0, -1);
      if (this.activePage === 'inbox') {
        if (roleCount > 0 && userCount > 0) {
          this.filterQuery.senderName = roles + '@' + users;
        } else if (roleCount > 0) {
          this.filterQuery.senderName = roles;
        } else if (userCount > 0) {
          this.filterQuery.senderName = users;
        }
      } else if (this.activePage === 'sent') {
        if (roleCount > 0 && userCount > 0) {
          this.filterQuery.recipientName = roles + '@' + users;
        } else if (roleCount > 0) {
          this.filterQuery.recipientName = roles;
        } else if (userCount > 0) {
          this.filterQuery.recipientName = users;
        }
      }
    }

  }

  filter(event) {
    this.filterData.emit()
  }

  assignFilterData(data) {
    this.filterCount = this.countFiltered(data);
    if (data.totalCount > 0 || data.setCount > 0) {
      this.hasFilterResults = true;
    }
    this.sendData.emit(data);
    this.hasFilterResult.emit({'bool': true, 'id': this.id});
  }

  countFiltered(data: any) {
    const filterCountOb = {
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
    for (const item of data.workitems) {
      filterCountOb.total++;
      if (item.status.includes('Read')) {
        filterCountOb.read++;
      } else if (item.status.includes('New')) {
        filterCountOb.new++;
      } else if (item.status.includes('Forward')) {
        filterCountOb.forwarded++;
      }
      if (item.type === 'TO') {
        filterCountOb.to++;
      } else if (item.type === 'CC') {
        filterCountOb.cc++;
      } else if (item.type === 'Reply') {
        filterCountOb.reply++;
      } else if (item.type === 'Reply-TO') {
        filterCountOb.replyto++;
      } else if (item.type === 'Reply-CC') {
        filterCountOb.replycc++;
      }
    }
    return filterCountOb;
  }

  exportToExcel() {
    if (this.activePage === 'inbox') {
      this.subscription.push(this.workflowService.exportInbox(this.filterQuery).subscribe(res => {
        const file = new Blob([res], {type: 'application/vnd.ms-excel'});
        const fileName = 'Inbox Report' + '.xlsx';
        saveAs(file, fileName);
      }));
    } else if (this.activePage === 'sent') {
      this.subscription.push(this.workflowService.exportSent(this.filterQuery).subscribe(res => {
        const file = new Blob([res], {type: 'application/vnd.ms-excel'});
        const fileName = 'Sent Report' + '.xlsx';
        saveAs(file, fileName);
      }));
    }
  }

  resetReceivedDatePicker(event) {
    this.filterQuery.receivedDate = '';
    this.previousRecSelectedDate = undefined;
  }

  resetDueDatePicker(event) {
    this.filterQuery.deadline = '';
    this.previousDueSelectedDate = undefined;
  }

  collapse(event) {
    this.collapseState.emit(true);
  }

  addToSubscriptions(subscription) {
    this.subscription.push(subscription);
  }

  ngOnDestroy() {
    for (let subs of this.subscription) {
      subs.unsubscribe();
    }
    this.subscription = null;
    this.filterResultWorkitems = [];
    this.status = [];
    this.type = [];
    this.priority = [];
    this.senderName = [];
    this.selectedReceivedDate = null;
    this.selectedDeadline = null;
    this.fromDateSelected = false;
    this.fromDeadlineDateSelected = false;
    this.previousRecSelectedDate = undefined;
    this.previousDueSelectedDate = undefined;
  }
}
