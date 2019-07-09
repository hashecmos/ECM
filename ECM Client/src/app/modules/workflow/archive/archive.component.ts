import {Component, OnDestroy, OnInit, QueryList, ViewChild, ViewChildren} from '@angular/core';
import {BreadcrumbService} from "../../../services/breadcrumb.service";
import {WorkflowService} from '../../../services/workflow.service';
import {UserService} from '../../../services/user.service';
import {Subscription} from 'rxjs/Rx';
import * as $ from 'jquery';
import {WorkitemSet} from '../../../models/workflow/workitem-set.model';
import {User} from '../../../models/user/user.model';
import {MultiSelectModule} from 'primeng/primeng';
import {SelectItem} from 'primeng/primeng';
import {style} from '@angular/animations';
import {FormBuilder, FormGroup, Validators, FormControl} from '@angular/forms';
import * as global from '../../../global.variables';
import {saveAs} from 'file-saver';
import {CoreService} from "../../../services/core.service";
import {FilterComponent} from "../../../components/generic-components/filter/filter.component";

@Component({
  selector: 'app-archive',
  templateUrl: './archive.component.html',
  styleUrls: ['./archive.component.css']
})
export class ArchiveComponent implements OnInit, OnDestroy {
  private subscription: Subscription[] = [];
  @ViewChildren(FilterComponent) filterComponent: QueryList<FilterComponent>;
  public selectedItem: any[] = [];
  public colHeaders: any[] = [];
  public sentColHeaders: any[] = [];
  public itemsPerPage: any;
  public totalRecords: any;
  emptyMessage: any;
  public archiveWorkitems: WorkitemSet = {};
  public columns: any[];
  public sentColumns: any[];
  public selectedColumns: string[] = [];
  public selectedSentColumns: string[] = [];
  public user = new User();
  public actions: string[] = [];
  defaultSelected = new FormControl();
  sentDefaultSelected = new FormControl();
  action = new FormControl();
  public selectedAction: any;
  public disableAction = true;
  public selectedCount = 0;
  public selectedTabIndex = 0;
  public previousSelectedTab: any;
  filterCount = {total: -1, pageSize: 0, to: 0, cc: 0, reply: 0, replyto: 0, replycc: 0, new: 0, read: 0, forwarded: 0,overdue: 0, newToday: 0};
  public filterQuery = {
    'receivedDate': '',
    'recipientName': '',
    'userId': '',
    'userType': '',
    'repStatus': '',
    'exportFormat':'',
    'exportFilter':false
  };
  public userInboxTabsTotalCount = 0;
  public userSentTabsTotalCount = 0;
  public roleInboxTabsTotalCount: any[] = [];
  public roleSentTabsTotalCount: any[] = [];
  public delInboxTabsTotalCount: any[] = [];
  public delSentTabsTotalCount: any[] = [];
  public hasFilterResults: any[] = [];
  public clearFilterBool: any[] = [];
  public sender: SelectItem[] = [];
  public recipients: SelectItem[] = [];
  public selectedUser;
  public type;
  public curPage: any = 1;
  public activeTab = 'inbox';
  public lazy = true;
  public archiveTieredItems: any[] = [];
  private subscriptions: any[] = [];
  request: any = {pageNo: 1};
  private advanceFilterShown = false;
  public showDelegationInactiveDialog = false;
  public forOptions: any[];
  constructor(private breadcrumbService: BreadcrumbService, private ws: WorkflowService, private us: UserService,
              private coreService: CoreService) {
    this.user = this.us.getCurrentUser();
    this.emptyMessage = global.no_workitem_found;
    this.breadcrumbService.setItems([
      {label: 'Workflow'},
      {label: 'Archive'},
      {label: this.user.fulName}
    ]);

    this.archiveTieredItems.push({
      label: 'Call Date Report',
      icon: 'ui-icon-event-note',
      items : [{
            label: 'PDF',
            icon: 'ui-icon-description', command: (event) => {
              this.exportArchive('all', 'pdf');
            }
          },{
            label: 'Excel',
            icon: 'ui-icon-assignment', command: (event) => {
              this.exportArchive('all', 'excel');
            }
          }
      ],
      disabled: true,
      visible: false
    },
      {
      label: 'Export All Workitems',
      icon: 'ui-icon-assignment-returned',
      disabled: true,
      items : [{
            label: 'PDF',
            icon: 'ui-icon-description', command: (event) => {
              this.exportArchive('all', 'pdf');
            }
          },{
            label: 'Excel',
            icon: 'ui-icon-assignment', command: (event) => {
              this.exportArchive('all', 'excel');
            }
          }
      ]
    });
  }

  ngOnInit() {
    if (this.user) {
      this.subscription.push(this.ws.getUserArchiveInbox(this.user.EmpNo,1, this.request.curPage).subscribe(
        data => {
          this.userInboxTabsTotalCount = data.totalCount;
        }
      ));
      this.subscription.push(this.ws.getUserArchiveSentItems(this.user.EmpNo,1, this.request.curPage).subscribe(
        data => {
          this.userSentTabsTotalCount = data.totalCount;
        }
      ));
      if (this.user.roles.length > 0) {
        this.user.roles.map((role, index) => {
          this.subscription.push(this.ws.getRoleArchiveInbox(role.id, this.user.EmpNo,1, this.request.curPage).subscribe(
            data => {
              this.roleInboxTabsTotalCount[role.id] = data.totalCount;
            }
          ));
          this.subscription.push(this.ws.getRoleArchiveSentItems(role.id, this.user.EmpNo,1, this.request.curPage).subscribe(
            data => {
              this.roleSentTabsTotalCount[role.id] = data.totalCount;
            }
          ));
        });
      }
      if (this.user.delegated.length > 0) {
        this.user.delegated.map((del, index) => {
          this.subscription.push(this.ws.getUserArchiveInbox(del.userId,1, this.request.curPage).subscribe(
            data => {
              this.delInboxTabsTotalCount[del.userId] = data.totalCount;
            }
          ));
          this.subscription.push(this.ws.getUserArchiveSentItems(del.userId, 1,this.request.curPage).subscribe(
            data => {
              this.delSentTabsTotalCount[del.userId] = data.totalCount;
            }
          ));
        });
      }
    }
    if (this.ws.archiveSelectedUserTab) {
      this.previousSelectedTab = this.ws.archiveSelectedUserTab.split('@');
      this.tabChange(this.previousSelectedTab[1], this.previousSelectedTab[0]);
    } else {
      if (this.user.roles.length > 0) {
        this.tabChange(this.user.roles[0].name + 'Inbox', 2);
      }
      else if (this.user) {
        this.tabChange(this.user.fulName + ' Inbox', 0);
      }
    }
    this.actions = [];
    this.colHeaders = [{field: 'status', header: 'Status', hidden: true}, {field: 'type', header: 'Type', hidden: true},
      {field: 'instructions', header: 'Instructions', hidden: true}, {field: 'receivedDate', header: 'Received Date', hidden: true, sortField: 'receivedDate2'},
      {field: 'senderName', header: 'Sender Name', hidden: true}, {field: 'actions', header: 'Actions', hidden: true},
      {field: 'recipientName', header: 'Recipient Name', hidden: true}, {field: 'wfCreatorName', header: 'Created By', hidden: true},
      {field: 'workitemId', header: 'workitemId', hidden: true}, {field: 'sentitemId', header: 'sentitemId', hidden: true},
      {field: 'deadline', header: 'Deadline', hidden: true}, {field: 'reminder', header: 'Reminder', hidden: true}
    ];
    this.columns = [{label: 'Status', value: 'status'}, {label: 'Type', value: 'type'},
      {label: 'Instructions', value: 'instructions'}, {label: 'Received Date', value: 'receivedDate'},
      {label: 'Sender Name', value: 'senderName'}, {label: 'Actions', value: 'actions'},
      {label: 'Recipient Name', value: 'recipientName'}, {label: 'Created By', value: 'wfCreatorName'},
      {label: 'Deadline', value: 'deadline'}, {label: 'Reminder', value: 'reminder'}
    ];
    this.selectedColumns = ['status', 'type', 'receivedDate', 'senderName', 'actions'];
    this.defaultSelected.setValue(this.selectedColumns);
    for (const colunm of this.selectedColumns) {
      for (const tableHead of this.colHeaders) {
        if (tableHead.field === colunm) {
          tableHead.hidden = false;
        }
      }
    }

    // sentitems
    this.sentColHeaders = [{field: 'subject', header: 'Subject', hidden: true}, {field: 'receivedDate', header: 'Sent Date', hidden: true, sortField: 'receivedDate2'},
      {field: 'wfCreatorName', header: 'Created By', hidden: true}, {field: 'workitemId', header: 'workitemId', hidden: true},
      {field: 'sentitemId', header: 'sentitemId', hidden: true}
    ];
    this.sentColumns = [{label: 'Subject', value: 'subject'}, {label: 'Sent Date', value: 'receivedDate'},
      {label: 'Created By', value: 'wfCreatorName'}
    ];
    this.selectedSentColumns = ['subject', 'receivedDate', 'wfCreatorName'];
    this.sentDefaultSelected.setValue(this.selectedSentColumns);
    for (const colunm of this.selectedSentColumns) {
      for (const tableHead of this.sentColHeaders) {
        if (tableHead.field === colunm) {
          tableHead.hidden = false;
        }
      }
    }
    this.getForOptions(this.user.EmpNo);
  }

  clearFilterResults(event){
    this.clearFilter();
  }
  closeFilters(event) {
    $('.filter').slideUp();
    this.advanceFilterShown = false;
    this.clearFilter();
  }
  clearFilter(){
    this.breadcrumbService.dashboardFilterQuery = undefined;
    this.resetFilterModel();
    this.getArchives();
  }
  resetFilterModel(){
    if(this.filterComponent){
      this.filterComponent.map(r=>{
         r.resetFilter();
      });
      this.request.recipientName = undefined;
    }
  }
  getFilterSenderOptions(id, userType) {
    this.sender = [];
    const subscription = this.ws.getInboxFilterUsers(id, userType, 'finish').subscribe(res => {
      for (const user of res) {
        this.sender.push({label: user.name, value: user.userType + ':' + user.id});
      }
    });
    this.coreService.progress = {busy: subscription, message: '', backdrop: true};
    this.addToSubscriptions(subscription);
  }
  getFilterRecipientOptions(id, userType){
    this.recipients = [];
    const subscription = this.ws.getSentitemFilterUsers(id, userType, 'archive').subscribe(res => {
      for (const user of res) {
        this.recipients.push({label: user.name, value: user.userType + ':' + user.id});
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

  getFiltertoggle(data) {
    this.advanceFilterShown = !this.advanceFilterShown;
    this.toggleFilter();
  }

  getSelectedAction(data: any) {
  }

  assignArchiveItems(data: any) {
    data.workitems.map(d => d.receivedDate2 = this.coreService.convertToTimeInbox(d.receivedDate));
    this.archiveWorkitems = data;
    this.itemsPerPage = data.pageSize;
    this.totalRecords = data.totalCount;
    this.archiveTieredItems.map((item, index) => {
      item.disabled = !data.workitems || data.workitems.length === 0;
    });
  }

  getFilteredData(data: any) {
    data.workitems.map(d => d.receivedDate2 = this.coreService.convertToTimeInbox(d.receivedDate));
    this.archiveWorkitems = data;
    this.itemsPerPage = data.pageSize;
    this.totalRecords = data.totalCount;
    this.archiveTieredItems.map((item, index) => {
      item.disabled = !data.workitems || data.workitems.length === 0;
    });
  }

  columnSelectionChanged(event: Event) {
    if(this.activeTab === 'inbox'){
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
    } else {
        for (const tableHead of this.sentColHeaders) {
          tableHead.hidden = true;
        }
        for (const colunm of this.selectedSentColumns) {
          for (const tableHead of this.sentColHeaders) {
            if (tableHead.field === colunm) {
              tableHead.hidden = false;
            }
          }
        }
    }
  }

  actionSelectionChanged(event) {

  }

  tabChange(textLabel, index) {
    $('.filter').slideUp();
    this.advanceFilterShown = false;
    this.resetFilterModel();
    this.selectedTabIndex = index;
    this.ws.archiveSelectedUserTab = this.selectedTabIndex + '@' + textLabel;
    this.selectedItem = [];
    this.clearFilterBool = [];
    this.disableAction = true;
    this.breadcrumbService.setItems([
      {label: 'Workflow'},
      {label: 'Archive'},
      {label: textLabel}
    ]);

    for (const role of this.user.roles) {
      if (textLabel.includes(role.name)) {
        this.request.userType = 'ROLE';
        this.request.userId = role.id;
        this.request.empNo = role.id;
        this.request.recipientName = undefined;
        this.request.senderName = undefined;
        this.ws.delegateId = undefined;
      }
    }
    for (const delegate of this.user.delegated) {
      if (textLabel.includes(delegate.delName)) {
        this.request.userType = 'USER';
        if (textLabel.includes('Sent')) {
          this.request.senderName = delegate.userId;
          this.request.recipientName = undefined;
        } else {
          this.request.recipientName = 'USER:' + delegate.userId;
          this.request.senderName = undefined;
        }
        this.request.userId = delegate.userId;
        this.request.empNo = this.user.EmpNo;
        this.us.validateDelegation(delegate.id).subscribe(res=>{
          if(res==='INACTIVE'){
            this.showDelegationInactiveDialog = true;
          }
        });
        this.ws.delegateId = delegate.id;
      }
    }
    if (textLabel.includes(this.user.fulName)) {
      this.request.userType = 'USER';
      if (textLabel.includes('Sent')) {
        this.request.senderName = this.user.EmpNo;
        this.request.recipientName = undefined;
      } else {
        this.request.recipientName = 'USER:' + this.user.EmpNo;
        this.request.senderName = undefined;
      }

      this.request.userId = this.user.EmpNo;
      this.request.empNo = this.user.EmpNo;
      this.ws.delegateId = undefined;
    }

    if (textLabel.includes('Inbox')) {
      this.activeTab = 'inbox';
      this.getFilterSenderOptions(this.request.userId, this.request.userType);
    }
    if (textLabel.includes('Sent')) {
      this.activeTab = 'sent';
      this.getFilterRecipientOptions(this.request.userId, this.request.userType)
    }
    this.getArchives();
  }

  countFiltered(data: any) {
    this.filterCount = {total: data.totalCount, pageSize: data.setCount, to: 0, cc: 0, reply: 0, replyto: 0, replycc: 0, new: 0, read: 0, forwarded: 0, overdue: 0, newToday: 0};
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
    }

  }

  toggleFilter() {
    this.clearFilterBool = [];
    $('.filter').slideToggle();
  }

  defaultFilter(type, page, id) {
    // this.filterQuery.recipientName = type+':'+id;
    this.filterQuery.userId = id;
    this.filterQuery.userType = type;
  }

  exportArchive(type, exportType) {
    if (type === 'today') {
      const today = new Date();
      this.filterQuery.receivedDate = today.getDate() + '/' + (today.getMonth() + 1) + '/' + today.getFullYear();
    }
    if (this.activeTab === 'inbox') {
      this.filterQuery.repStatus = 'finish';
      if(exportType === 'pdf'){
        this.filterQuery.exportFormat = 'pdf';
        const subscription = this.ws.exportInbox(this.filterQuery).subscribe(res => {
        const file = new Blob([res], {type: 'application/pdf'});
        const fileName = 'Archived_Inbox' + '.pdf';
        saveAs(file, fileName);
        });
        this.coreService.progress = {busy: subscription, message: '', backdrop: true};
        this.addToSubscriptions(subscription);
      } else {
        this.filterQuery.exportFormat = 'xls';
        const subscription = this.ws.exportInbox(this.filterQuery).subscribe(res => {
        const file = new Blob([res], {type: 'application/vnd.ms-excel'});
        const fileName = 'Archived_Inbox' + '.xlsx';
        saveAs(file, fileName);
        });
        this.coreService.progress = {busy: subscription, message: '', backdrop: true};
        this.addToSubscriptions(subscription);
      }
    } else if (this.activeTab === 'sent') {
        this.filterQuery.repStatus = 'archive';
      if(exportType === 'pdf'){
        this.filterQuery.exportFormat = 'pdf';
        const subscription = this.ws.exportSent(this.filterQuery).subscribe(res => {
        const file = new Blob([res], {type: 'application/pdf'});
        const fileName = 'Archived_Sent' + '.pdf';
        saveAs(file, fileName);
        });
        this.coreService.progress = {busy: subscription, message: '', backdrop: true};
        this.addToSubscriptions(subscription);
      } else {
        this.filterQuery.exportFormat = 'xls';
        const subscription = this.ws.exportSent(this.filterQuery).subscribe(res => {
        const file = new Blob([res], {type: 'application/vnd.ms-excel'});
        const fileName = 'Archived_Sent' + '.xlsx';
        saveAs(file, fileName);
        });
        this.coreService.progress = {busy: subscription, message: '', backdrop: true};
        this.addToSubscriptions(subscription);
      }
    }
    this.filterQuery.receivedDate = '';
  }

  refreshTable(event) {
    if (this.ws.archiveSelectedUserTab) {
      this.previousSelectedTab = this.ws.archiveSelectedUserTab.split('@');
      this.tabChange(this.previousSelectedTab[1], this.previousSelectedTab[0]);
    }
  }

  assignSortNotPaginationInfo(data) {
    if (!data || !data.rows) {
      return;
    }
    if (data.globalFilter.length > 0) {
      const newData = [];
      this.archiveWorkitems.workitems.map(item => {
        if ((item.subject && item.subject.toLowerCase().indexOf(data.globalFilter.toLowerCase()) !== -1) ||
          (item.status && item.status.toLowerCase().indexOf(data.globalFilter.toLowerCase()) !== -1) ||
          (item.type && item.type.toLowerCase().indexOf(data.globalFilter.toLowerCase()) !== -1) ||
          (item.receivedDate && item.receivedDate.toLowerCase().indexOf(data.globalFilter.toLowerCase()) !== -1) ||
          (item.wfCreatorName && item.wfCreatorName.toLowerCase().indexOf(data.globalFilter.toLowerCase()) !== -1) ||
          (item.actions && item.actions.toLowerCase().indexOf(data.globalFilter.toLowerCase()) !== -1)) {
          newData.push(item);
        }
      });
      this.archiveWorkitems.workitems = newData;
      return;
    }

    this.request.pageNo = Math.ceil(data.first / data.rows) + 1;
    this.request.sort = data.sortField;
    if (data.sortField === 'receivedDate2') {
      this.request.sort = 'createdDate';
    }
    if (data.sortOrder === 1) {
      this.request.order = 'ASC';
    }
    else {
      this.request.order = 'DESC';
    }


    this.getArchives();


  }

  getArchives() {
    //this.request.repStatus = 'finish';
    this.request.empNo = this.user.EmpNo;
    let subscription;
    if (this.activeTab === 'inbox') {
       this.request.repStatus = 'finish';
      subscription = this.ws.searchInbox(this.request).subscribe(res => {
        res.workitems.map(d => d.receivedDate2 = this.coreService.convertToTimeInbox(d.receivedDate));
        this.itemsPerPage = res.pageSize;
        this.totalRecords = res.totalCount;
        this.defaultFilter(this.request.userType, 'inbox', this.request.userId);
        this.archiveWorkitems = res;
        if (this.advanceFilterShown) {
          this.countFiltered(res);
        } else {
          this.filterCount.total = -1;
        }
        this.archiveTieredItems.map((item, index) => {
          item.disabled = !this.archiveWorkitems.workitems || this.archiveWorkitems.workitems.length === 0;
        });

      });

    }
    else if (this.activeTab === 'sent') {
       this.request.repStatus = 'archive';
      subscription = this.ws.searchSentUser(this.request).subscribe(res => {
        res.workitems.map(d => d.receivedDate2 = this.coreService.convertToTimeInbox(d.receivedDate));
        this.itemsPerPage = res.pageSize;
        this.totalRecords = res.totalCount;
        this.defaultFilter(this.request.userType, 'sent', this.request.userId);
        this.archiveWorkitems = res;
        if (this.advanceFilterShown) {
          this.countFiltered(res);
        } else {
          this.filterCount.total = -1;
        }
        this.archiveTieredItems.map((item, index) => {
          item.disabled = !this.archiveWorkitems.workitems || this.archiveWorkitems.workitems.length === 0;
        });

      });

    }

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
    this.colHeaders = [];
    this.itemsPerPage = undefined;
    this.archiveWorkitems = {};
    this.columns = [];
    this.selectedColumns = [];
    this.user = undefined;
    this.defaultSelected = undefined;
    this.actions = [];
    this.selectedAction = undefined;
    this.disableAction = true;
    this.selectedCount = 0;
    this.selectedTabIndex = 0;
    this.userInboxTabsTotalCount = 0;
    this.userSentTabsTotalCount = 0;
    this.roleInboxTabsTotalCount = [];
    this.roleSentTabsTotalCount = [];
    this.delInboxTabsTotalCount = [];
    this.delSentTabsTotalCount = [];

  }
}
