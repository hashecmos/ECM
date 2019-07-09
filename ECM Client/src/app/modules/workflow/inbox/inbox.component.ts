import {Component, OnInit, OnDestroy, ViewChild, ViewChildren, QueryList} from '@angular/core';
import {BreadcrumbService} from "../../../services/breadcrumb.service";
import {WorkflowService} from '../../../services/workflow.service';
import {UserService} from '../../../services/user.service';
import {Subscription} from 'rxjs/Rx';
import * as $ from 'jquery';
import {WorkitemSet} from '../../../models/workflow/workitem-set.model';
import {User} from '../../../models/user/user.model';
import {DataTable, SelectItem} from 'primeng/primeng';
import {ConfirmationService} from 'primeng/primeng';
import {FormControl} from '@angular/forms';
import {Router} from '@angular/router';
import {BrowserEvents} from '../../../services/browser-events.service';
import * as global from '../../../global.variables';
import {CoreService} from '../../../services/core.service';
import {saveAs} from 'file-saver';
import {GrowlService} from "../../../services/growl.service";
import {FilterComponent} from "../../../components/generic-components/filter/filter.component";

@Component({
  templateUrl: './inbox.component.html'
})
export class InboxComponent implements OnInit, OnDestroy {
  public selectedItem: any;
  public colHeaders: any[] = [];
  public itemsPerPage: any;
  public totalRecords: any;
  public inboxWorkitems: WorkitemSet = {};
  public columns: any[];
  public curPage: any = 1;
  public selectedColumns: string[] = [];
  public user = new User();
  public actions: string[] = [];
  defaultSelected = new FormControl();
  advanceFilterShown = false;
  action = new FormControl();
  public selectedAction: any;
  emptyMessage: any;
  public disableAction = true;
  private subscription: Subscription[] = [];
  lazy: boolean;
  selectedUser: any;
  displayProgress = false;
  type: any;
  public selectedCount = 0;
  public selectedTabIndex = 0;
  public previousSelectedTab: any;
  username: any;
  beforeDate: Date;
  overlayPanel: any;
  filterQuery = {
    'receivedDate': '',
    'recipientName': '',
    'userId': '',
    'userType': '',
    'repStatus': '',
    'receiveCount': 0,
    'exportFormat':'',
    'exportFilter':false
  };
  filterCount = {
    total: 0,
    pageSize: 0,
    to: 0,
    cc: 0,
    reply: 0,
    new: 0,
    read: 0,
    forwarded: 0,
    overdue: 0,
    newToday: 0
  };
  request: any = {pageNo: 1};
  public dashboardSearchQuery: any[] = [];
  public sender: SelectItem[] = [];
  public dashboardFilter = false;
  public hasFilterResults: any[] = [];
  public inboxTieredItems: any[] = [];
  public busy: Subscription;
  private subscriptions: any[] = [];
  public usersTabTotalCountBadge: any[] = [];
  public showDelegationInactiveDialog = false;
  @ViewChild('dt') dataTable: DataTable;
  @ViewChildren(FilterComponent) filterComponent: QueryList<FilterComponent>;
  private selectedWorkitem: any = {};
  public selectedWkitemId;
  public forOptions: any[];
  constructor(private breadcrumbService: BreadcrumbService, private ws: WorkflowService,
              private us: UserService, private bs: BrowserEvents, private coreService: CoreService,
              private confirmationService: ConfirmationService, private router: Router, private growlService: GrowlService,
              private workflowService: WorkflowService) {
    this.lazy = true;
    this.selectedUser = undefined;
    this.type = undefined;
    this.emptyMessage = global.no_workitem_found;
    this.user = this.us.getCurrentUser();
    this.breadcrumbService.setItems([
      {label: 'Workflow'},
      {label: 'Inbox'},
      {label: this.user.fulName}
    ]);
    this.inboxTieredItems.push({
      label: 'Call Date Report',
      icon: 'ui-icon-event-note',
      items : [{
            label: 'PDF',
            icon: 'ui-icon-description', command: (event) => {
              this.exportInbox('today','pdf');
            }
          },{
            label: 'Excel',
            icon: 'ui-icon-assignment', command: (event) => {
              this.exportInbox('today','excel');
            }
          }
      ],
      disabled: true,
      visible: false
    }, {
      label: 'Export All Workitems',
      icon: 'ui-icon-assignment-returned',
      disabled: true,
      items : [{
            label: 'PDF',
            icon: 'ui-icon-description', command: (event) => {
              this.exportInbox('all','pdf');
            }
          },{
            label: 'Excel',
            icon: 'ui-icon-assignment', command: (event) => {
              this.exportInbox('all','excel');
            }
          }
      ]
    });
  }


  ngOnInit() {
    this.username = global.username;
    this.workflowService.updateInboxCount();
    const subscription = this.us.logIn(this.username, 'def').subscribe(data => {
      localStorage.setItem('user', JSON.stringify(data));
    }, Error => {
      localStorage.removeItem('user');
      this.router.navigate(['/auth/auth-failure'])
    });
    this.coreService.progress = {busy: subscription, message: '', backdrop: true};
    this.addToSubscriptions(subscription);
    this.user = this.us.getCurrentUser();

    if (this.user) {
      const subscription2 = this.ws.getUserInbox(this.user.EmpNo, 1).subscribe(
        data => {
          this.usersTabTotalCountBadge[this.user.EmpNo] = data.totalCount;
        });

      this.coreService.progress = {busy: subscription2, message: '', backdrop: true};
      this.addToSubscriptions(subscription2);
      if (this.user.roles.length > 0) {
        this.user.roles.map((role, index) => {
          const subscription3 = this.ws.getRoleInbox(role.id, this.user.EmpNo, this.request.pageNo).subscribe(data => {
            this.usersTabTotalCountBadge[role.id] = data.totalCount;

          });
          this.coreService.progress = {busy: subscription3, message: '', backdrop: true};
          this.addToSubscriptions(subscription3);
        });
      }
      if (this.user.delegated.length > 0) {
        this.user.delegated.map((del, index) => {
          const subscription4 = this.ws.getUserInbox(del.userId, this.request.curPage).subscribe(data => {
            this.usersTabTotalCountBadge[del.userId] = data.totalCount;
          });
          this.coreService.progress = {busy: subscription4, message: '', backdrop: true};
          this.addToSubscriptions(subscription4);
        });
      }
    }
    if (this.breadcrumbService.dashboardFilterQuery && this.breadcrumbService.dashboardFilterQuery.filterStatus !== 'Actioned') {
      this.dashboardFilter = true;
      const id = this.breadcrumbService.dashboardFilterQuery.filterUserId;
      this.dashboardSearchQuery[id] = this.breadcrumbService.dashboardFilterQuery;
      this.tabChange(this.dashboardSearchQuery[id].filterUserName, this.dashboardSearchQuery[id].filterActiveTabIndex,false);
    } else {
      if (this.ws.inboxSelectedUserTab) {
        this.previousSelectedTab = this.ws.inboxSelectedUserTab.split('@');
        this.tabChange(this.previousSelectedTab[1], this.previousSelectedTab[0],false);
      } else {
        if (this.user.roles.length > 0) {
          this.tabChange(this.user.roles[0].name, 1,false);
        }
        else if (this.user) {
          this.tabChange(this.user.fulName, 0,false);
        }
      }
    }

    this.actions = ['Finish', 'Finish Before'];
    this.colHeaders = [{field: 'status', header: 'Status', hidden: true},
      {field: 'type', header: 'Type', hidden: true}, {field: 'instructions', header: 'Instructions', hidden: true},
      {field: 'receivedDate', header: 'Received Date', hidden: true, sortField: 'receivedDate2'},
      {field: 'senderName', header: 'Sender Name', hidden: true}, {field: 'actions', header: 'For', hidden: true},
      {field: 'recipientName', header: 'Recipient Name', hidden: true}, {
        field: 'wfCreatorName',
        header: 'Created By',
        hidden: true
      },
      {field: 'workitemId', header: 'workitemId', hidden: true}, {
        field: 'sentitemId',
        header: 'sentitemId',
        hidden: true
      },
      {field: 'deadline', header: 'Deadline', hidden: true}, {field: 'reminder', header: 'Reminder', hidden: true},
      {field: 'isNew', header: 'IsNew', hidden: true},
    ];
    this.columns = [{label: 'Status', value: 'status'}, {label: 'Type', value: 'type'},
      {label: 'Instructions', value: 'instructions'}, {label: 'Received Date', value: 'receivedDate'},
      {label: 'Sender Name', value: 'senderName'}, {label: 'For', value: 'actions'},
      {label: 'Recipient Name', value: 'recipientName'}, {label: 'Created By', value: 'wfCreatorName'},
      {label: 'Deadline', value: 'deadline'}, {label: 'Reminder', value: 'reminder'}
    ];
    this.selectedColumns = ['status', 'type', 'receivedDate', 'senderName'];
    this.defaultSelected.setValue(this.selectedColumns);
    for (const colunm of this.selectedColumns) {
      for (const tableHead of this.colHeaders) {
        if (tableHead.field === colunm) {
          tableHead.hidden = false;
        }

      }
    }
    this.bs.openedWkitem.subscribe(data=>this.assignDefaultSelected(data));
    this.getForOptions(this.user.EmpNo);
  }
  assignDefaultSelected(e){
    this.selectedWkitemId=e;

  }

  getWorkitemProgress() {
    const subscription = this.workflowService.getWorkitemProgress(this.selectedWorkitem.workitemId).subscribe(res => {
      res.map(r => {
        if (r.empNo === this.user.EmpNo) {
          r.from = true;
        }
      });
      this.selectedWorkitem.progress = res;
      this.displayProgress = true;

    });
    this.addToSubscriptions(subscription);
    this.coreService.progress = {busy: subscription, message: '', backdrop: true};


  }

  addWorkitemProgress(event) {
    const subscription = this.workflowService.addWorkitemProgress(event.message, this.user.EmpNo, this.selectedWorkitem.workitemId)
      .subscribe(res => {
        this.growlService.showGrowl({
          severity: 'info',
          summary: 'Success', detail: 'Workitem Progress Added Successfully'
        });
        event = {};
        this.getWorkitemProgress();
      }, err => {

      });
    this.addToSubscriptions(subscription);
    this.coreService.progress = {busy: subscription, message: '', backdrop: true};

  }

  removeWorkitemProgress(id) {
    this.confirmationService.confirm({
      message: 'Are you sure that you want to perform this action?',
      key: 'addToCartConfirmation',
      accept: () => {
        this.deleteWorkitemProgress2(id);
      }
    });

  }

  deleteWorkitemProgress2(id) {
    const subscription = this.workflowService.removeWorkitemProgress(id).subscribe(res => {
      this.growlService.showGrowl({
        severity: 'info',
        summary: 'Success', detail: 'Workitem Progress Removed Successfully'
      });
      this.getWorkitemProgress();

    }, err => {
    });
    this.addToSubscriptions(subscription);
    this.coreService.progress = {busy: subscription, message: '', backdrop: true};

  }


  getFilterSenderOptions(id, userType) {
    this.sender = [];
    const subscription = this.ws.getInboxFilterUsers(id, userType, 'active').subscribe(res => {
      for (const user of res) {
        this.sender.push({label: user.name, value: user.userType + ':' + user.id});
      }
    });
    this.coreService.progress = {busy: subscription, message: '', backdrop: true};
    this.addToSubscriptions(subscription);
  }

  assignSortNotPaginationInfo(data) {
    if (!data || !data.rows) {
      return;
    }
    if (data.globalFilter.length > 0) {
      const newData = [];
      this.inboxWorkitems.workitems.map(item => {
        if ((item.subject && item.subject.toLowerCase().indexOf(data.globalFilter.toLowerCase()) !== -1) ||
          (item.status && item.status.toLowerCase().indexOf(data.globalFilter.toLowerCase()) !== -1) ||
          (item.type && item.type.toLowerCase().indexOf(data.globalFilter.toLowerCase()) !== -1) ||
          (item.receivedDate && item.receivedDate.toLowerCase().indexOf(data.globalFilter.toLowerCase()) !== -1) ||
          (item.wfCreatorName && item.wfCreatorName.toLowerCase().indexOf(data.globalFilter.toLowerCase()) !== -1) ||
          (item.actions && item.actions.toLowerCase().indexOf(data.globalFilter.toLowerCase()) !== -1)) {
          newData.push(item);
        }

      });
      this.inboxWorkitems.workitems = newData;
      return;
    }
    this.request.pageNo = Math.ceil(data.first / data.rows) + 1;
    this.request.sort = data.sortField;
    this.request.empNo = this.user.EmpNo;
    if (data.sortField === 'receivedDate2') {
      this.request.sort = 'createdDate';
    }
    if (data.sortOrder === 1) {
      this.request.order = 'ASC';
    }
    else {
      this.request.order = 'DESC';
    }

    this.searchInbox();

  }

  searchInbox() {
    this.request.repStatus = 'active';
    const subscription = this.ws.searchInbox(this.request).subscribe(res => {
      res.workitems.map((d) => {
          d.receivedDate2 = this.coreService.convertToTimeInbox(d.receivedDate);
          d.progress = 'progress';
          const oneDay = 24 * 60 * 60 * 1000;
          const tod = new Date();
          const arrDate = d.receivedDate.split('/');
          const rec = new Date(arrDate[1] + '/' + arrDate[0] + '/' + arrDate[2]);
          const diffDays = Math.round(Math.abs((tod.getTime() - rec.getTime()) / (oneDay)));
          d.daysleft = diffDays;
          //  if(rec>tod){
          //   d.deadlineExceed=false;
          // }
          // else if(rec<tod){
          //   d.deadlineExceed=true;
          // }
          const today = new Date();
          today.setHours(0, 0, 0, 0);
          if (d.status.includes('New') && d.receivedDate2 >= today.getTime()) {
            d.isNew = true;
          }
        }
      );
      this.itemsPerPage = res.pageSize;
      this.totalRecords = res.totalCount;
      //this.usersTabTotalCountBadge[this.request.userId] = res.totalCount;
      this.inboxWorkitems = res;
      this.defaultFilter(this.request.userType, this.request.userId);
      if (this.advanceFilterShown) {
        this.countFiltered(res);
      } else {
        this.filterCount.total = -1;
      }
      this.inboxTieredItems.map((item, index) => {
        item.disabled = !this.inboxWorkitems.workitems || this.inboxWorkitems.workitems.length === 0;
      });
    });
    this.coreService.progress = {busy: subscription, message: '', backdrop: true};
    this.addToSubscriptions(subscription);

  }

  countFiltered(data: any) {
    this.filterCount = {
      total: data.totalCount,
      pageSize: data.setCount,
      to: 0,
      cc: 0,
      reply: 0,
      new: 0,
      read: 0,
      forwarded: 0,
      overdue: 0,
      newToday: 0
    };
    for (const item of data.workitems) {
      if (item.status.includes('Read')) {
        this.filterCount.read++;
      } if (item.status.includes('New')) {
        this.filterCount.new++;
      } if (item.actionId === 1) {
        this.filterCount.forwarded++;
      } if (item.type.includes( 'TO')) {
        this.filterCount.to++;
      } if (item.type.includes( 'CC')) {
        this.filterCount.cc++;
      } if (item.actionId === 2) {
        this.filterCount.reply++;
      }
      const today = new Date();
      today.setHours(0, 0, 0, 0);
      if (item.deadline) {
        const deadline = this.coreService.convertToTimeInbox(item.deadline);
        if (item.deadline) {
          if (deadline <= today.getTime()) {
            this.filterCount.overdue++;
          }
        }
      }
      if (item.receivedDate) {
        const receivedDate = this.coreService.convertToTimeInbox(item.receivedDate);
        if (item.receivedDate) {
          if (receivedDate >= today.getTime() && item.status.includes('New')) {
            this.filterCount.newToday++;
          }
        }
      }
    }
  }

  getData(data: any) {
    this.selectedItem = data;
    if (this.selectedItem) {
      if (this.selectedItem.length > 0) {
        this.disableAction = false;
        this.selectedCount = this.selectedItem.length;
      } else {
        this.disableAction = true;
        this.selectedCount = 0;
      }
    }
  }

  hasFilterResultBool(event) {
    this.hasFilterResults[event.id] = event.bool;
  }

  clearFilterResults(event) {
    this.clearFilter();
  }

  closeFilters(event) {
    this.advanceFilterShown = false;
    $('.filter').slideUp();
    this.clearFilter();
  }

  clearFilter() {
    this.breadcrumbService.dashboardFilterQuery = undefined;
    this.dashboardSearchQuery = [];
    this.dashboardFilter = false;
    this.resetFilterModel();
    this.searchInbox();
  }

  resetFilterModel() {
    if (this.filterComponent) {
      this.filterComponent.map(r => {
        r.resetFilter();
      });
    }
  }

  getFiltertoggle(data: any) {
    this.advanceFilterShown = !this.advanceFilterShown;
    this.toggleFilter();
  }

  getFilteredData(data: any) {
    data.workitems.map(d => d.receivedDate2 = this.coreService.convertToTimeInbox(d.receivedDate));
    this.inboxWorkitems = data;
    this.itemsPerPage = data.pageSize;
    this.totalRecords = data.totalCount;
    this.inboxTieredItems.map((item, index) => {
      item.disabled = !this.inboxWorkitems.workitems || this.inboxWorkitems.workitems.length === 0;
    });
  }

  getSelectedAction(data: any, op) {
    if (data === 'Finish') {
      this.finishWorkitems();
    }
    else if (data === 'Finish Before') {
      this.openOverlayPanel(op);
    }
  }

  assignInboxItems(data: any) {
    data.workitems.map(d => d.receivedDate2 = this.coreService.convertToTimeInbox(d.receivedDate));
    this.inboxWorkitems = data;
    this.itemsPerPage = data.pageSize;
    this.totalRecords = data.totalCount;
    this.inboxTieredItems.map((item, index) => {
      item.disabled = !this.inboxWorkitems.workitems || this.inboxWorkitems.workitems.length === 0;
    });
  }

  assignInboxItems2(data: any) {
    data.workitems.map(d => d.receivedDate2 = this.coreService.convertToTimeInbox(d.receivedDate));
    data.workitems.splice(0, 5);
    this.inboxWorkitems = data;

    this.itemsPerPage = data.pageSize;
    this.totalRecords = data.totalCount;
    console.log("total workitems " + this.inboxWorkitems.workitems.length);
    this.inboxTieredItems.map((item, index) => {
      item.disabled = !this.inboxWorkitems.workitems || this.inboxWorkitems.workitems.length === 0;
    });
  }

  columnSelectionChanged(event: Event) {
    for (const tableHead of this.colHeaders) {
      tableHead.hidden = true;
    }
    for (const colunm of this.selectedColumns) {
      for (const tableHead of this.colHeaders) {
        if (tableHead.field === colunm) {
          tableHead.hidden = false;
        }
      }
    }
  }

  actionSelectionChanged(event) {

  }

  tabChange(textLabel, index, reset) {
    $('.filter').slideUp();
    if (this.dashboardFilter) {
      this.advanceFilterShown = true;
    } else {
      this.advanceFilterShown = false;
    }
    this.resetFilterModel();
    this.selectedTabIndex = index;
    this.ws.inboxSelectedUserTab = this.selectedTabIndex + '@' + textLabel;
    this.selectedItem = [];
    this.disableAction = true;
    this.breadcrumbService.setItems([
      {label: 'Workflow'},
      {label: 'Inbox'},
      {label: textLabel}
    ]);

    for (const role of this.user.roles) {
      if (role.name === textLabel) {
        this.request.userType = 'ROLE';
        this.request.userId = role.id;
        this.request.empNo = this.user.EmpNo;
        this.request.recipientName = undefined;
        this.workflowService.delegateId = undefined;
      }
    }
    for (const delegate of this.user.delegated) {
      if (delegate.delName === textLabel) {
        this.request.userType = 'USER';
        this.request.userId = delegate.userId;
        this.request.recipientName = 'USER:' + delegate.userId;
        this.request.empNo = this.user.EmpNo;

        this.us.validateDelegation(delegate.id).subscribe(res=>{
          if(res==='INACTIVE'){
            this.showDelegationInactiveDialog = true;
          }
        });
        this.workflowService.delegateId = delegate.id;
      }
    }
    if (this.user.fulName === textLabel) {
      this.request.userType = 'USER';
      this.request.userId = this.user.EmpNo;
      this.request.recipientName = 'USER:' + this.user.EmpNo;
      this.request.empNo = this.user.EmpNo;
      this.workflowService.delegateId = undefined;
    }
    if(!this.dashboardFilter){
      if(!reset && this.previousSelectedTab && this.ws.pageNoSelected > 0 && this.ws.pageNoSelected !== undefined){
        this.request.pageNo = this.ws.pageNoSelected;
      } else {
        this.ws.pageNoSelected = 0;

      }
      this.searchInbox();
    }
    this.dashboardFilter = false;
    this.getFilterSenderOptions(this.request.userId, this.request.userType)
  }

  toggleFilter() {
    $('.filter').slideToggle();
  }

  finishWorkitems() {
    this.confirmationService.confirm({
      message: 'Do you want to Finish this workitem?',
      header: 'Finish Confirmation',
      icon: 'ui-icon-help',
      accept: () => {
        if (this.selectedItem.length > 0) {
          let count = 0;
          this.selectedItem.map((item, index) => {
            const subscription = this.ws.finishWorkitem(item.workitemId)
              .subscribe(data => {
                count++;
                if (this.selectedItem.length === count) {
                  this.selectedItem = [];
                  this.finishSuccess();
                }
              }, Error => this.finishFailed());
            this.coreService.progress = {busy: subscription, message: '', backdrop: true};
            this.addToSubscriptions(subscription);
          });
        }
      },
      reject: () => {
      }
    });
  }

  defaultFilter(type, id) {
    this.filterQuery.recipientName = type + ':' + id;
    this.filterQuery.userId = id;
    this.filterQuery.userType = type;
    this.filterQuery.repStatus = 'active';
  }

  exportInbox(type, exportType) {
    if (type === 'today') {
      const today = new Date();
      this.filterQuery.receivedDate = today.getDate() + '/' + (today.getMonth() + 1) + '/' + today.getFullYear();
    }
    if(exportType === 'pdf'){
      this.filterQuery.exportFormat="pdf";
      const subscription = this.ws.exportInbox(this.filterQuery).subscribe(res => {
        const file = new Blob([res], {type: 'application/pdf'});
        const fileName = 'Inbox' + '.pdf';
        saveAs(file, fileName);
        this.filterQuery.receivedDate = '';
      });
      this.coreService.progress = {busy: subscription, message: '', backdrop: true};
      this.addToSubscriptions(subscription);
    } else {
      this.filterQuery.exportFormat="xls";
      const subscription = this.ws.exportInbox(this.filterQuery).subscribe(res => {
        const file = new Blob([res], {type: 'application/vnd.ms-excel'});
        const fileName = 'Inbox' + '.xlsx';
        saveAs(file, fileName);
        this.filterQuery.receivedDate = '';
      });
      this.coreService.progress = {busy: subscription, message: '', backdrop: true};
      this.addToSubscriptions(subscription);
    }
  }

  refreshTable(event) {
    this.workflowService.updateInboxCount();
    if (this.ws.inboxSelectedUserTab) {
      this.previousSelectedTab = this.ws.inboxSelectedUserTab.split('@');
      // this.selectedTabIndex = this.previousSelectedTab[0];
      this.tabChange(this.previousSelectedTab[1], this.previousSelectedTab[0],false);
    }
  }

  openOverlayPanel(op) {
    this.overlayPanel = op;
    if (op.visible) {
      op.visible = false;
    }
    else {
      op.visible = true;
    }

  }

  selectBeforeDate(event) {
    let empNo = 0;
    let roleId = 0;
    if (this.request.userType === 'USER') {
      empNo = this.request.userId;
      roleId = 0;
    }
    else if (this.request.userType === 'ROLE') {
      empNo = 0;
      roleId = this.request.userId;
    }
    const bDate = this.coreService.formatDateForFinishBefore(event);
    this.confirmationService.confirm({
      header: 'Finish?',

      message: 'All Workitems received before' + ' ' + bDate + ' ' + 'will be shifted to archived items,are you sure?',
      accept: () => {
        //Actual logic to perform a confirmation
        this.ws.finishWorkitemBefore(empNo, roleId, bDate).subscribe(val => this.finishBeforeSuccess(val), error => this.finishFailed());

      }

    });
    this.overlayPanel.visible = false;
  }

  finishBeforeSuccess(val) {
    if (val === 'Workitems not found') {
      this.growlService.showGrowl({
        severity: 'error',
        summary: 'No Workitems', detail: 'No workitems found..Choose a different date'
      });
    }
    else {
      const count = this.getArchiveCount(val.trim());
      let message;
      if (count === '1') {
        message = count + ' ' + 'Workitem Finished';
      }
      else {
        message = count + ' ' + 'Workitems Finished';
      }
      this.growlService.showGrowl({
        severity: 'info',
        summary: 'Success', detail: message
      });
      this.redirectToArchive();
    }
  }

  redirectToArchive() {
    const selectedTab = this.ws.inboxSelectedUserTab.split('@');
    const tabIndex = parseInt(selectedTab[0], 10);
    this.ws.archiveSelectedUserTab = tabIndex * 2 + '@' + selectedTab[1] + 'Inbox';
    this.router.navigateByUrl('workflow/archive');
  }

  finishSuccess() {
    this.growlService.showGrowl({
      severity: 'info',
      summary: 'Success', detail: 'Finished Successfully'
    });
    this.redirectToArchive();
  }

  finishFailed() {
    this.growlService.showGrowl({
      severity: 'error',
      summary: 'Failure', detail: 'Failed To Finish Workitems'
    });
  }

  getArchiveCount(str) {
    return str.split('-')[1];
  }

  showProgressDialogue(event) {
    this.selectedWorkitem = {};
    this.selectedWorkitem.workitemId = event;
    this.getWorkitemProgress();

  }

  hideDisplayProgress() {
    this.displayProgress = false;
  }

  addToSubscriptions(subscription) {
    this.subscriptions.push(subscription);
  }

  clearSubscriptions() {
    this.subscriptions.map(s => {
      s.unsubscribe();
    });
  }

  reloadApp(){
    this.showDelegationInactiveDialog = false;
    window.location.reload(true);
  }

  getForOptions(EmpNo){
    this.forOptions = [];
     const subscription = this.ws.getActions(EmpNo)
        .subscribe(res => {
          res.map((option)=>{
            this.forOptions.push({label: option.name, value:option.name})
          })
        });
     this.coreService.progress = {busy: subscription, message: '', backdrop: true};
     this.addToSubscriptions(subscription);
  }

  ngOnDestroy() {
    //this.resetFilterModel();
    this.clearSubscriptions();
    //localStorage.removeItem('openWkItemId');
    this.selectedItem = [];
    this.colHeaders = [];
    this.itemsPerPage = undefined;
    this.totalRecords = undefined;
    this.inboxWorkitems = {};
    this.columns = [];
    this.curPage = undefined;
    this.selectedColumns = [];
    this.user = undefined;
    this.actions = [];
    this.defaultSelected = undefined;
    this.action = undefined;
    this.selectedAction = undefined;
    this.emptyMessage = undefined;
    this.disableAction = true;
    this.subscription = [];

    this.selectedUser = undefined;
    this.type = undefined;
    this.selectedCount = 0;
    this.selectedTabIndex = 0;
    this.breadcrumbService.dashboardFilterQuery = undefined;
    this.dashboardSearchQuery = [];
    this.dashboardFilter = false;
    this.hasFilterResults = [];
    this.usersTabTotalCountBadge = [];
    if (this.busy) {
      this.busy.unsubscribe();
    }
  }
}
