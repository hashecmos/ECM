import {Component, EventEmitter, Input, OnDestroy, OnInit, Output} from '@angular/core';
import * as $ from 'jquery';
import {BrowserEvents} from '../../../services/browser-events.service';
import {DocumentService} from '../../../services/document.service';
import {Subscription} from 'rxjs/Subscription';
import {User} from '../../../models/user/user.model';
import {UserService} from '../../../services/user.service';
import {ConfirmationService, Message, SelectItem, TreeNode} from 'primeng/primeng';
import * as global from '../../../global.variables';
import {Router} from '@angular/router';
import {ContentService} from '../../../services/content-service.service';
import {FormControl, FormGroup, Validators} from '@angular/forms';
import {EntryTemplateDetails} from '../../../models/document/entry-template-details.model';
import {DocumentInfoModel} from '../../../models/document/document-info.model';
import {GrowlService} from '../../../services/growl.service';
import 'rxjs/Rx' ;
import {saveAs} from 'file-saver';
import {AccessPolicyService} from "../../../services/access-policy.service";
import {CoreService} from "../../../services/core.service";
import {AdminService} from "../../../services/admin.service";

@Component({
  selector: 'app-rightpanel',
  templateUrl: './rightpanel.component.html',
  styleUrls: ['./rightpanel.component.css'],
})
export class RightpanelComponent implements OnInit, OnDestroy {
  @Input() public currentScreen: any;
  @Output() sendUpdate = new EventEmitter();
  @Output() sendFolders = new EventEmitter();
  @Output() sendMoveToFolder = new EventEmitter();
  @Output() refreshScreen = new EventEmitter();
  @Output() togglePanel = new EventEmitter();
  public saveDocInfo = new DocumentInfoModel();
  public docTemplateDetails = new EntryTemplateDetails();
  public docEditPropForm: FormGroup;
  public allowDownloads = false;
  public allowCheckin = false;
  public allowLaunch = false;
  public allowRemoveFav = false;
  public allowEmail = false;
  public allowLinks = false;
  public allowEditProp = false;
  private subscriptions: Subscription[] = [];
  private showFileIn = false;
  public showMove = false;
  selectedDocs: any[];
  msgs: Message[] = [];
  public user = new User();
  private base_url: string;
  clickedFolder: any;
  folderId: any;
  folderList: TreeNode[];
  public entryTemp = false;
  public update = false;
  public fileselected = false;
  private dateselected: Object = {};
  private updateddDocuments = new FormData();
  public uploadedFile;
  public docSysProp: any;
  public edit = false;
  public editAttachment: boolean;
  public busyModal: Subscription;
  public errorJson: any;
  removeFolderList: TreeNode[];
  moveFolderList: TreeNode[];
  selectedRemoveFolder: any;
  selectedMoveFolder: TreeNode;
  showRemove = false;
  showMoveFrom = false;
  public ecmNo;
  accessPolicies: any[];
  showPermissionDialogue = false;
  selectedPolicy: any = {id: undefined, permissions: []};
  newPermissions: any[];
  searchText: any;
  accessLevelsMap = {
    'Full Control': 983511,
    'Owner': 393687,
    'Author': 131543,
    'Viewer': 131201,
    'Custom': 131201
  };
  private tempPermissions: any[];
  private disableAddNewPermission = false;

  constructor(private bs: BrowserEvents, private ds: DocumentService, private us: UserService,
              private router: Router, private cs: ContentService, private accessPolicyService: AccessPolicyService,
              private growlService: GrowlService, private confirmationService: ConfirmationService, private as: AdminService,
              private documentService: DocumentService, private coreService: CoreService) {
    this.user = this.us.getCurrentUser();
    this.base_url = global.base_url;
    this.docEditPropForm = new FormGroup({
      DocumentTitle: new FormControl(null, [Validators.required, this.noWhitespaceValidator])
    });
  }

  onModalHide() {
    this.docEditPropForm.reset();
    this.uploadedFile = undefined;
    this.docSysProp = [];
    //this.refresh();

    this.saveDocInfo = null;
    this.fileselected = false;
    this.entryTemp = false;
  }

  cancel() {
    this.onModalHide();
    this.update = false;
  }

  ngOnInit() {
    this.bs.docsSelected.subscribe(data => this.assignDocsSelected(data));
  }

  confirmUnfile() {
    this.confirmationService.confirm({
      message: 'Are you sure that you want to perform this action?',
      key: 'confirmKey',
      accept: () => {
        this.showRemove = true;
        this.removeFolderList = undefined;
        this.ds.getDocumentFolders(this.selectedDocs[0].id).subscribe(data => this.assignDocuments(data))

      }
    });
  }

  assignDocuments(docs) {
    const topFolder = [];
    docs.map((d, i) => {
      if (d != null) {
        if (d.type === 'PermissionsFolder' || d.type === 'PermissionFolder') {
          topFolder.push({
            label: d.name,
            data: d,
            'level': '1',
            'expandedIcon': 'ui-icon-folder-open',
            'collapsedIcon': 'ui-icon-folder-shared',
            'children': [],
            'leaf': true
          });
        }
        else {
          topFolder.push({
            label: d.name,
            data: d,
            'level': '1',
            'expandedIcon': 'ui-icon-folder-open',
            'collapsedIcon': 'ui-icon-folder',
            'children': [],
            'leaf': true
          });

        }
      }
    });
    this.removeFolderList = topFolder;
  }

  removeFromFolder() {
    localStorage.setItem('unfileClicked', 'true');
    this.folderId = localStorage.getItem('folderId');
    for (const doc of this.selectedRemoveFolder) {
      this.cs.unfileFromFolder(doc.data.id, this.selectedDocs[0].id).subscribe(data => this.unfileSuccess(), Error => this.unfileFailed());
    }
    this.showRemove = false;
  }

  assignDocsSelected(data) {
    if( data!==undefined) {
      this.selectedDocs = data;
      //this.ds.getDocumentFolders(data[0].id).subscribe(data=>this.assignDocuments(data))
      if (this.selectedDocs.length > 0) {
        this.allowDownloads = true;
        this.allowLaunch = true;
        this.allowRemoveFav = true;
        this.allowCheckin = true;
        this.allowEmail = true;
        this.allowLinks = false;

      } else {
        this.allowDownloads = true;
        this.allowLaunch = false;
        this.allowCheckin = false;
        this.allowRemoveFav = false;
        this.allowEmail = false;

      }

      if (this.selectedDocs.length > 1) {
        this.allowLinks = true;
        this.allowCheckin = false;
        this.allowEmail = false;
        this.allowEditProp = false;
        this.allowDownloads = true;
      } else if (this.selectedDocs.length === 1) {
        this.allowCheckin = true;
        this.allowLinks = false;
        this.allowEditProp = true;
      } else if (this.selectedDocs.length < 1) {
        this.allowCheckin = false;
        this.allowLinks = false;
        this.allowEditProp = false;
      }
      if (this.selectedDocs.length > 2) {
        this.allowLinks = false;
      }
    }
  }

  download() {
    if (this.selectedDocs.length > 1) {
      const docIDs = [];
      for (const doc of this.selectedDocs) {
        docIDs.push(doc.id)
      }
      this.subscriptions.push(this.ds.downloadMultipleDocument(docIDs).subscribe(res => {
        const file = new Blob([res], {type: 'application/zip'});
        const fileName = 'Documents' + '.zip';
        saveAs(file, fileName);
      }));
    } else {
      //window.location.assign(this.ds.downloadThisDocument(this.selectedDocs[0].id));
      const fileName = this.selectedDocs[0].fileName;
      this.ds.downloadDocumentPanel(this.selectedDocs[0].id).subscribe(res => {
        console.log(decodeURI(fileName));
        const file = new Blob([res], {type: res.type});
        saveAs(file, fileName);
      },err=>{
        console.log(err);
      });
    }
    this.refresh();
  }

  launch() {
    this.confirmForSameDocName(() => {
      this.launch2();
    });
  }

  launch2() {
    let count = 0;
    for (const doc of this.selectedDocs) {
      let subscription = this.ds.addToCart(this.user.EmpNo, doc.id)
        .subscribe(res => {
          count++;
          if (count === this.selectedDocs.length) {
            subscription = this.documentService.getCart(this.user.EmpNo).subscribe(items => {
              this.documentService.refreshCart(items);
              this.bs.skipDocLaunch.emit('skipdoc');
              this.router.navigate(['workflow/launch', {actionType: 'browseLaunch', docId: this.selectedDocs[0].id}]);
            }, err => {

            });
            this.coreService.progress = {busy: subscription, message: '', backdrop: true};
            this.addToSubscriptions(subscription);
          }

        });
      this.coreService.progress = {busy: subscription, message: '', backdrop: true};
      this.addToSubscriptions(subscription);
    }

  }

  confirmForSameDocName(postFn) {
    this.documentService.getCart(this.user.EmpNo).subscribe(docs => {
      let exists = false;
      for (const doc of this.selectedDocs) {
        if (this.documentService.checkForSameNameDoc(docs, doc.fileName, doc.id)) {
          exists = true;
        }
      }

      if (!exists) {
        postFn();
      } else {
        this.confirmationService.confirm({
          message: 'Document with same name already exists in Cart, do you want to continue?',
          key: 'addToCartConfirmation',
          accept: () => {
            postFn();
          }
        });
      }
    })
  }

  addCart() {
    this.confirmForSameDocName(() => {
      this.addCart2();
    });
  }

  addCart2() {
    for (const doc of this.selectedDocs) {
      this.subscriptions.push(this.ds.addToCart(this.user.EmpNo, doc.id)
        .subscribe(res => this.addToCartSuccess(doc.name), error => this.addToCartFailure()));
    }


  }

  addToCartSuccess(name) {
    this.growlService.showGrowl({
      severity: 'info',
      summary: 'Success', detail: 'Added To Cart Successfully'
    });
    this.subscriptions.push(this.ds.getCart(this.user.EmpNo).subscribe((data) => {
      this.documentService.refreshCart(data);
    }));
    this.refresh();

  }

  addToCartFailure() {
    this.growlService.showGrowl({
      severity: 'error',
      summary: 'Failure', detail: 'Add To Cart Failed'
    });
  }

  removeFav() {
    this.sendUpdate.emit(this.selectedDocs);
  }

  refresh() {
    this.refreshScreen.emit(this.selectedDocs);
  }

  mailTo() {
    let title;
    let urls;
    let htmlString = '';
    let subjectString = '';
    this.selectedDocs.map((d, i) => {
      subjectString += d.fileName;
      title = d.fileName;
      urls = this.base_url + 'DocumentService/' + 'downloadDocument?id=' + d.id + "%0D%0A";
      htmlString += "<html><head></head><body><a href='" + urls + "'><span style=color:#336699>" + title + "</span></a></body></html>";
      if (this.selectedDocs.length > i + 1) {
        htmlString += '<br>';
        subjectString += ',';

      }
    });
    let htmlString2 = '<textarea id="textbox" style="width: 300px; height: 600px;">\n' +
      'Subject:' + subjectString + '\n' +
      'X-Unsent: 1\n' +
      'Content-Type: text/html\n' +
      '\n' +
      htmlString +
      '</textarea> <br>';
    var textFile;
    var data = new Blob([htmlString2], {type: 'text/plain'});
    if (textFile !== null) {
      window.URL.revokeObjectURL(textFile);
    }
    textFile = window.URL.createObjectURL(data);
    saveAs(data, "mailto.eml");

  }


  linkDocs() {
    this.subscriptions.push(this.ds.linkDocuments(this.selectedDocs[0].id, this.selectedDocs[1].id)
      .subscribe(data => this.successLink(), error => this.errorLink()));
  }

  successLink() {
    this.growlService.showGrowl({
      severity: 'info',
      summary: 'Success', detail: 'Linked Docs Successfully'
    });
    this.refresh();

  }

  errorLink() {
    this.growlService.showGrowl({
      severity: 'error',
      summary: 'Failure', detail: 'Failed To Link Docs'
    });

  }

  addFavourites() {
    for (const doc of this.selectedDocs) {
      this.subscriptions.push(this.ds.addToFavorites(this.user.EmpNo, doc.id)
        .subscribe(data1 => this.successAdd(), error => this.failureAdd()));
    }
  }

  successAdd() {
    this.growlService.showGrowl({
      severity: 'info',
      summary: 'Success', detail: 'Added To Favourites'
    });
    this.refresh();

  }

  failureAdd() {
    this.growlService.showGrowl({
      severity: 'error',
      summary: 'Failure', detail: 'Failed To Add To Favourites'
    });

  }

  openEditDoc() {
    this.toggleSidePanel();
    this.busyModal = this.ds.getDocument(this.selectedDocs[0].id)
      .subscribe(data => {
        if (data.entryTemplate) {
          this.assignFieldsForEditDoc(data);
        } else {
          this.entryTemp = false;
          this.saveDocInfo = null;
        }
      },err=>{
        if(err.statusText==='OK'){
           this.growlService.showGrowl({
          severity: 'error',
          summary: 'Invalid Document', detail: 'This Document is either deleted or not found'
        });
        }
      });
    this.addToSubscriptions(this.busyModal);
  }

  assignFieldsForEditDoc(data) {
    this.saveDocInfo = data;
    this.busyModal = this.cs.getEntryTemplate(data.entryTemplate).subscribe(data2 => {
      this.entryTemp = true;
      this.docTemplateDetails = data2;
      this.docTemplateDetails.props.forEach(control => {
        if (control.req === 'true') {
          if (control.dtype === 'DATE') {
            this.docEditPropForm.addControl(control.symName, new FormControl(null, Validators.required));
          }else{
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
      });
      for (const prop of this.saveDocInfo.props) {
        if (prop.dtype === 'DATE' && prop.mvalues[0] !== null) {
          this.docEditPropForm.get(prop.symName).setValue(prop.mvalues[0]);
        } else {
          this.docEditPropForm.get(prop.symName).setValue(prop.mvalues[0]);
        }
      }
    });
    const subscriptions = this.as.getNextECMNo().subscribe(data => {
      this.ecmNo = data;
    });

    this.addToSubscriptions(subscriptions);
    this.addToSubscriptions(this.busyModal)
  }
  noWhitespaceValidator(control: FormControl) {
    let isWhitespace = (control.value || '').trim().length === 0;
    let isValid = !isWhitespace;
    return isValid ? null : { 'whitespace': true }
}

  toggleSidePanel() {
    this.togglePanel.emit();
  }

  editprop() {
    this.toggleSidePanel();
  }

  assignFolderSelected(data) {
    this.clickedFolder = data;
  }

  unfileFolder() {
    localStorage.setItem('unfileClicked', 'true');
    this.folderId = localStorage.getItem('folderId');
    for (const doc of this.selectedDocs) {
      this.cs.unfileFromFolder(this.folderId, doc.id).subscribe(data => this.unfileSuccess(), Error => this.unfileFailed());
    }
  }

  unfileSuccess() {
    setTimeout(() => {
      this.refresh();
      this.growlService.showGrowl({
        severity: 'info',
        summary: 'Success', detail: 'Removed From Folder '
      });
    }, 900);

  }

  unfileFailed() {
    this.growlService.showGrowl({
      severity: 'error',
      summary: 'Failure', detail: 'Failed To Remove'
    });
  }

  fileIn() {
    this.showFileIn = false;
    this.sendFolders.emit(this.selectedDocs);
  }

  moveToFolder() {
    this.showMove = false;
    this.sendMoveToFolder.emit(this.selectedDocs);
  }

  openSubTree() {
    this.toggleSidePanel();
    this.showFileIn = true
  }

  openMoveTree() {
    this.toggleSidePanel();
    this.selectedMoveFolder = undefined;
    this.ds.getDocumentFolders(this.selectedDocs[0].id).subscribe(data => this.assignMoveFolders(data))

  }

  selectFolderMove() {
    let movefolder = this.selectedMoveFolder.data.id;
    localStorage.setItem('folderIdForMoveConfirm', movefolder);
  }

  assignMoveFolders(data) {
    const topFolder = [];
    data.map((d, i) => {
      if (d != null) {
        if (d.type === 'PermissionsFolder' || d.type === 'PermissionFolder') {
          topFolder.push({
            label: d.name,
            data: d,
            'level': '1',
            'expandedIcon': 'ui-icon-folder-open',
            'collapsedIcon': 'ui-icon-folder-shared',
            'children': [],
            'leaf': true
          });
        }
        else {
          topFolder.push({
            label: d.name,
            data: d,
            'level': '1',
            'expandedIcon': 'ui-icon-folder-open',
            'collapsedIcon': 'ui-icon-folder',
            'children': [],
            'leaf': true
          });

        }
      }
    });
    this.moveFolderList = topFolder;
    if (data.length > 1) {
      this.showMoveFrom = true;
    }
    else {
      this.showMove = true;
    }

  }

  closePopUp() {
    //this.refresh();
    this.docEditPropForm.reset();
  }

  cancelEdit() {
    this.edit = false;
    this.docEditPropForm.reset();
  }


  selectFile(event) {
    if ((event.files[0].name).toLowerCase().includes('.jpg')||(event.files[0].name).toLowerCase().includes('.jpeg')
      || (event.files[0].name).toLowerCase().includes('.png') || (event.files[0].name).toLowerCase().includes('.gif')
      || (event.files[0].name).toLowerCase().includes('.pdf') || (event.files[0].name).toLowerCase().includes('.doc')
      || (event.files[0].name).toLowerCase().includes('.zip')|| (event.files[0].name).toLowerCase().includes('.tiff')
      || (event.files[0].name).toLowerCase().includes('.docx') || (event.files[0].name).toLowerCase().includes('.xls')
      || (event.files[0].name).toLowerCase().includes('.xlsx')
      || (event.files[0].name).toLowerCase().includes('.ppt') || (event.files[0].name).toLowerCase().includes('.pptx')) {
      for (const file of event.files) {
        this.uploadedFile = file;
      }
      if (this.uploadedFile !== undefined && this.entryTemp) {
        this.fileselected = true;
      } else {
        this.fileselected = false;
      }
      this.updateddDocuments = new FormData();
      this.updateddDocuments.append('document', this.uploadedFile);
    }

  }

  removeSelectedFile() {
    this.fileselected = false;
    this.uploadedFile = undefined;
  }

  updatedAttachment() {
    this.busyModal = this.ds.checkOut(this.saveDocInfo.id)
      .subscribe(data => this.checkoutSuccess(data), Error => this.updateFailed(Error));
    this.addToSubscriptions(this.busyModal);
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
    this.busyModal = this.ds.checkIn(this.updateddDocuments)
      .subscribe(data2 => this.updateSuccess(data2), Error => this.updateFailed(Error));
    this.addToSubscriptions(this.busyModal);
  }

  updateSuccess(data) {
    this.docSysProp = [];
    this.growlService.showGrowl({
      severity: 'info',
      summary: 'Success', detail: 'Updated Successfully'
    });
    this.editAttachment = false;
    this.fileselected = false;
    this.update = false;
    this.refresh();
    this.onModalHide();
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
    this.busyModal = this.ds.updateProperties(this.saveDocInfo)
      .subscribe(data => this.updateSuccess(data), Error => this.updateFailed(Error));
    this.addToSubscriptions(this.busyModal)
  }

  // updatesuccessprop() {
  //   this.growlService.showGrowl({
  //     severity: 'info',
  //     summary: 'Success', detail: 'Properties Updated Successfully'
  //   });
  //   this.edit = false;
  //   this.refresh();
  // }

  // updatepropfail() {
  //   this.growlService.showGrowl({
  //     severity: 'error',
  //     summary: 'Failure', detail: 'Edit Properties Failed'
  //   });
  // }

  openDoc() {
    this.bs.openDocInfoPanel.emit(this.selectedDocs);
  }

  openEditSecurity() {
    this.getDocPermissions();
  }

  closeEditSecurity() {
    this.newPermissions = [];
    this.refresh();
  }

  getDocPermissions() {
    this.selectedPolicy.permissions = [];
    const subscription = this.documentService.getDocumentAdhocPermissions(this.selectedDocs[0].id).subscribe(res => {
      this.selectedPolicy.id = this.selectedDocs[0].id;
      this.tempPermissions = res;
      res.map((r, i) => {
        r.id = i;
        if (r.inheritDepth === -2 || r.inheritDepth === -3) {
          this.selectedPolicy.permissions.push(Object.assign({}, r));
        }
      });
      this.selectedPolicy.permissions = [...this.selectedPolicy.permissions];
      this.showPermissionDialogue = true;
    }, err => {
      this.addNewPermission();
      this.showPermissionDialogue = true;
    });
    this.coreService.progress = {busy: subscription, message: '', backdrop: true};
    this.addToSubscriptions(subscription);
  }

  accessTypeChanged(permission) {
    permission.action = 'ADD';
  }

  permissionChanged(permission) {
    permission.action = 'ADD';
    permission.accessMask = this.accessLevelsMap[permission.accessLevel];
  }

  savePermissions() {
    const newPermissions = [];
    const selectedPolicy = Object.assign({}, this.selectedPolicy);
    selectedPolicy.permissions.map((p, i) => {
      if (p.action === 'ADD') {
        const oldP = Object.assign({}, p);

        oldP.accessLevel = this.tempPermissions[p.id].accessLevel;
        oldP.id = undefined;
        oldP.accessMask = this.accessLevelsMap[oldP.accessLevel];
        oldP.action = 'REMOVE';
        oldP.accessType = 'ALLOW';
        selectedPolicy.permissions.splice(i, 1, oldP);
        newPermissions.push(p);
      }
      p.id = undefined;
    });
    selectedPolicy.permissions = selectedPolicy.permissions.concat(newPermissions);
    if (this.newPermissions) {
      this.newPermissions.map(newPermission => {
        if (newPermission.granteeName) {
          const newPermissionObj: any = {};
          newPermissionObj.accessType = newPermission.accessType;
          newPermissionObj.action = 'ADD';
          newPermissionObj.depthName = '';
          newPermissionObj.inheritDepth = -3;
          newPermissionObj.permissionSource = 'DIRECT';
          newPermissionObj.granteeName = newPermission.granteeName.login;
          newPermissionObj.accessLevel = newPermission.accessLevel;
          newPermissionObj.accessMask = this.accessLevelsMap[newPermission.accessLevel];
          selectedPolicy.permissions.push(newPermissionObj);
        }
      });
    }
    const successMsg = 'Permission Updated Successfully';
    const errorMsg = 'Error In Updating Permission';
    const subscription = this.documentService.setDocumentAdhocPermissions(selectedPolicy).subscribe(res => {
      this.growlService.showGrowl({
        severity: 'info',
        summary: 'Success', detail: successMsg
      });
      this.showPermissionDialogue = false;
      this.newPermissions = [];
      this.refresh();
    }, err => {
      this.growlService.showGrowl({
        severity: 'error',
        summary: 'Error', detail: errorMsg
      });
    });

    this.coreService.progress = {busy: subscription, message: '', backdrop: true};
    this.addToSubscriptions(subscription);

  }

  addNewPermission() {
    if (!this.newPermissions) {
      this.newPermissions = [];
    }
    this.newPermissions = [...this.newPermissions, {
      granteeType: 'USER',
      accessLevel: 'Full Control',
      accessType: 'ALLOW'
    }];
  }

  getGranteesSuggestion(event) {
    if (event.np.granteeType === 'USER') {
      if (event.event.query.length >= 3) {
        const subscription = this.as.searchLDAPUsers(event.event.query).subscribe(res => {
          event.np.granteesSuggestion = res;

        }, err => {
        });
        this.addToSubscriptions(subscription);
        this.coreService.progress = {busy: subscription, message: '', backdrop: false};
      }
    } else {
      if (event.event.query.length >= 3) {
        const subscription = this.as.searchLDAPGroups(event.event.query).subscribe(res => {
          event.np.granteesSuggestion = res;
        }, err => {
        });
        this.addToSubscriptions(subscription);
        this.coreService.progress = {busy: subscription, message: '', backdrop: false};
      }
    }
  }

  addPermission(permission) {
    this.selectedPolicy.permissions.map((p, i) => {
      if (p === permission) {
        p.action = 'READ';
        this.selectedPolicy.permissions = [...this.selectedPolicy.permissions];
      }
    });
  }

  removePermission(permission) {
    this.selectedPolicy.permissions.map((p, i) => {
      if (p === permission) {
        p.action = 'REMOVE';
        this.selectedPolicy.permissions = [...this.selectedPolicy.permissions];
      }
    });
  }

  removeNewPermission(permission) {
    this.newPermissions.map((p, i) => {
      if (p === permission) {
        this.newPermissions.splice(i, 1);
        this.newPermissions = [...this.newPermissions];
      }
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

  ngOnDestroy() {
    this.clearSubscriptions();
    this.showRemove = false;
    this.selectedRemoveFolder = undefined;
    this.selectedMoveFolder = undefined;


  }
}

