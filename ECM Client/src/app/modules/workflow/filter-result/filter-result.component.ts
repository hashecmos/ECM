import {Component, OnDestroy, OnInit, QueryList, ViewChildren} from '@angular/core';
import {WorkitemSet} from "../../../models/workflow/workitem-set.model";
import {Subscription} from "rxjs/Rx";
import {FilterComponent} from "../../../components/generic-components/filter/filter.component";
import {User} from "../../../models/user/user.model";
import {ConfirmationService, SelectItem} from "primeng/primeng";
import {WorkflowService} from "../../../services/workflow.service";
import {UserService} from "../../../services/user.service";
import {Router} from "@angular/router";
import {GrowlService} from "../../../services/growl.service";
import {CoreService} from "../../../services/core.service";
import {BreadcrumbService} from "../../../services/breadcrumb.service";
import {BrowserEvents} from "../../../services/browser-events.service";
import * as global from "../../../global.variables";
import * as $ from 'jquery';
import {saveAs} from 'file-saver';
@Component({
  selector: 'app-filter-result',
  templateUrl: './filter-result.component.html',
  styleUrls: ['./filter-result.component.css']
})
export class FilterResultComponent implements OnInit, OnDestroy {
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
  public dashboardSelectedTab: any;
  filterCount = {total: -1, pageSize: 0, to: 0, cc: 0, reply: 0, replyto: 0, replycc: 0, new: 0, read: 0, forwarded: 0};
  public filterQuery = {
    'receivedDate': '',
    'senderName': '',
    'userId': '',
    'userType': '',
    'repStatus': '',
    'exportFormat': '',
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
      {label: 'Actioned'},
      {label: this.user.fulName}
    ]);
    this.sentTieredItems.push({
      label: 'Call Date Report',
      icon: 'ui-icon-event-note',
      items : [{
            label: 'PDF',
            icon: 'ui-icon-description', command: (event) => {
              this.exportActioned('today', 'pdf');
            }
          },{
            label: 'Excel',
            icon: 'ui-icon-assignment', command: (event) => {
              this.exportActioned('today', 'excel');
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
              this.exportActioned('all', 'pdf');
            }
          },{
            label: 'Excel',
            icon: 'ui-icon-assignment', command: (event) => {
              this.exportActioned('all', 'excel');
            }
          }
      ]
    });

    this.actions = ['Recall', 'Archive', 'Finish Before'];
    this.colHeaders = [
      {field: 'subject', header: 'Subject', hidden: true}, {field: 'receivedDate', header: 'Sent Date', hidden: true, sortField: 'receivedDate2'},
      {field: 'wfCreatorName', header: 'Created By', hidden: true}, {field: 'workitemId', header: 'workitemId', hidden: true},
      {field: 'sentitemId', header: 'sentitemId', hidden: true}, {field: 'status', header: 'Status', hidden: true}
    ];
    this.columns = [{label: 'Subject', value: 'subject'}, {label: 'Sent Date', value: 'receivedDate'},
      {label: 'Created By', value: 'wfCreatorName'}, {label: 'Status', value: 'status'}
    ];
    this.selectedColumns = ['subject', 'receivedDate', 'wfCreatorName', 'status'];
    for (const colunm of this.selectedColumns) {
      for (const tableHead of this.colHeaders) {
        if (tableHead.field === colunm) {
          tableHead.hidden = false;
        }
      }
    }
  }

  ngOnInit() {
    if (this.breadcrumbService.fromDashboard && this.breadcrumbService.dashboardFilterQuery.filterStatus === 'Actioned') {
        //this.dashboardFilter = true;
        //this.dashboardSelectedTab = this.breadcrumbService.dashboardTabSelected.split('@');
        this.breadcrumbService.setItems([
          {label: 'Workflow'},
          {label: 'Actioned'},
          {label: this.breadcrumbService.dashboardFilterQuery.filterUserName}
        ]);
        //this.request.sysStatus = 'Actioned';
        if(this.breadcrumbService.dashboardFilterQuery){
          this.request.userId = this.breadcrumbService.dashboardFilterQuery.filterUserId;
          this.request.userType = this.breadcrumbService.dashboardFilterQuery.filterUserType.toUpperCase();
          this.request.type = this.breadcrumbService.dashboardFilterQuery.filterWIType;
            if(this.breadcrumbService.dashboardFilterQuery.filterReceivedDay === 'Today'){
              const date = new Date();
              this.request.receivedDate = date.getDate() + '/' + (date.getMonth() + 1) + '/' + date.getFullYear();
            }
        }
        this.getSentItems();
        //this.getFilterSenderOptions(this.request.userId, this.request.userType);
        //this.breadcrumbService.fromDashboard = false;
        this.getForOptions(this.user.EmpNo);
      }
  }
  countFiltered(data: any) {
    this.filterCount = {total: data.totalCount, pageSize: data.setCount, to: 0, cc: 0, reply: 0, replyto: 0, replycc: 0, new: 0, read: 0, forwarded: 0};
    for (const item of data.workitems) {
      if (item.actionId === 1) {
        this.filterCount.forwarded++;
      }  else if (item.actionId === 2) {
        this.filterCount.reply++;
      }
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
    //this.breadcrumbService.dashboardFilterQuery = undefined;
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
    else if (data === 'Finish Before') {
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
    this.disableAction = true;
    this.selectedItem = [];
    this.clearFilterBool = [];
    this.request.repStatus = 'active';
    this.request.empNo = this.user.EmpNo;
    const subscription = this.ws.searchActionedItems(this.request).subscribe(res => {
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
                  setTimeout(()=>{
                    this.getSentItems();
                    this.selectedItem = [];
                  },200)
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

  exportActioned(type, exportType) {
    if (type === 'today') {
      const today = new Date();
      this.filterQuery.receivedDate = today.getDate() + '/' + (today.getMonth() + 1) + '/' + today.getFullYear();
    }
    if(exportType === 'pdf'){
      this.filterQuery.exportFormat = 'pdf';
      const subscription = this.ws.exportActioned(this.filterQuery).subscribe(res => {
      const file = new Blob([res], {type: 'application/pdf'});
      const fileName = 'Actioned' + '.pdf';
      saveAs(file, fileName);
      this.filterQuery.receivedDate = '';
     });
      this.coreService.progress={busy:subscription,message:''};
      this.addToSubscriptions(subscription);
    } else {
      this.filterQuery.exportFormat = 'xls';
      const subscription = this.ws.exportActioned(this.filterQuery).subscribe(res => {
      const file = new Blob([res], {type: 'application/vnd.ms-excel'});
      const fileName = 'Actioned' + '.xlsx';
      saveAs(file, fileName);
      this.filterQuery.receivedDate = '';
     });
      this.coreService.progress={busy:subscription,message:''};
      this.addToSubscriptions(subscription);
    }
  }

  refreshTable(event) {
    this.getSentItems();
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
    //this.breadcrumbService.dashboardFilterQuery = undefined;
    this.selectedTabIndex = 0;
    this.userTabsTotalCount = 0;
    this.roleTabsTotalCount = [];
    this.delTabsTotalCount = [];
    this.clearFilterBool = [];
    this.hasFilterResults = [];
  }
}
