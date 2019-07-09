import {Component, OnInit, Input, Output, EventEmitter, OnDestroy, ViewChild} from '@angular/core';
import {ConfirmationService, DataTable, MenuItem} from 'primeng/primeng';
import {DocumentInfoModel} from '../../../models/document/document-info.model';
import {Subscription} from 'rxjs/Rx';
import {DocumentService} from '../../../services/document.service';
import {BrowserEvents} from '../../../services/browser-events.service';
import {DocumentSecurityModel} from '../../../models/document/document-security.model';
import {DomSanitizer, SafeResourceUrl} from '@angular/platform-browser';
import {Router, ActivatedRoute} from '@angular/router';
import {FileViewerComponent} from 'ngx-file-viewer';
import {WorkflowService} from '../../../services/workflow.service';
import {User} from '../../../models/user/user.model';
import {ContentService} from '../../../services/content-service.service';
import {UserService} from '../../../services/user.service';
import {CoreService} from "../../../services/core.service";
import {GrowlService} from "../../../services/growl.service";
import {WorkitemDetails} from "../../../models/workflow/workitem-details.model";
import {$} from "protractor";

@Component({
  selector: 'data-table',
  templateUrl: './datatable.component.html',
})
export class DataTableComponent implements OnInit, OnDestroy {
  @Input() public tableData: any[];
  @Input() public colHeaders: any[];
  @Input() public showInfoIcon: any;
  @Input() public showDownloadIcon: any = false;
  @Input() public showCount: any = false;
  @Input() public showAddCartIcon: any = false;
  @Input() public showCheckBox: any = true;
  @Input() public itemsPerPage: any;
  @Input() public rowExpandable: any;
  @Input() public totalRecords: any;
  @Input() public totalCount: any;
  @Input() public showProgressBtn = false;
  @Input() public lazy = false;
  @Input() public emptyMessage: any;
  @Input() public activePage: any;
  @Output() sendData = new EventEmitter();
  @Output() addToCart = new EventEmitter();
  @Output() download = new EventEmitter();
  @Output() toggleProgressDialogue = new EventEmitter();
  @Output() sendSortPagination = new EventEmitter();
  public docInfo: DocumentInfoModel[];
  public docVersion: DocumentInfoModel[];
  public docHistory: DocumentInfoModel[];
  public linkedDocuments: DocumentInfoModel[];
  public docSecurity: DocumentSecurityModel[];
  public docSysProp: any;
  private noLink = false;
  public selectedVersion: any = {props: []};
  viewDocTitle: any;
  headId: any;
  foldersFiledIn: any;
  private attach_url: SafeResourceUrl;
  public selectedRows: any;
  public rowSelectionMode: string;
  public resizableColumns: boolean;
  public reorderableColumns: boolean;
  public enableGlobalFilter = true;
  private subscriptions: Subscription[] = [];
  private subscriptionEmit: Subscription[] = [];
  private pageUrl: any;
  viewer = false;
  displayinfo = false;
  notWorkflow = false;
  docTitle: any;
  public user = new User();
  public fromPage: any;
  sortF: any;
  isViewerClick = false;
  showIframe = false;
  @Output() refreshScreen = new EventEmitter();
  @Output() viewDraftItems = new EventEmitter();
  public sentitemWorkitems: any;
  public docTrack: any[] = [];
  public showTrack = false;
  public workitemHistory: any;
  public trackWorkitemDetails: WorkitemDetails;
  public trackColHeaders: any[];
  public busyModal: Subscription;
  first: any = 0;
  public showDelegationInactiveDialog = false;
  public reportPage = false;

  constructor(private ds: DocumentService, private sanitizer: DomSanitizer, private router: Router, private bs: BrowserEvents,
              private us: UserService, private coreService: CoreService,
              private ws: WorkflowService, private confirmationService: ConfirmationService, private contentServive: ContentService,
              private growlService: GrowlService) {
    if (this.router.url.includes('report')) {
      this.showCheckBox = false;
      this.reportPage = true;
    }
    this.rowSelectionMode = 'multiple';
    this.resizableColumns = true;
    this.reorderableColumns = true;
    this.enableGlobalFilter = true;
    this.docSysProp = [];
    this.selectedRows = [];
    this.docInfo = [];
    this.pageUrl = this.router.url;
    this.user = this.us.getCurrentUser();
    this.fromPage = this.pageUrl.slice(this.pageUrl.indexOf('#/workflow') + 11);
    this.trackColHeaders = [{field: 'actionBy', header: 'Actioned By', hidden: false}, {
      field: 'recipientName',
      header: 'Recipient Name',
      hidden: false
    },
      {field: 'timeStamp', header: 'Time', hidden: false}, {field: 'details', header: 'Action', hidden: false}];
  }

  rowSelected(event: any) {
    if (!(this.pageUrl.includes('workflow'))) {
      this.notWorkflow = true;
      if (this.isViewerClick) {
        this.sendData.emit(this.selectedRows);
        this.viewer = true;
        this.viewDoc(event.data);
      }
    }

  }

  clearCheckedItems() {
    this.selectedRows = Object.assign([], []);
    this.sendData.emit(this.selectedRows);
  }

  changeFilterText() {
    this.clearCheckedItems();
    const subscription = this.bs.changeFilterText.emit(this.selectedRows);
    this.addToSubscriptions(subscription);
  }

  mToggleProgressDialogue(workitemId, $event?) {
    this.toggleProgressDialogue.emit(workitemId);
    if ($event) {
      $event.stopPropagation();
    }

  }

  showSentWorkitems(event) {
    localStorage.setItem('openWkItem', JSON.stringify(event.data));
    if (this.activePage === 'sent') {
      localStorage.setItem('openWkItem', JSON.stringify(event.data));
      if (this.fromPage === 'sent' || this.fromPage === 'actioned' || this.reportPage) {
        const subscription = this.ws.getSentItemsWorkitems(event.data.workitemId, event.data.senderId, 'ACTIVE').subscribe(
          data => this.assignSentitemWorkitem(data, event.data), Error => console.log(Error));
        this.coreService.progress = {busy: subscription, message: ''};
        this.addToSubscriptions(subscription);
      } else if (this.fromPage === 'archive') {
        const subscription = this.ws.getSentItemsWorkitems(event.data.workitemId, event.data.senderId, 'ARCHIVE').subscribe(
          data => this.assignSentitemWorkitem(data, event.data), Error => console.log(Error));
        this.coreService.progress = {busy: subscription, message: ''};
        this.addToSubscriptions(subscription);
      }
    }
  }

  assignSentitemWorkitem(data, selectedItem) {
    this.sentitemWorkitems = data;
    if (data.length < 1) {
      this.noWorkitemFound(selectedItem);
    }
  }

  noWorkitemFound(selectedItem) {
    if (selectedItem.status === 'RECALL') {
      this.growlService.showGrowl({
        severity: 'error',
        summary: 'Failure',
        detail: 'No Workitem Found for Recalled item'
      });
    } else if (selectedItem.status === 'ARCHIVE') {
      this.growlService.showGrowl({
        severity: 'error',
        summary: 'Failure',
        detail: 'No Workitem Found for Archived item'
      });
    }
    else {
      this.growlService.showGrowl({
        severity: 'error',
        summary: 'Failure',
        detail: 'No Workitem Found'
      });
    }
  }

  ngOnInit() {
    if (this.activePage === 'inbox' || this.activePage === 'sent') {
      if (this.ws.first) {
        this.first = this.ws.first;
      }
      if (localStorage.getItem('openWkItem')) {
        this.selectedRows = [];
        this.selectedRows.push(JSON.parse(localStorage.getItem('openWkItem')));
        this.sendData.emit(this.selectedRows);
      }
    }

    // if(this.ws.pageNoSelected > 0 && this.ws.pageNoSelected !== undefined){
    //     this.first = this.ws.pageNoSelected;
    // }
    // if(localStorage.getItem('openWkItem')){
    //   this.selectedRows=JSON.parse(localStorage.getItem('openWkItem'));
    //   //this.checked();
    // }
    this.bs.clearSelectedDocs.subscribe(data => this.assignDocsSelected());
    this.subscriptions.push(this.bs.openDocInfoPanel.subscribe(data => this.assignDocInfoSelected(data)));
  }

  assignDocInfoSelected(data) {
    if (this.displayinfo === false) {
      this.displayinfo = true;
      this.openDocInfo(data[0]);
    }
  }

  assignDocsSelected() {
    this.selectedRows = [];
    this.sendData.emit(this.selectedRows);

  }

  checked() {
    this.sendData.emit(this.selectedRows);
    this.bs.docsSelected.emit(this.selectedRows);

  }

  unchecked() {
    this.sendData.emit(this.selectedRows);
    this.bs.docsSelected.emit(this.selectedRows);
  }

  mAddToCart(doc) {
    this.addToCart.emit(doc);
  }

  mDownload(doc) {
    this.download.emit(doc);
  }

  changePage(event) {
    this.ws.pageNoSelected = (event.first / event.rows) + 1;
    this.ws.first = event.first;
  }

  goToTaskDetails(event, from, subjects) {
    localStorage.setItem('openWkItem', JSON.stringify(subjects));
    if (this.fromPage !== 'sent') {
      if (this.pageUrl.indexOf('workflow') !== -1) {
        if (from === 'hyperlink') {
          if (this.activePage === 'draft') {
            this.viewDraftItem(subjects);
          } else {
            if (this.ws.delegateId !== undefined) {
              this.us.validateDelegation(this.ws.delegateId).subscribe(res => {
                if (res === 'INACTIVE') {
                  this.showDelegationInactiveDialog = true;
                } else {
                  this.router.navigateByUrl(`${this.pageUrl}/taskdetail/${subjects.workitemId}`);
                }
              });
            } else {
              this.router.navigateByUrl(`${this.pageUrl}/taskdetail/${subjects.workitemId}`);
            }
          }
        } else if (this.activePage !== 'draft') {
          if (this.ws.delegateId !== undefined) {
            this.us.validateDelegation(this.ws.delegateId).subscribe(res => {
              if (res === 'INACTIVE') {
                this.showDelegationInactiveDialog = true;
              } else {
                this.router.navigateByUrl(`${this.pageUrl}/taskdetail/${subjects.workitemId}`);
              }
            });
          } else {
            this.router.navigateByUrl(`${this.pageUrl}/taskdetail/${event.data.workitemId}`);
          }
        }
      }
    }
  }

  openSentWorkitem(workitemId) {
    if (this.activePage === 'sent') {
      if (this.pageUrl.indexOf('workflow') !== -1) {
        if (this.ws.delegateId !== undefined) {
          this.us.validateDelegation(this.ws.delegateId).subscribe(res => {
            if (res === 'INACTIVE') {
              this.showDelegationInactiveDialog = true;
            } else {
              this.router.navigateByUrl(`${this.pageUrl}/taskdetail/${workitemId}`);
            }
          });
        } else {
          this.router.navigateByUrl(`${this.pageUrl}/taskdetail/${workitemId}`);
        }
      }
    }
  }

  openDocInfo(doc) {
    this.docInfo = [];
    let subscription = this.ds.getDocument(doc.id)
      .subscribe(data => this.assignDocInfo(data));
    this.addToSubscriptions(subscription);
    subscription = this.ds.getDocumentVersions(doc.id)
      .subscribe(data => this.assignDocVersions(data));
    this.addToSubscriptions(subscription);
    subscription = this.ds.getDocumentPermissions(doc.id)
      .subscribe(data => this.assignDocSecurity(data));
    this.addToSubscriptions(subscription);
    subscription = this.ds.getLinks(doc.id)
      .subscribe(data => this.assignDocLink(data));
    this.addToSubscriptions(subscription);
    subscription = this.ds.getDocumentHistory(doc.id)
      .subscribe(data => this.assignDocHistory(data));
    this.addToSubscriptions(subscription);
    subscription = this.ds.getDocumentFolders(doc.id)
      .subscribe(data => this.assignDocumentFolders(data));
    this.addToSubscriptions(subscription);
    subscription = this.ds.getDocumentWorkflowHistory(doc.id)
      .subscribe(data => this.assignDocumentWorkflowHistory(data));
    this.coreService.progress = {busy: subscription, message: ''};
  }

  assignDocInfo(data) {
    this.docSysProp = [];
    data.props.map(p => {
      if (p.hidden === 'false') {
        this.docInfo.push(p);
      }
    });
    this.viewDocTitle = data.props[0].mvalues + " " + "(" + data.docclass + ")";
    this.docSysProp.push(data);
  }

  assignDocVersions(data) {
    this.docVersion = data;
  }

  assignDocSecurity(data) {
    this.docSecurity = data;
  }

  assignDocLink(data) {
    if (data.length > 0) {
      this.linkedDocuments = data;
      this.noLink = false;
    }
    else {
      this.noLink = true;
    }
  }

  assignDocHistory(data) {
    this.docHistory = data;
  }

  assignDocumentFolders(data) {
    this.foldersFiledIn = data;
  }

  assignDocumentWorkflowHistory(data) {
    this.docTrack = data;
  }

  closeModal() {
    this.docSysProp = [];
    this.displayinfo = false;
    this.viewDocTitle = '';
  }

  closeViewPopUp() {
    this.showIframe = false;
    this.isViewerClick = false;
  }

  downloadDoc(doc) {
    window.location.assign(this.ds.downloadThisDocument(doc.id));
  }

  openView(e, names) {
    this.isViewerClick = true;
    this.docTitle = names.name;
    this.viewDoc(names);
    e.stopPropagation();
  }

  viewDoc(doc) {
    this.showIframe = true;
    this.ds.getDocumentInfo(doc.id).subscribe(data => this.assignDocIdForView(data));

  }

  assignDocIdForView(data) {
    this.attach_url = this.transform(this.ds.getViewUrl(data.id));
    this.viewer = true
  }

  transform(url) {
    return this.sanitizer.bypassSecurityTrustResourceUrl(url);
  }

  refresh() {
    this.refreshScreen.emit(this.selectedRows);
  }

  loadLazy(event){
    this.sendSortPagination.emit(event);
  }

  selectVersion(version) {
    if (this.selectedVersion.id === version.id) {
      this.selectedVersion.id = undefined;
      return;
    }
    const props = [];
    this.selectedVersion.id = version.id;
    this.selectedVersion.props = [];
    if (version.props) {
      version.props.map(p => {
        this.selectedVersion.props.push({prop: p.desc, value: p.mvalues[0]})
      })
    } else {
      const subscription = this.ds.getDocument(version.id).subscribe(res => {
        version.props = res.props;
        version.props.map(p => {
          props.push({prop: p.desc, value: p.mvalues[0]})
        });
        this.selectedVersion.props = props;

      });
      this.coreService.progress = {busy: subscription, message: ''};
      this.addToSubscriptions(subscription);

    }
  }

  viewDraftItem(event) {
    this.viewDraftItems.emit(event);
  }


  sortFields(event) {

  }

  showTrackSentitem(workitemId) {
    this.busyModal = this.ws.getSentItemHistory(workitemId)
      .subscribe(data => {
        if (data) {
          this.workitemHistory = data.reverse();
        }
      });
    this.addToSubscriptions(this.busyModal);
    this.showTrack = true;
  }

  showTrackWorkitem(event) {
    if (event.data.details !== 'Launch') {
      this.busyModal = this.ws.getWorkitem(event.data.workitemId, this.user.EmpNo)
        .subscribe(data => this.trackWorkitemDetails = data);
      this.addToSubscriptions(this.busyModal);
    }
  }

  addToSubscriptions(subscription) {
    this.subscriptions.push(subscription);
  }

  clearSubscriptions() {
    this.subscriptions.map(s => {
      if (s) {
        s.unsubscribe();
      }
    });
  }

  reloadApp() {
    this.showDelegationInactiveDialog = false;
    window.location.reload(true);
  }

  ngOnDestroy() {
    this.clearSubscriptions();
    this.displayinfo = false;
  }

}


