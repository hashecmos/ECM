import {
  Component, OnInit, Input, Output, EventEmitter, OnDestroy, ViewChild, ViewChildren,
  QueryList
} from '@angular/core';
import {BreadcrumbService} from "../../../services/breadcrumb.service";
// services
import {WorkflowService} from '../../../services/workflow.service';
import {UserService} from '../../../services/user.service';
// models
import {WorkitemSet} from '../../../models/workflow/workitem-set.model';
import {User} from '../../../models/user/user.model';
// libraries
import {Subscription} from 'rxjs/Rx';
import * as $ from 'jquery';
import {MultiSelectModule} from 'primeng/primeng';
import {SelectItem, Message} from 'primeng/primeng';
import {ConfirmDialogModule, ConfirmationService} from 'primeng/primeng';
import {Router} from '@angular/router';
import {BrowserEvents} from '../../../services/browser-events.service';
import * as global from '../../../global.variables';
import {CoreService} from '../../../services/core.service';
import {saveAs} from 'file-saver';
import {GrowlService} from "../../../services/growl.service";
import {FilterComponent} from "../../../components/generic-components/filter/filter.component";

@Component({
  templateUrl: './sent.component.html',
  providers: [ConfirmationService]
})
export class SentComponent implements OnInit, OnDestroy {
  public user = new User();
  emptyMessage: any;
  public selectedItem: any[] = [];
  public colHeaders: any[] = [];
  public itemsPerPage: any;
  public totalRecords: any;
  public sentWorkitems: WorkitemSet = {};
  public columns: any[] = [];
  public selectedColumns: string[] = [];
  public actions: string[] = [];
  public disableAction = true;
  public selectedCount = 0;
  public curPage: any = 1;
  selectedUser: any;
  type: any;
  lazy: boolean;
  beforeDate: Date;
  overlayPanel: any;
  public selectedTabIndex = 0;
  public previousSelectedTab: any;
  filterCount = {total: -1, pageSize: 0, to: 0, cc: 0, reply: 0, replyto: 0, replycc: 0, new: 0, read: 0, forwarded: 0};
  public filterQuery = {
    'receivedDate': '',
    'senderName': '',
    'userId': '',
    'userType': '',
    'repStatus': '',
    'exportFormat':'',
    'exportFilter':false
  };
  request: any = {pageNo: 1};
  public userTabsTotalCount = 0;
  public roleTabsTotalCount: any[] = [];
  public delTabsTotalCount: any[] = [];
  public sender: SelectItem[] = [];
  public hasFilterResults: any[] = [];
  public clearFilterBool: any[] = [];
  public sentTieredItems: any[] = [];
  @ViewChildren(FilterComponent) filterComponent: QueryList<FilterComponent>;
  public usersTabTotalCountBadge: any[] = [];
  public recalled = false;
  private subscription: Subscription[] = [];
  private subscriptions: any[] = [];
  private advanceFilterShown = false;
  private selectedWorkitem: any = {};
  private displayProgress = false;
  private dashboardFilter = false;
  public showDelegationInactiveDialog = false;
  public dashboardSearchQuery: any[] = [];
  public forOptions: any[];
  constructor(private breadcrumbService: BreadcrumbService, private ws: WorkflowService,
              private us: UserService, private bs: BrowserEvents,
              private router: Router, private confirmationService: ConfirmationService, private growlService: GrowlService,
              private coreService: CoreService, private workflowService: WorkflowService) {
    this.user = this.us.getCurrentUser();
    this.selectedUser = undefined;
    this.type = undefined;
    this.lazy = true;
    this.emptyMessage = global.no_workitem_found;
    this.breadcrumbService.setItems([
      {label: 'Workflow'},
      {label: 'Sent'},
      {label: this.user.fulName}
    ]);
    this.sentTieredItems.push({
      label: 'Call Date Report',
      icon: 'ui-icon-event-note',
      items : [{
            label: 'PDF',
            icon: 'ui-icon-description', command: (event) => {
              this.exportSent('today','pdf');
            }
          },{
            label: 'Excel',
            icon: 'ui-icon-assignment', command: (event) => {
              this.exportSent('today','excel');
            }
          }
      ],
      disabled: true,
      visible:false
    }, {
      label: 'Export All Workitems',
      icon: 'ui-icon-assignment-returned',
      disabled: true,
      items : [{
            label: 'PDF',
            icon: 'ui-icon-description', command: (event) => {
              this.exportSent('all','pdf');
            }
          },{
            label: 'Excel',
            icon: 'ui-icon-assignment', command: (event) => {
              this.exportSent('all','excel');
            }
          }
      ]
    });
  }

  ngOnInit() {
    if (this.user) {
      this.subscription.push(this.ws.getUserSentItems(this.user.EmpNo, this.request.pageNo).subscribe(
        data => {
          this.usersTabTotalCountBadge[this.user.EmpNo] = data.totalCount;
        }));
      if (this.user.roles.length > 0) {
        this.user.roles.map((role, index) => {
          this.subscription.push(this.ws.getRoleSentItems(role.id, this.user.EmpNo, this.request.pageNo).subscribe(
            data => {
              this.usersTabTotalCountBadge[role.id] = data.totalCount;
            }));
        });
      }
      if (this.user.delegated.length > 0) {
        this.user.delegated.map((del, index) => {
          this.subscription.push(this.ws.getUserSentItems(del.userId, this.request.pageNo).subscribe(
            data => {
              this.usersTabTotalCountBadge[del.userId] = data.totalCount;
            }));
        });
      }
    }
    if (this.breadcrumbService.dashboardFilterQuery && this.breadcrumbService.dashboardFilterQuery.filterStatus !== 'Actioned') {
      this.dashboardFilter = true;
      const id = this.breadcrumbService.dashboardFilterQuery.filterUserId;
      this.dashboardSearchQuery[id] = this.breadcrumbService.dashboardFilterQuery;
      this.tabChange(this.dashboardSearchQuery[id].filterUserName, this.dashboardSearchQuery[id].filterActiveTabIndex,false);
    } else {
      if (this.ws.sentSelectedUserTab) {
        this.previousSelectedTab = this.ws.sentSelectedUserTab.split('@');
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
    this.actions = ['Recall', 'Archive', 'Archive Before'];
    this.colHeaders = [
      {field: 'subject', header: 'Subject', hidden: true},
      {field: 'receivedDate', header: 'Sent Date', hidden: true, sortField: 'receivedDate2'},
      {field: 'wfCreatorName', header: 'Created By', hidden: true}, {field: 'workitemId', header: 'workitemId', hidden: true},
      {field: 'sentitemId', header: 'sentitemId', hidden: true}
    ];
    this.columns = [{label: 'Subject', value: 'subject'}, {label: 'Sent Date', value: 'receivedDate'},
      {label: 'Created By', value: 'wfCreatorName'}
    ];
    this.selectedColumns = ['subject', 'receivedDate', 'wfCreatorName'];
    for (const colunm of this.selectedColumns) {
      for (const tableHead of this.colHeaders) {
        if (tableHead.field === colunm) {
          tableHead.hidden = false;
        }
      }
    }
    this.getForOptions(this.user.EmpNo);
  }

  countFiltered(data: any) {
    this.filterCount = {total: data.totalCount , pageSize: data.setCount, to: 0, cc: 0, reply: 0, replyto: 0, replycc: 0, new: 0, read: 0, forwarded: 0};
    for (const item of data.workitems) {
      //this.filterCount.total++;
      // if (item.status.includes('Read')) {
      //   this.filterCount.read++;
      // } if (item.status.includes('New')) {
      //   this.filterCount.new++;
      // } if (item.actionId === 1) {
      //   this.filterCount.forwarded++;
      // } if (item.type.includes( 'TO') || item.type.includes( 'Reply-TO')) {
      //   this.filterCount.to++;
      // } if (item.type.includes( 'CC') || item.type.includes( 'Reply-TO')) {
      //   this.filterCount.cc++;
      // } if (item.actionId === 2  || item.type.includes( 'Reply')) {
      //   this.filterCount.reply++;
      // }
    }

  }

  clearFilterResults(event) {
    this.clearFilter();
  }

  closeFilters(event) {
    $('.filter').slideUp();
    this.advanceFilterShown = false;
    this.clearFilter();
  }

  clearFilter() {
    this.breadcrumbService.dashboardFilterQuery = undefined;
    this.resetFilterModel();
    this.getSentItems();
  }

  resetFilterModel() {
    if (this.filterComponent) {
      this.filterComponent.map(r => {
        r.resetFilter();
      });
      this.request.recipientName = undefined;
    }
  }

  getFilterSenderOptions(id, userType) {
    this.sender = [];
    const subscription = this.ws.getSentitemFilterUsers(id, userType, 'active').subscribe(res => {
      for (const user of res) {
        this.sender.push({label: user.name, value: user.userType + ':' + user.id});
      }
    });
    this.coreService.progress = {busy: subscription, message: '', backdrop: true};
    this.addToSubscriptions(subscription);
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

  clearFilters(event) {
    $('.filter').slideUp();
    this.clearFilterBool[event.id] = event.bool;
  }

  getFiltertoggle(data: any) {
    this.advanceFilterShown = !this.advanceFilterShown;
    this.toggleFilter();
  }

  getFilteredData(data: any) {
    data.workitems.map(d => d.receivedDate2 = this.coreService.convertToTimeInbox(d.receivedDate));
    this.sentWorkitems = data;
    this.itemsPerPage = data.pageSize;
    this.totalRecords = data.totalCount;
    this.sentTieredItems.map((item, index) => {
      item.disabled = !data.workitems || data.workitems.length === 0;
    });
  }

  getSelectedAction(data: any, op) {
    if (data === 'Archive') {
      this.archiveSentitems();
    } else if (data === 'Recall') {
      this.recallSentitems();
    }
    else if (data === 'Archive Before') {
      this.openOverlayPanel(op);
    }
  }

  assignSentItems(data: any) {
    data.workitems.map(d => d.receivedDate2 = this.coreService.convertToTimeInbox(d.receivedDate));
    this.sentWorkitems = data;
    this.itemsPerPage = data.pageSize;
    this.totalRecords = data.totalCount;
    this.sentTieredItems.map((item, index) => {
      item.disabled = !data.workitems || data.workitems.length === 0;
    });
  }

  hideDisplayProgress() {
    this.displayProgress = false;
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

  showProgressDialogue(event) {
    this.selectedWorkitem = {};
    this.selectedWorkitem.workitemId = event;
    this.getWorkitemProgress();

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

  assignSortNotPaginationInfo(data) {
    if (!data || !data.rows) {
      return;
    }
    if (data.globalFilter.length > 0) {
      const newData = [];
      this.sentWorkitems.workitems.map(item => {
        if (item.subject.toLowerCase().indexOf(data.globalFilter.toLowerCase()) !== -1) {
          newData.push(item);
        }
      });
      this.sentWorkitems.workitems = newData;
      return;
    }

    this.request.pageNo = Math.ceil(data.first / data.rows) + 1;
    this.request.sort = data.sortField;
    if (data.sortField === 'receivedDate2') {
      this.request.sort = 'createdDate';
    }
    else if (data.sortField === 'actions') {
      this.request.sort = 'type';
    }
    if (data.sortOrder === 1) {
      this.request.order = 'ASC';
    }
    else {
      this.request.order = 'DESC';
    }

    this.getSentItems();
  }

  getSentItems() {
    this.request.repStatus = 'active';
    this.request.empNo = this.user.EmpNo;
    const subscription = this.ws.searchSentUser(this.request).subscribe(res => {
      res.workitems.map(d => d.receivedDate2 = this.coreService.convertToTimeInbox(d.receivedDate));
      if (this.recalled) {
        this.usersTabTotalCountBadge[this.request.userId] = res.totalCount;
        this.recalled = false;
      }
      this.defaultFilter(this.request.userType, this.request.userId);
      this.itemsPerPage = res.pageSize;
      this.totalRecords = res.totalCount;
      this.sentWorkitems = res;
      if (this.advanceFilterShown) {
        this.countFiltered(res);
      } else {
        this.filterCount.total = -1;
      }
      this.sentTieredItems.map((item, index) => {
        item.disabled = !this.sentWorkitems.workitems || this.sentWorkitems.workitems.length === 0;
      });
    });
    this.coreService.progress = {busy: subscription, message: '', backdrop: true};
    this.addToSubscriptions(subscription);
  }

  tabChange(textLabel, index,reset) {
    $('.filter').slideUp();
    if (this.dashboardFilter) {
      this.advanceFilterShown = true;
    } else {
      this.advanceFilterShown = false;
    }
    this.resetFilterModel();
    this.selectedTabIndex = index;

    this.ws.sentSelectedUserTab = this.selectedTabIndex + '@' + textLabel;
    this.disableAction = true;
    this.selectedItem = [];
    this.clearFilterBool = [];
    this.breadcrumbService.setItems([
      {label: 'Workflow'},
      {label: 'Sent'},
      {label: textLabel}
    ]);
    this.getSentItemsForUsers(textLabel,reset);
    this.getFilterSenderOptions(this.request.userId, this.request.userType);
  }

  getSentItemsForUsers(userName,reset) {
    for (const role of this.user.roles) {
      if (role.name === userName) {
        this.request.userType = 'ROLE';
        this.request.userId = role.id;
        this.request.empNo = role.id;
        this.request.recipientName = undefined;
        this.workflowService.delegateId = undefined;
      }
    }
    for (const delegate of this.user.delegated) {
      if (delegate.delName === userName) {
        this.request.userType = 'USER';
        this.request.userId = delegate.userId;
        this.request.empNo = this.user.EmpNo;

        this.us.validateDelegation(delegate.id).subscribe(res=>{
          if(res==='INACTIVE'){
            this.showDelegationInactiveDialog = true;
          }
        });
        this.workflowService.delegateId = delegate.id;
      }
    }
    if (this.user.fulName === userName) {
      this.request.userType = 'USER';
      this.request.userId = this.user.EmpNo;
      this.request.empNo = this.user.EmpNo;
      this.workflowService.delegateId = undefined;
    }
    if(!this.dashboardFilter){
      if(!reset && this.previousSelectedTab && this.ws.pageNoSelected > 0 && this.ws.pageNoSelected !== undefined){
        this.request.pageNo = this.ws.pageNoSelected;
      } else {
        this.ws.pageNoSelected = 0;
      }
      this.getSentItems();
    }
    this.dashboardFilter = false;
  }

  toggleFilter() {
    this.clearFilterBool = [];
    $('.filter').slideToggle();
  }

  archiveSentitems() {
    this.confirmationService.confirm({
      message: 'Do you want to Archive this workitem?',
      header: 'Archive Confirmation',
      icon: 'ui-icon-help',
      accept: () => {
        if (this.selectedItem.length > 0) {
          let count = 0;
          this.selectedItem.map((item, index) => {
            this.subscription.push(this.ws.archiveSentitem(item.workitemId)
              .subscribe(data => {
                count++;
                if (this.selectedItem.length === count) {
                  this.selectedItem = [];
                  this.archiveSuccess();
                }
              }));
          });
        }
      },
      reject: () => {
      }
    });
  }

  recallSentitems() {
    this.confirmationService.confirm({
      message: 'Do you want to Recall this workitem?',
      header: 'Recall Confirmation',
      icon: 'ui-icon-help',
      accept: () => {
        this.recalled = true;
        if (this.selectedItem.length > 0) {
          let count = 0;
          this.selectedItem.map((item, index) => {
            this.subscription.push(this.ws.recallSentitem(item.workitemId)
              .subscribe(data => {
                count++;
                if (this.selectedItem.length === count) {
                  this.sentWorkitems = {};
                  this.growlService.showGrowl({
                    severity: 'info',
                    summary: 'Success', detail: 'Recalled Successfully'
                  });
                  setTimeout(() => {
                    this.getSentItemsForUsers(this.selectedItem[0].senderName,false);
                    this.selectedItem = [];
                  }, 200)
                }
              }));
          });
        }
      },
      reject: () => {
      }
    });
  }

  failed(error) {
    this.growlService.showGrowl({
      severity: 'error',
      summary: 'Failure', detail: 'Operation Failed'
    });
  }

  defaultFilter(type, id) {
    // this.filterQuery.senderName = type+':'+id;
    this.filterQuery.userId = id;
    this.filterQuery.userType = type;
    this.filterQuery.repStatus = 'active';
  }

  exportSent(type, exportType) {
    if (type === 'today') {
      const today = new Date();
      this.filterQuery.receivedDate = today.getDate() + '/' + (today.getMonth() + 1) + '/' + today.getFullYear();
    }
    if(exportType === 'pdf'){
      this.filterQuery.exportFormat = 'pdf';
      const subscription = this.ws.exportSent(this.filterQuery).subscribe(res => {
      const file = new Blob([res], {type: 'application/pdf'});
      const fileName = 'Sent' + '.pdf';
      saveAs(file, fileName);
      this.filterQuery.receivedDate = '';
     });
      this.coreService.progress={busy:subscription,message:''};
      this.addToSubscriptions(subscription);
    } else {
      this.filterQuery.exportFormat = 'xls';
      const subscription = this.ws.exportSent(this.filterQuery).subscribe(res => {
      const file = new Blob([res], {type: 'application/vnd.ms-excel'});
      const fileName = 'Sent' + '.xlsx';
      saveAs(file, fileName);
      this.filterQuery.receivedDate = '';
     });
      this.coreService.progress={busy:subscription,message:''};
      this.addToSubscriptions(subscription);
    }
  }

  refreshTable(event) {
    if (this.ws.sentSelectedUserTab) {
      this.previousSelectedTab = this.ws.sentSelectedUserTab.split('@');
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
      header: 'Archive?',
      message: 'All Workitems sent before' + ' ' + bDate + ' ' + 'will be shifted to archived items,are you sure?',
      accept: () => {
        //Actual logic to perform a confirmation
        this.ws.archiveSentitemBefore(empNo, roleId, bDate).subscribe(dat => {
          if (dat) {
            this.archiveBeforeSuccess(dat);
          }
          else {
            this.archiveFailed();
          }
        });
      }
    });
    this.overlayPanel.visible = false;
  }

  archiveBeforeSuccess(val) {
    if (val === 'Workitems not found') {
      this.growlService.showGrowl({
        severity: 'error',
        summary: 'No Workitems', detail: 'No workitems found..Choose a different date'
      });
    }
    else {
      const count = this.getArchiveCount(val).trim();
      let message;
      if (count === '1') {
        message = count + ' ' + 'Workitem Archived';
      }
      else {
        message = count + ' ' + 'Workitems Archived';
      }
      this.growlService.showGrowl({
        severity: 'info',
        summary: 'Success', detail: message
      });
      this.redirectToArchive();
    }
  }

  getArchiveCount(str) {
    return str.split('-')[1];
  }

  redirectToArchive() {
    const selectedTab = this.ws.sentSelectedUserTab.split('@');
    const tabIndex = parseInt(selectedTab[0], 10);
    this.ws.archiveSelectedUserTab = (tabIndex * 2) + 1 + '@' + selectedTab[1] + 'Sent';
    this.router.navigateByUrl('workflow/archive');
  }

  archiveSuccess() {
    this.growlService.showGrowl({
      severity: 'info',
      summary: 'Success', detail: 'Archived Successfully'
    });
    this.redirectToArchive();
  }

  archiveFailed() {
    this.growlService.showGrowl({
      severity: 'error',
      summary: 'Failure', detail: 'Failed To Archive Workitems'
    });
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
    this.clearSubscriptions();
    for (const subs of this.subscription) {
      subs.unsubscribe();
    }
    this.subscription = [];
    this.selectedItem = [];
    this.user = undefined;
    this.emptyMessage = undefined;
    this.colHeaders = [];
    this.itemsPerPage = undefined;
    this.totalRecords = undefined;
    this.sentWorkitems = {};
    this.columns = [];
    this.selectedColumns = [];
    this.actions = [];
    this.disableAction = true;
    this.selectedCount = 0;
    this.curPage = 1;
    this.selectedUser = undefined;
    this.type = undefined;
    this.breadcrumbService.dashboardFilterQuery = undefined;
    this.selectedTabIndex = 0;
    this.userTabsTotalCount = 0;
    this.roleTabsTotalCount = [];
    this.delTabsTotalCount = [];
    this.clearFilterBool = [];
    this.hasFilterResults = [];
  }
}
