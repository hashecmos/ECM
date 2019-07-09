import {Component, OnInit, Input, Output, EventEmitter, OnDestroy, AfterViewInit} from '@angular/core';
import {Router, ActivatedRoute} from '@angular/router'
import {FormBuilder, FormGroup, Validators, FormControl} from '@angular/forms';
import {SelectItem, Message, MenuItem} from 'primeng/primeng';
import {ConfirmationService} from 'primeng/primeng';
// services
import {BreadcrumbService} from "../../../services/breadcrumb.service";
import {WorkflowService} from '../../../services/workflow.service';
import {UserService} from '../../../services/user.service';
import {DocumentService} from '../../../services/document.service';
import {ContentService} from '../../../services/content-service.service';
import {Subscription} from 'rxjs/Rx';
import * as $ from 'jquery';
import * as global from '../../../global.variables';
import 'rxjs/Rx';
import {DomSanitizer, SafeResourceUrl} from '@angular/platform-browser';
// models
import {User} from '../../../models/user/user.model';
import {WorkitemDetails} from '../../../models/workflow/workitem-details.model';
import {DocumentInfoModel} from '../../../models/document/document-info.model';
import {DocumentSecurityModel} from '../../../models/document/document-security.model';
import {EntryTemplateDetails} from '../../../models/document/entry-template-details.model';
import {EntryTemplate} from '../../../models/document/entry-template.model';
import {Recall} from '../../../models/workflow/recall.model';
import {WorkItemAction} from '../../../models/workflow/workitem-action.model';
import {Recipients} from '../../../models/user/recipients.model';
import {BrowserEvents} from '../../../services/browser-events.service';
import {GrowlService} from '../../../services/growl.service';
import {CoreService} from "../../../services/core.service";
import {Observable} from "rxjs/Observable";

@Component({
  templateUrl: './task-detail.component.html',
  styleUrls: ['./task-detail.component.css'],
})
export class TaskDetailComponent implements OnInit, OnDestroy, AfterViewInit {
  private currentUser = new User();
  pdfSrc: Object;
  private pageUrl: any;
  public workitem: WorkitemDetails;
  private subscription: Subscription[] = [];
  public displayIframe = false;
  private current_url: SafeResourceUrl = null;
  private attach_url: SafeResourceUrl;
  public workitemHistory: any;
  public selectedRows: any;
  public colHeaders: any[] = [];
  public esignEnabled = false;
  public empNo: any;
  public roleId: any;
  public flagInitial: string;
  public docId: string;
  public docInfo: DocumentInfoModel[];
  public docVersion: DocumentInfoModel[];
  public docHistory: DocumentInfoModel[];
  public linkedDocuments: DocumentInfoModel[];
  public docSecurity: DocumentSecurityModel[];
  public docSysProp: any;
  private noLink = false;
  viewDocTitle: any;
  headId: any;
  public fileselected = false;
  private updateddDocuments = new FormData();
  public fileUploaded: any = undefined;
  public saveDocInfo = new DocumentInfoModel();
  public docTemplateDetails = new EntryTemplateDetails();
  public docEditPropForm: FormGroup;
  msgs: Message[] = [];
  public fromPage: any;
  public editAttachment: boolean;
  public editProperties: boolean;
  public entryTemp = false;
  public to: string[];
  foldersFiledIn: any;
  private breadCrumbPath: any[] = [];
  private recallmodel = new Recall();
  public addUserRecipients = [];
  addUser: any = {
    documents: {existing: {}, new: {}, cartItems: []},
    recipients: {roles: {}, list: {}, search: {result: []}, toList: [], ccList: []},
    workflow: {model: {}}
  };
  roleTreeExpandedIcon = 'ui-icon-people-outline';
  roleTreeCollapsedIcon = 'ui-icon-people';
  public distList = {'id': 1, 'empNo': 1002, 'name': 'Distribution List', lists: []};
  public defaultList = {'id': -1, 'empNo': 1002, 'name': 'Default List'};
  public globalList = {'id': 1, 'empNo': 1002, 'name': 'Global List', lists: []};
  roleTreeSelection: any;
  private filteredRoles: any[];
  public AddUserDialog = false;
  public busyModel: Subscription;
  public trackWorkitemDetails: WorkitemDetails;
  public selectedVersion: any = {props: []};
  public toRecipients: any[] = [];
  public ccRecipients: any[] = [];
  public docTrack: any[] = [];
  private tmpRoleTree = [];
  private workitemProgress: any[];
  private errorJson: any;
  public displayProgress = false;
  public ESignedAttachments: any[] = [];
  public pollingData: any;
  public recipientsTab = false;
  public eSignDialog = false;
  public showDelegationInactiveDialog = false;
  public showRecallInactiveDialog = false;
  public isesignverified = false;
  displayinfo = false;
  strikeIndex: any;
  attachment: any;

  constructor(public router: Router, private breadcrumbService: BreadcrumbService, private workflowService: WorkflowService,
              private sanitizer: DomSanitizer, private us: UserService, private ds: DocumentService, private cs: ContentService,
              private bs: BrowserEvents, private coreService: CoreService,
              private fb: FormBuilder, private confirmationService: ConfirmationService, private growlService: GrowlService) {
    this.pageUrl = router.url;
    this.sanitizer = sanitizer;
    this.workitem = new WorkitemDetails();
    this.currentUser = this.us.getCurrentUser();
    this.workitem.workitemId = this.pageUrl.slice(this.pageUrl.indexOf('taskdetail/') + 11);
    this.fromPage = (this.pageUrl.slice(this.pageUrl.indexOf('workflow/') + 9)).split('/');
    this.docEditPropForm = new FormGroup({
      DocumentTitle: new FormControl(null, [Validators.required, this.noWhitespaceValidator])
    });
    this.recallmodel.items = [];
    this.initAdduser();

  }

  ngOnInit() {

    const subscription = this.workflowService.getWorkitem(this.workitem.workitemId, this.currentUser.EmpNo)
      .subscribe(data => {
        this.workitem = data;
        this.breadCrumbPath = [
          {label: 'Workflow'}
        ];
        if (this.fromPage[0] === 'inbox') {
          this.breadCrumbPath.push({label: 'Inbox', routerLink: ['/workflow/' + this.fromPage[0]]});
          if (this.workitem.recipientName) {
            this.breadCrumbPath.push({label: this.workitem.recipientName});
          } else {
            this.breadCrumbPath.push({label: this.workitem.recipientRoleName});
          }
        } else if (this.fromPage[0] === 'sent') {
          this.breadCrumbPath.push({label: 'Sent', routerLink: ['/workflow/' + this.fromPage[0]]});
          if (this.workitem.senderName) {
            this.breadCrumbPath.push({label: this.workitem.senderName});
          } else {
            this.breadCrumbPath.push({label: this.workitem.senderRoleName});
          }
        } else if (this.fromPage[0] === 'archive') {
          this.breadCrumbPath.push({label: 'Archive', routerLink: ['/workflow/' + this.fromPage[0]]});
          if (this.workitem.senderName) {
            this.breadCrumbPath.push({label: this.workitem.senderName});
          } else if (this.workitem.senderRoleName) {
            this.breadCrumbPath.push({label: this.workitem.senderRoleName});
          } else if (this.workitem.recipientName) {
            this.breadCrumbPath.push({label: this.workitem.recipientName});
          } else if (this.workitem.recipientRoleName) {
            this.breadCrumbPath.push({label: this.workitem.recipientRoleName});
          }
        } else if (this.fromPage[0] === 'actioned') {
          this.breadCrumbPath.push({label: 'Actioned', routerLink: ['/workflow/' + this.fromPage[0]]});
          if (this.workitem.senderName) {
            this.breadCrumbPath.push({label: this.workitem.senderName});
          } else {
            this.breadCrumbPath.push({label: this.workitem.senderRoleName});
          }
        }
        this.breadCrumbPath.push({label: this.workitem.subject});
        this.breadcrumbService.setItems(this.breadCrumbPath);

        this.colHeaders = [{field: 'actionBy', header: 'Actioned By', hidden: false}, {
          field: 'recipientName',
          header: 'Recipient Name',
          hidden: false
        },
          {field: 'timeStamp', header: 'Time', hidden: false}, {field: 'details', header: 'Action', hidden: false}];
        this.getWorkitemHistory();
        if (this.workitem.actions === 'Signature' || this.workitem.actions === 'Initial') {
          this.esignEnabled = true;
          if (this.workitem.actions === 'Signature') {
            this.workitem.attachments.map((attachment, index) => {
              this.subscription.push(this.ds.verifyESign(attachment.docId, this.workitem.workitemId).subscribe(res => {
                if (res && res === 'True') {
                  this.isesignverified = true;
                  this.ESignedAttachments[attachment.docId] = true;
                } else {
                  this.ESignedAttachments[attachment.docId] = false;
                  this.isesignverified = false;
                }
              }));
            });
          }
        } else {
          this.esignEnabled = false;
        }
        this.populateRecipients();
      });
    this.coreService.progress = {busy: subscription, message: ''};
    this.addToSubscriptions(subscription);
    //this.getWorkitemProgress();
    setTimeout(() => {
      this.workflowService.updateInboxCount();
    }, 2000);

  }

  populateRecipients() {
    this.toRecipients = [];
    this.ccRecipients = [];
    this.workitem.recipients.map((user, index) => {
      if (user.actionType === 'to' || user.actionType === 'TO' || user.actionType === 'Reply-TO') {
        this.toRecipients.push(user);
      } else if (user.actionType === 'cc' || user.actionType === 'CC' || user.actionType === 'Reply-CC') {
        this.ccRecipients.push(user);
      }
    });
  }

  ngAfterViewInit() {
    $('#workitemProgressTab>p-accordionTab>div>a').attr('href', 'javascript:;');
  }

  getWorkitemHistory() {
    const subscription = this.workflowService.getWorkitemHistory(this.workitem.workitemId)
      .subscribe(data => {
        if (data) {
          this.workitemHistory = data.reverse();
        }
      });
    this.coreService.progress = {busy: subscription, message: ''};
    this.addToSubscriptions(subscription);
  }

  onUpload(event) {
    if ((event.files[0].name).toLowerCase().includes('.jpg')||(event.files[0].name).toLowerCase().includes('.jpeg')
      || (event.files[0].name).toLowerCase().includes('.png') || (event.files[0].name).toLowerCase().includes('.gif')
      || (event.files[0].name).toLowerCase().includes('.pdf') || (event.files[0].name).toLowerCase().includes('.doc')
      || (event.files[0].name).toLowerCase().includes('.zip')|| (event.files[0].name).toLowerCase().includes('.tiff')
      || (event.files[0].name).toLowerCase().includes('.docx') || (event.files[0].name).toLowerCase().includes('.xls')
      || (event.files[0].name).toLowerCase().includes('.xlsx')
      || (event.files[0].name).toLowerCase().includes('.ppt') || (event.files[0].name).toLowerCase().includes('.pptx')) {
      this.fileUploaded = event.files[0];
      if (this.fileUploaded !== undefined && this.entryTemp) {
        this.fileselected = true;
      } else {
        this.fileselected = false;
      }
    }

  }

  openDocInfo(doc) {
    this.docInfo = [];
    this.ds.getDocumentInfo(doc.docId).subscribe(data => this.validDoc(data), error => this.noDocFound(doc))

  }

  noWhitespaceValidator(control: FormControl) {
    let isWhitespace = (control.value || '').trim().length === 0;
    let isValid = !isWhitespace;
    return isValid ? null : {'whitespace': true}
  }

  validDoc(data) {
    this.displayinfo = true;
    this.subscription.push(this.ds.getDocument(data.id)
      .subscribe(data => this.assignDocInfo(data)));
    this.subscription.push(this.ds.getDocumentVersions(data.id)
      .subscribe(data => this.assignDocVersions(data)));
    this.subscription.push(this.ds.getDocumentPermissions(data.id)
      .subscribe(data => this.assignDocSecurity(data)));
    this.subscription.push(this.ds.getLinks(data.id)
      .subscribe(data => this.assignDocLink(data)));
    this.subscription.push(this.ds.getDocumentHistory(data.id)
      .subscribe(data => this.assignDocHistory(data)));
    this.subscription.push(this.ds.getDocumentFolders(data.id)
      .subscribe(data => this.assignDocumentFolders(data)));
    this.subscription.push(this.ds.getDocumentWorkflowHistory(data.id)
      .subscribe(data => this.assignDocumentWorkflowHistory(data)));
  }

  openEditDoc(doc) {
    this.busyModel = this.ds.getDocument(doc.docId)
      .subscribe(data => {
        this.editAttachment = true;
        this.fileUploaded = undefined;
        if (data.entryTemplate) {
          this.assignFieldsForEditDoc(data);
        } else {
          this.entryTemp = false;
        }
      }, err => {
        if (err.statusText === 'OK') {
          this.growlService.showGrowl({
            severity: 'error',
            summary: 'Invalid Document', detail: 'This document is either deleted or you dont have permission'
          });
          this.editAttachment = false;
        }
      });
    this.addToSubscriptions(this.busyModel);
  }

  getWorkitemProgress($event?) {
    if ($event) {
      $event.stopPropagation();
    }
    this.busyModel = this.workflowService.getWorkitemProgress(this.workitem.workitemId).subscribe(res => {
      res.map(r => {
        if (r.empNo === this.currentUser.EmpNo) {
          r.from = true;
        }
      });
      this.workitemProgress = res;
    });
    this.addToSubscriptions(this.busyModel);
    this.displayProgress = true;
  }

  addWorkitemProgress(event) {
    this.busyModel = this.workflowService.addWorkitemProgress(event.message, this.currentUser.EmpNo, this.workitem.workitemId)
      .subscribe(res => {
        this.growlService.showGrowl({
          severity: 'info',
          summary: 'Success', detail: 'Workitem Progress Added Successfully'
        });
        event.message = undefined;
        this.getWorkitemProgress();
      }, err => {

      });
    this.addToSubscriptions(this.busyModel);
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
    this.busyModel = this.workflowService.removeWorkitemProgress(id).subscribe(res => {
      this.growlService.showGrowl({
        severity: 'info',
        summary: 'Success', detail: 'Workitem Progress Removed Successfully'
      });
      this.getWorkitemProgress();

    }, err => {
    });
    this.addToSubscriptions(this.busyModel);
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

  assignFieldsForEditDoc(data) {
    this.saveDocInfo = data;
    this.busyModel = this.cs.getEntryTemplate(data.entryTemplate).subscribe(data1 => {
      this.entryTemp = true;
      this.docTemplateDetails = data1;
      this.docTemplateDetails.props.forEach(control => {
        // if (control.hidden === 'false'){
        if (control.req === 'true') {
          if (control.dtype === 'DATE') {
            this.docEditPropForm.addControl(control.symName, new FormControl(null, Validators.required));
          } else {
            this.docEditPropForm.addControl(control.symName, new FormControl(null, [Validators.required, this.noWhitespaceValidator]));

          }
        } else {
          this.docEditPropForm.addControl(control.symName, new FormControl(null, Validators.maxLength(200)));
        }
        if (control.symName === 'OrgCode') {
          if (control.lookups) {
            const removables = [];
            control.lookups.map((d, i) => {
              if (d.label.trim().length > 4) {
                removables.push(i);

              }
            });
            removables.map((d, i) => {
              control.lookups.splice(d - i, 1);
            });
          }
        }
        // }
      });
      for (const prop of this.saveDocInfo.props) {
        // if (prop.hidden === 'false') {
        if (prop.dtype === 'DATE' && prop.mvalues[0] !== null) {
          this.docEditPropForm.get(prop.symName).setValue(prop.mvalues[0]);
        } else {
          this.docEditPropForm.get(prop.symName).setValue(prop.mvalues[0]);
        }
        // }
      }
    });
    this.addToSubscriptions(this.busyModel);
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
    } else {
      this.noLink = true;
    }
  }

  assignDocHistory(data) {
    this.docHistory = data;
  }

  assignDocumentWorkflowHistory(data) {
    this.docTrack = data;
  }

  closeModal() {
    this.docSysProp = [];
  }

  closeEditAttModal() {
    this.docEditPropForm.reset();
    this.saveDocInfo = null;
    this.fileselected = false;
    this.entryTemp = false;
    this.fileUploaded = undefined;
    this.updateddDocuments = new FormData();
  }

  cancel() {
    this.closeEditAttModal();
    this.editAttachment = false;
  }

  removeSelectedFile() {
    this.fileselected = false;
    this.fileUploaded = undefined;
  }

  viewAttachmentLink(doc: any, flag) {
    this.ds.getDocumentInfo(doc.docId).subscribe(data => this.assignDocIdForView(data), err => this.noDocFound(doc));
  }

  assignDocIdForView(data) {
    this.displayIframe = true;
    this.current_url = this.transform(this.ds.getViewUrl(data.id));
  }

  noDocFound(doc) {
    this.growlService.showGrowl({
      severity: 'error',
      summary: 'Invalid Document', detail: 'This document is either deleted or you dont have permission'
    });
    this.workitem.attachments.map((d, i) => {
      if (doc.docId === d.docId) {
        this.strikeIndex = i;
      }
    });
  }

  confirmRemoveLink(docLink) {
    this.confirmationService.confirm({
      message: 'Are you sure that you want to perform this action?',
      key: 'confirmRemoveLink',
      accept: () => {
        this.removeLink(docLink)
      }
    });
  }

  closeViewer(event) {
    this.displayIframe = false;
  }

  transform(url) {
    return this.sanitizer.bypassSecurityTrustResourceUrl(url);
  }

  downloadDoc(doc) {
    this.ds.getDocumentInfo(doc.docId).subscribe(data => this.assignDocIdForDownload(data), err => this.noDocFound(doc));

  }

  assignDocIdForDownload(data) {
    window.location.assign(this.ds.downloadDocument(data.id));
  }

  eSign(doc) {
    if (this.workitem.actions === 'Signature') {
      const subscription = this.ds.verifyESign(doc.docId, this.workitem.workitemId).subscribe(res => {
        if (res && res === 'True') {
          this.growlService.showGrowl({
            severity: 'error',
            summary: 'Warning', detail: 'You Have Already Signed this Document'
          });
          this.ESignedAttachments[doc.docId] = true;
          this.isesignverified = true;
        } else if (res && res === 'False') {
          this.openESignPage(doc);
          this.eSignDialog = true;
          this.isesignverified = false;
          this.pollingData = this.ds.verifyESignInInterval(doc.docId, this.workitem.workitemId).subscribe((data) => {
            if (data && data === 'True') {
              this.pollingData.unsubscribe();
              this.isesignverified = true;
              this.ESignedAttachments[doc.docId] = true;
              let senderName;
              if (this.workitem.senderRoleName) {
                senderName = this.workitem.senderRoleName;
              } else {
                senderName = this.workitem.senderName;
              }
              this.confirmationService.confirm({
                message: 'eSign successful would you like to reply ' + senderName,
                header: 'eSign Confirmation',
                icon: 'ui-icon-help',
                accept: () => {
                  this.replyWorkitem(this.workitem);
                  this.eSignDialog = false;
                },
                reject: () => {
                  this.eSignDialog = false;
                }
              });
            }
            //this.coreService.progress = {busy: this.pollingData, message: ''};
            this.addToSubscriptions(this.pollingData);
          });

        }
      });
      this.coreService.progress = {busy: subscription, message: ''};
      this.addToSubscriptions(subscription);
    } else if (this.workitem.actions === 'Initial') {
      this.openESignPage(doc);
    }
  }

  openESignPage(doc) {
    console.log('opening esign window');
    this.empNo = this.currentUser.EmpNo;
    if (this.workitem.recipientRoleId !== 0) {
      this.roleId = this.workitem.recipientRoleId;
    } else {
      this.roleId = 0;
    }
    this.docId = doc.docId;
    if (this.workitem.actions === 'Initial') {
      this.flagInitial = 'Y';
    } else {
      this.flagInitial = 'N';
    }
    //  var jspPage = document.getElementById("esignId") as HTMLImageElement;
    const sysDateTime = new Date();
    const fulldatetime = sysDateTime.getTime();
    const browser = navigator.appName;
    if (browser === 'Microsoft Internet Explorer') {
      window.opener = self;
    }
    let esign_Url;
    if (this.workitem.actions === 'Initial') {
      esign_Url = `${global.esign_url}eInitial.jsp?empno=${this.currentUser.KocId}&witemid=${this.workitem.workitemId}&roleid=${this.roleId}&docid=${this.docId}&initial=${this.flagInitial}&sysdatetime=${fulldatetime}`
    } else {
      esign_Url = `${global.esign_url}eSign.jsp?empno=${this.currentUser.KocId}&witemid=${this.workitem.workitemId}&roleid=${this.roleId}&docid=${this.docId}&initial=${this.flagInitial}&sysdatetime=${fulldatetime}`
    }
    window.open(esign_Url, "", "menubar=0,location='',toolbar=0,scrollbars=yes,resizable=yes,top=100,left=400,width=800,height=600");
  }

  canceleSign() {
    this.eSignDialog = false;
    if (this.workitem.actions === 'Signature') {
      this.pollingData.unsubscribe();
    }
  }

  updatedAttachment() {
    this.updateddDocuments = new FormData();
    this.updateddDocuments.append('document', this.fileUploaded);
    this.busyModel = this.ds.checkOut(this.saveDocInfo.id)
      .subscribe(data => this.checkoutSuccess(data), Error => this.updateFailed(Error));
    this.addToSubscriptions(this.busyModel);
    for (const prop of this.saveDocInfo.props) {
      if (prop.dtype === 'DATE') {
        if (this.docEditPropForm.get(prop.symName).value !== null) {
          prop.mvalues = [this.getFormatedDate(this.docEditPropForm.get(prop.symName).value)];
        }
      } else if (prop.dtype !== 'DATE') {
        prop.mvalues = [this.docEditPropForm.get(prop.symName).value];
      }
    }
    this.saveDocInfo.format = undefined;
    this.updateddDocuments.append('DocInfo', JSON.stringify(this.saveDocInfo));
  }

  getFormatedDate(value) {
    const date = new Date(value);
    return date.getDate() + '/' + (date.getMonth() + 1) + '/' + date.getFullYear();
  }

  checkoutSuccess(data) {
    this.busyModel = this.ds.checkIn(this.updateddDocuments)
      .subscribe(data1 => this.updateSuccess(data1), Error => this.updateFailed(Error));
    this.addToSubscriptions(this.busyModel);
  }

  updateSuccess(data) {
    this.docSysProp = [];
    if (data) {
      this.growlService.showGrowl({
        severity: 'info',
        summary: 'Success', detail: 'Updated Successfully'
      });
      this.editAttachment = false;
      this.closeEditAttModal();
    } else {
      this.updateFailed('error');
    }
    this.fileselected = false;
    this.ngOnInit();
  }

  updateFailed(error) {
    this.errorJson = JSON.parse(error.error).responseMessage;
    this.growlService.showGrowl({
      severity: 'error',
      summary: 'Failure', detail: this.errorJson
    });
  }

  updateEdits() {
    for (const prop of this.saveDocInfo.props) {
      if (prop.dtype === 'DATE') {
        if (this.docEditPropForm.get(prop.symName).value !== null) {
          prop.mvalues = [this.getFormatedDate(this.docEditPropForm.get(prop.symName).value)];
        }
      } else if (prop.dtype !== 'DATE') {
        prop.mvalues = [this.docEditPropForm.get(prop.symName).value];
      }
    }
    this.busyModel = this.ds.updateProperties(this.saveDocInfo).subscribe(data => this.updateSuccess(data));
    this.addToSubscriptions(this.busyModel);
  }

  // editSuccess(data) {
  //   this.docSysProp = [];
  //   if (data) {
  //     this.growlService.showGrowl({
  //       severity: 'info',
  //       summary: 'Success', detail: 'Edited Successful'
  //     });
  //     this.editProperties = false;
  //   } else {
  //     this.editFailed('error');
  //   }
  //   this.ngOnInit();
  // }

  // editFailed(error) {
  //   this.errorJson=JSON.parse(error.error).responseMessage;
  //   this.growlService.showGrowl({
  //     severity: 'error',
  //     summary: 'Failure', detail: this.errorJson
  //   });
  // }

  recallWorkitem(event) {
    this.confirmationService.confirm({
      message: 'Do you want to Recall this workitem?',
      header: 'Recall Confirmation',
      icon: 'ui-icon-help',
      accept: () => {
        if (this.workflowService.delegateId !== undefined) {
          this.us.validateDelegation(this.workflowService.delegateId).subscribe(res => {
            if (res === 'INACTIVE') {
              this.showDelegationInactiveDialog = true;
            } else {
              this.recallmodel.empNo = this.currentUser.EmpNo;
              if (this.currentUser.roles.length > 0) {
                this.recallmodel.roleId = this.currentUser.roles[0].id;
              }
              this.recallmodel.items[0] = this.workitem.workitemId;
              const subscription = this.workflowService.recallWorkitem(this.recallmodel)
                .subscribe(data => {
                  this.growlService.showGrowl({
                    severity: 'info',
                    summary: 'Success', detail: 'Recalled Successfully'
                  });
                  this.router.navigateByUrl('/workflow/' + this.fromPage[0]);
                });
              this.coreService.progress = {busy: subscription, message: ''};
              this.addToSubscriptions(subscription);
            }
          });
        } else {
          this.recallmodel.empNo = this.currentUser.EmpNo;
          if (this.currentUser.roles.length > 0) {
            this.recallmodel.roleId = this.currentUser.roles[0].id;
          }
          this.recallmodel.items[0] = this.workitem.workitemId;
          const subscription = this.workflowService.recallWorkitem(this.recallmodel)
            .subscribe(data => {
              this.growlService.showGrowl({
                severity: 'info',
                summary: 'Success', detail: 'Recalled Successfully'
              });
              this.router.navigateByUrl('/workflow/' + this.fromPage[0]);
            });
          this.coreService.progress = {busy: subscription, message: ''};
          this.addToSubscriptions(subscription);
        }
      },
      reject: () => {
      }
    });
  }

  finishWorkitem(event) {
    this.confirmationService.confirm({
      message: 'Do you want to Finish this workitem?',
      header: 'Finish Confirmation',
      accept: () => {
        if (this.workflowService.delegateId !== undefined) {
          this.us.validateDelegation(this.workflowService.delegateId).subscribe(res => {
            if (res === 'INACTIVE') {
              this.showDelegationInactiveDialog = true;
            } else {
              this.workflowService.validateWorkitem(this.workitem.workitemId).subscribe(res1 => {
                if (res1 === 'INACTIVE') {
                  this.showRecallInactiveDialog = true;
                } else {
                  const subscription = this.workflowService.finishWorkitem(this.workitem.workitemId).subscribe(data => {
                    this.growlService.showGrowl({
                      severity: 'info',
                      summary: 'Success', detail: 'Finished Successfully'
                    });
                    this.router.navigateByUrl('/workflow/' + this.fromPage[0]);
                  });
                  this.coreService.progress = {busy: subscription, message: ''};
                  this.addToSubscriptions(subscription);
                }
              });
            }
          });
        } else {
          this.workflowService.validateWorkitem(this.workitem.workitemId).subscribe(res => {
            if (res === 'INACTIVE') {
              this.showRecallInactiveDialog = true;
            } else {
              const subscription = this.workflowService.finishWorkitem(this.workitem.workitemId).subscribe(data => {
                this.growlService.showGrowl({
                  severity: 'info',
                  summary: 'Success', detail: 'Finished Successfully'
                });
                this.router.navigateByUrl('/workflow/' + this.fromPage[0]);
              });
              this.coreService.progress = {busy: subscription, message: ''};
              this.addToSubscriptions(subscription);

            }
          });
        }
      },
      reject: () => {
      }
    });
  }

  archiveWorkitem(event) {
    this.confirmationService.confirm({
      message: 'Do you want to Archive this workitem?',
      header: 'Archive Confirmation',
      icon: 'ui-icon-help',
      accept: () => {
        const subscription = this.workflowService.archiveWorkitem(this.workitem.workitemId)
          .subscribe(data => {
            this.growlService.showGrowl({
              severity: 'info',
              summary: 'Success', detail: 'Archived Successfully'
            });
            if (this.fromPage[0] === 'inbox') {
              const selectedInboxTab = this.workflowService.inboxSelectedUserTab.split('@');
              const inboxTabIndex = parseInt(selectedInboxTab[0], 10);
              this.workflowService.archiveSelectedUserTab = inboxTabIndex * 2 + '@' + selectedInboxTab[1] + 'Inbox';
            } else if (this.fromPage[0] === 'sent') {
              const selectedSentTab = this.workflowService.sentSelectedUserTab.split('@');
              const sentTabIndex = parseInt(selectedSentTab[0], 10);
              this.workflowService.archiveSelectedUserTab = (sentTabIndex * 2) + 1 + '@' + selectedSentTab[1] + 'Sent';
            }
            this.router.navigateByUrl('/workflow/archive');
          });
        this.coreService.progress = {busy: subscription, message: ''};
        this.addToSubscriptions(subscription);
      },
      reject: () => {
      }
    });
  }

  adduserWorkitem() {
    const wia = new WorkItemAction();
    wia.actions = this.workitem.actions;
    // wia.actionDetails = this.workitem.actionName
    wia.attachments = this.workitem.attachments;
    wia.deadline = this.workitem.deadline;
    wia.id = this.workitem.workitemId;
    wia.instructions = this.workitem.instructions;
    wia.recipients = this.workitem.recipients;
    if (this.addUser.recipients.toList.length > 0) {
      for (const toUser of this.addUser.recipients.toList) {
        const user = new Recipients();
        user.name = toUser.name;
        user.actionType = toUser.actionType;
        user.userType = toUser.userType;
        if (toUser.userType === 'USER') {
          user.id = toUser.EmpNo;
        } else if (toUser.userType === 'ROLE') {
          user.id = toUser.id;
        }
        if (!this.alreadyExistInRecp(this.workitem.recipients, user)) {
          wia.recipients.push(user);
        }
      }
    }
    if (this.addUser.recipients.ccList.length > 0) {
      for (const ccUser of this.addUser.recipients.ccList) {
        const user = new Recipients();
        user.name = ccUser.name;
        user.actionType = ccUser.actionType;
        user.userType = ccUser.userType;
        if (ccUser.userType === 'USER') {
          user.id = ccUser.EmpNo;
        } else if (ccUser.userType === 'ROLE') {
          user.id = ccUser.id;
        }
        if (!this.alreadyExistInRecp(this.workitem.recipients, user)) {
          wia.recipients.push(user);
        }
      }
    }

    wia.reminder = this.workitem.reminder;
    wia.EMPNo = this.currentUser.EmpNo;
    if (this.currentUser.roles.length > 0) {
      wia.roleId = this.currentUser.roles[0].id
    } else {
      wia.roleId = 0
    }
    // wia.wiAction = this.workitem.actionId
    const subscription = this.workflowService.addUserWorkitem(wia)
      .subscribe(data => {
        this.growlService.showGrowl({
          severity: 'info',
          summary: 'Success', detail: 'Added User Successfully'
        });
        this.populateRecipients();
        this.getWorkitemHistory();
      });
    this.coreService.progress = {busy: subscription, message: ''};
    this.addToSubscriptions(subscription);
    this.AddUserDialog = false;
    this.addUser.recipients.toList = [];
    this.addUser.recipients.ccList = [];
  }

  alreadyExistInRecp(recp, newUser) {
    let exist = false;
    recp.map((rec, index) => {
      if (rec.name === newUser.name) {
        exist = true;
      }
    });
    return exist
  }

  initAdduser() {
    this.addUser.documents.existing = {
      model: {
        contentSearch: {name: "Content", symName: "CONTENT", dtype: "STRING", mvalues: []},
        actionType: 'Default'
      }
    };
    this.addUser.recipients.roles = {
      selectCriterions: [{label: 'Title', value: 'NAME'},
        {label: 'Org Code', value: 'ORGCODE'}], result: [], model: {selectedCriterion: 'NAME'}
    };
    this.addUser.recipients.roles.roleTree = [];
    this.addUser.recipients.search = {
      result: [],
      searchCriterions: [{label: 'Name', value: 'NAME'}, {label: 'Email', value: 'EMAIL'},
        {label: 'Designation', value: 'TITLE'}, {label: 'Phone', value: 'PHONE'}, {label: 'Org Code', value: 'ORGCODE'},
        {label: 'KOC No', value: 'KOCNO'}], model: {searchCriterion: 'NAME'}
    };
    this.addUser.recipients.list = {userList: [], selectedUserList: {}, subLists: []};
  }

  prepareAdduser(event) {
    if (this.workflowService.delegateId !== undefined) {
      this.us.validateDelegation(this.workflowService.delegateId).subscribe(res => {
        if (res === 'INACTIVE') {
          this.showDelegationInactiveDialog = true;
        } else {
          this.workflowService.validateWorkitem(this.workitem.workitemId).subscribe(res1 => {
            if (res1 === 'INACTIVE') {
              this.showRecallInactiveDialog = true;
            } else {
              this.getOrgRole(true);
              this.getUserLists();
            }
          });
        }
      });
    } else {
      this.workflowService.validateWorkitem(this.workitem.workitemId).subscribe(res => {
        if (res === 'INACTIVE') {
          this.showRecallInactiveDialog = true;
        } else {
          this.getOrgRole(true);
          this.getUserLists();
        }
      });
    }
  }

  prepareStepItems() {
    this.userExist(this.addUser.recipients);
  }

  closeAddUserModel() {
    this.addUser.recipients.toList = [];
    this.addUser.recipients.ccList = [];
  }

  cancelAddUserModel() {
    this.AddUserDialog = false;
    this.closeEditAttModal();
  }

  getOrgRole(init) {
    this.tmpRoleTree = [];
    this.addUser.recipients.roles.roleTree = [];
    const subscription = this.us.getTopRolesList().subscribe(res => {
      res.map((head) => {
        this.tmpRoleTree.push({
          label: head.headRoleName,
          data: head,
          expandedIcon: this.roleTreeExpandedIcon,
          collapsedIcon: this.roleTreeCollapsedIcon,
          leaf: false,
          expanded: false
        });
      });
      //this.getSubOrgRoles(this.tmpRoleTree[0], init);
      this.addUser.recipients.roles.roleTree = this.tmpRoleTree;
    }, err => {

    });
    this.addToSubscriptions(subscription);
  }

  getUserLists() {
    this.distList.lists = [];
    const subscription = this.us.getUserLists(true).subscribe(res => {
      const remainings = [];
      res.map((l, i) => {
        if (l.id > 1 && l.isGlobal === 'N') {
          this.distList.lists.push(l);
        } else if (l.id > 1 && l.isGlobal === 'Y') {
          this.globalList.lists.push(l);
        } else {
          remainings.push(l);
        }
      });
      this.addUser.recipients.list.userList = remainings;
      this.addUser.recipients.list.userList.push(this.defaultList);
      this.addUser.recipients.list.userList.push(this.distList);
      this.addUser.recipients.list.userList.push(this.globalList);
    }, err => {

    });
    this.addToSubscriptions(subscription);
  }

  getSubOrgRoles(parent, init) {
    const subscription = this.us.getSubRolesList(parent.data.id).subscribe(res => {
      parent.children = [];
      res.map(d => {
        parent.children.push({
          label: d.headRoleName,
          data: d,
          expandedIcon: this.roleTreeExpandedIcon,
          collapsedIcon: this.roleTreeCollapsedIcon,
          leaf: false
        });
      });
      if (init) {
        this.getUserSupervisorTree(this.tmpRoleTree);
      }
    }, err => {

    });
    this.addToSubscriptions(subscription);
  }

  getUserSupervisorTree(tmpRoleTree) {
    const subscription = this.us.getUserSupervisorTree(this.currentUser.EmpNo).subscribe(res => {
      if (res.length > 1) {
        this.setChildren(this.tmpRoleTree[0], res, 1);
      }
      else {
        this.addUser.recipients.roles.roleTree = tmpRoleTree;
      }
    }, err => {

    });
    this.addToSubscriptions(subscription);
  }

  setChildren(parent, response, index) {
    let newParent;
    if (!parent.children) {
      parent.children = [];
      parent.children.push({
        label: response[index].headRoleName, data: response[index], expandedIcon: this.roleTreeExpandedIcon,
        collapsedIcon: this.roleTreeCollapsedIcon, leaf: false, expanded: true
      });
      newParent = parent.children[0];
    } else {
      parent.children.map(c => {
        if (c.data.id === response[index].id) {
          c.expanded = true;
          newParent = c;
        }
      })
    }

    if (index < response.length - 1) {
      this.setChildren(newParent, response, index + 1);
    } else {
      this.addUser.recipients.roles.roleTree = this.tmpRoleTree;
    }

  }

  forwardWorkitem(event) {
    if (this.workflowService.delegateId !== undefined) {
      this.us.validateDelegation(this.workflowService.delegateId).subscribe(res => {
        if (res === 'INACTIVE') {
          this.showDelegationInactiveDialog = true;
        } else {
          this.workflowService.validateWorkitem(this.workitem.workitemId).subscribe(res1 => {
            if (res1 === 'INACTIVE') {
              this.showRecallInactiveDialog = true;
            } else {
              this.router.navigate(['/workflow/launch', 'forward', {id: this.workitem.workitemId}]);
            }
          });
        }
      });
    } else {
      this.workflowService.validateWorkitem(this.workitem.workitemId).subscribe(res => {
        if (res === 'INACTIVE') {
          this.showRecallInactiveDialog = true;
        } else {
          this.router.navigate(['/workflow/launch', 'forward', {id: this.workitem.workitemId}]);
        }
      });
    }
  }

  relaunchWorkItem(event) {
    if (this.workflowService.delegateId !== undefined) {
      this.us.validateDelegation(this.workflowService.delegateId).subscribe(res => {
        if (res === 'INACTIVE') {
          this.showDelegationInactiveDialog = true;
        } else {
          this.workflowService.validateWorkitem(this.workitem.workitemId).subscribe(res1 => {
            if (res1 === 'INACTIVE') {
              this.showRecallInactiveDialog = true;
            } else {
              this.router.navigate(['/workflow/launch', 'reLaunch', {id: this.workitem.workitemId}]);
            }
          });
        }
      });
    } else {
      this.workflowService.validateWorkitem(this.workitem.workitemId).subscribe(res => {
        if (res === 'INACTIVE') {
          this.showRecallInactiveDialog = true;
        } else {
          this.router.navigate(['/workflow/launch', 'reLaunch', {id: this.workitem.workitemId}]);
        }
      });
    }
  }

  replyAllWorkitem(event) {
    if (this.workflowService.delegateId !== undefined) {
      this.us.validateDelegation(this.workflowService.delegateId).subscribe(res => {
        if (res === 'INACTIVE') {
          this.showDelegationInactiveDialog = true;
        } else {
          this.workflowService.validateWorkitem(this.workitem.workitemId).subscribe(res1 => {
            if (res1 === 'INACTIVE') {
              this.showRecallInactiveDialog = true;
            } else {
              this.router.navigate(['/workflow/launch', 'replyAll', {id: this.workitem.workitemId}]);
            }
          });
        }
      });
    } else {
      this.workflowService.validateWorkitem(this.workitem.workitemId).subscribe(res => {
        if (res === 'INACTIVE') {
          this.showRecallInactiveDialog = true;
        } else {
          this.router.navigate(['/workflow/launch', 'replyAll', {id: this.workitem.workitemId}]);
        }
      });
    }


  }

  replyWorkitem(event) {
    if (this.workflowService.delegateId !== undefined) {
      this.us.validateDelegation(this.workflowService.delegateId).subscribe(res => {
        if (res === 'INACTIVE') {
          this.showDelegationInactiveDialog = true;
        } else {
          this.workflowService.validateWorkitem(this.workitem.workitemId).subscribe(res1 => {
            if (res1 === 'INACTIVE') {
              this.showRecallInactiveDialog = true;
            } else {
              this.router.navigate(['/workflow/launch', 'reply', {id: this.workitem.workitemId}]);
            }
          });
        }
      });
    } else {
      this.workflowService.validateWorkitem(this.workitem.workitemId).subscribe(res => {
        if (res === 'INACTIVE') {
          this.showRecallInactiveDialog = true;
        } else {
          this.router.navigate(['/workflow/launch', 'reply', {id: this.workitem.workitemId}]);
        }
      });
    }


  }

  previousPage(workitem) {
    this.workflowService.validateWorkitem(workitem.workitemId).subscribe(res => {
      if (res === 'INACTIVE') {
        this.showRecallInactiveDialog = true;
      } else {
        this.router.navigateByUrl('/workflow/' + this.fromPage[0]);
        this.workflowService.updateInboxCount();
      }
    });

  }

  assignDocumentFolders(data) {
    this.foldersFiledIn = data;
  }

  removeLink(doc) {
    this.headId = doc.head;
    this.busyModel = this.ds.removeLink(doc.head, doc.tail)
      .subscribe(data => this.successremoveLink());
    this.addToSubscriptions(this.busyModel);
  }

  successremoveLink() {
    this.busyModel = this.ds.getLinks(this.headId)
      .subscribe(data => this.assignDocLink(data));
    this.addToSubscriptions(this.busyModel);
  }

  showTrackWorkitem(event) {
    if (event.data.details !== 'Launch') {
      const subscription = this.workflowService.getWorkitem(event.data.workitemId, this.currentUser.EmpNo)
        .subscribe(data => this.trackWorkitemDetails = data);
      this.coreService.progress = {busy: subscription, message: ''};
      this.addToSubscriptions(subscription);
    }
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
      this.busyModel = this.ds.getDocument(version.id).subscribe(res => {
        version.props = res.props;
        version.props.map(p => {
          props.push({prop: p.desc, value: p.mvalues[0]})
        });
        this.selectedVersion.props = props;
      });
      this.addToSubscriptions(this.busyModel);
    }
  }

  userExist(recipients) {
    let exist = false;
    if (recipients.toList.length > 0 || recipients.ccList.length > 0) {
      this.workitem.recipients.map((recUser) => {
        recipients.toList.map((toUser, index) => {
          if (recUser.name === toUser.name) {
            recipients.toList.splice(index, 1);
            exist = true;
          }
        });
        recipients.ccList.map((ccUser, index) => {
          if (recUser.name === ccUser.name) {
            recipients.ccList.splice(index, 1);
            exist = true;
          }
        });
      });
    }
    if (exist) {
      this.growlService.showGrowl({
        severity: 'error',
        summary: 'Failure', detail: 'User Already Exist'
      });
    }
  }

  getRoleMembers(role) {
    if (!role.members) {
      let RoleNameString = '';
      const subscription = this.us.getRoleMembers(role.id).subscribe((res: any) => {
        for (const RName of res) {
          if (RName.name !== undefined) {
            RoleNameString = RoleNameString + '\n' + RName.name;
          }
        }
        role.members = RoleNameString.slice(1);

      }, err => {

      });
      this.addToSubscriptions(subscription);
    }
  }

  addToSubscriptions(subscription) {
    this.subscription.push(subscription);
  }

  reloadApp() {
    this.showDelegationInactiveDialog = false;
    this.showRecallInactiveDialog = false;
    this.router.navigate(['/workflow']);
  }

  openDocInValidate(id) {
    window.open(this.ds.validateDocument(id));
  }

  ngOnDestroy() {
    for (let subs of this.subscription) {
      subs.unsubscribe();
    }
    this.subscription = null;
    this.esignEnabled = false;
    this.attach_url = null;
    this.current_url = null;
    this.displayIframe = false;
    this.workitemHistory = [];
    this.workitem = null;
    this.empNo = null;
    this.roleId = null;
    this.flagInitial = null;
    this.docId = null;
    this.docInfo = [];
    this.docVersion = [];
    this.docHistory = [];
    this.linkedDocuments = [];
    this.docSecurity = [];
    this.docSysProp = null;
    this.noLink = false;
    this.saveDocInfo = null;
    this.fileselected = false;
    this.fileUploaded = null;
    this.trackWorkitemDetails = undefined;
    this.displayProgress = false;
    if (this.busyModel) {
      this.busyModel.unsubscribe();
    }
    this.ESignedAttachments = [];
    if (this.pollingData) {
      this.pollingData.unsubscribe();
    }
    this.eSignDialog = false;
    this.showDelegationInactiveDialog = false;
    this.showRecallInactiveDialog = false;
    this.isesignverified = false;
  }
}
