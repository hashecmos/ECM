import {Component, OnDestroy, OnInit, ViewChild} from '@angular/core';
import {DataTable, TreeNode} from 'primeng/primeng';
import {ContentService} from '../../../services/content-service.service';
import {DocumentService} from '../../../services/document.service';
import {AppComponent} from '../../../app.component';
import {DocumentInfoModel} from '../../../models/document/document-info.model';
import {BrowserEvents} from '../../../services/browser-events.service';
import {BreadcrumbService} from '../../../services/breadcrumb.service';
import {Subscription} from "rxjs/Subscription";
import {GrowlService} from '../../../services/growl.service';
import * as global from '../../../global.variables';
import {CoreService} from '../../../services/core.service';
import {UserService} from "../../../services/user.service";
import {saveAs} from 'file-saver';
import {WorkflowService} from "../../../services/workflow.service";
import {TreeComponent} from "../../../components/generic-components/tree/tree.component";
import {DataTableComponent} from "../../../components/generic-components/datatable/datatable.component";

@Component({
  selector: 'browse-document',
  templateUrl: './browse-document.component.html',
  providers: [ContentService],
})
export class BrowseDocumentComponent implements OnInit, OnDestroy {
  folderList: TreeNode[];
  public tableData: any[];
  @ViewChild(TreeComponent) tree: TreeComponent;
  @ViewChild(DataTable) dataTableComponent: DataTableComponent;
  public selectedItem: any;
  documentFolders: DocumentInfoModel[] = [];
  public colHeaders: any[];
  public busy: Subscription;
  public itemsPerPage: any;
  screen: any = 'Browse';
  sideMenu: any;
  folderPath: any;
  openDocVisible = false;
  emptyMessage: any;
  assignedPath: any;
  assignedId: any;
  private subscription: Subscription[] = [];
  private subscriptions: any[] = [];
  public exportFields: any[] = ['name', 'creator', 'addOn', 'modOn', 'modifier', 'format', 'verNo'];

  constructor(private breadcrumbService: BreadcrumbService, public cs: ContentService, public app: AppComponent,
              public ds: DocumentService, private bs: BrowserEvents, private growlService: GrowlService, public us: UserService,
              private coreService: CoreService) {
    this.breadcrumbService.setItems([
      {label: 'Folders'},
      {label: 'Public Folders'}
    ]);
    this.us.getUserSettings().subscribe(val => {
      const res: any = val;
      this.assignPagination(res);
    });
    this.colHeaders = [
      {field: 'creator', header: 'Created By'},
      {field: 'addOn', header: 'Added On', sortField: 'addOn2'},
      {field: 'modOn', header: 'Modified On', sortField: 'modOn2'},
      {field: 'modifier', header: 'Modified By'}];
  }

  assignPagination(val) {
    if (val !== undefined) {
      val.map((d, i) => {
        if (d.key === 'Page Size') {
          if (d.val) {
            this.itemsPerPage = parseInt(d.val, 10);
          } else {
            this.itemsPerPage = 15;
          }

        }
      });

    }
    this.loadSavedDocuments()
  }

  loadSavedDocuments() {
    if (this.ds.savedFolderBrowse.folderResultsSavedBrowse) {
      this.documentFolders = this.ds.savedFolderBrowse.folderResultsSavedBrowse;

    }
  }


  ngOnInit() {
    if (this.ds.savedFolderBrowse.folderPathSavedBrowse) {
      this.folderPath = this.ds.savedFolderBrowse.folderPathSavedBrowse;
    }
    else {
      this.folderPath = '/ECMRootFolder/Public Folders/';
    }
    localStorage.setItem('split-pane', null);
    this.emptyMessage = global.no_doc_found;
    setTimeout(() => this.setPanelOverlay(), 0);
    setTimeout(() => {
        this.bs.sendFolderDocs.subscribe(data => this.assignFolderDocs(data));

      }
      , 6);
    this.bs.closeAddDocModel.subscribe(data => this.assignModelClose(data));


  }

  assignModelClose(data) {
    if (data === 'close') {
      this.openDocVisible = false;
    }
  }

  setPanelOverlay() {
    if (this.app.layoutStatic === true) {
      this.app.layoutStatic = false;
    }
  }


  assignFolderPath(data) {
    this.folderPath = data;
    localStorage.setItem('path', data);
    this.assignedPath = data;
    this.assignedId = localStorage.getItem('folderId')

  }


  assignFolderDocs(data) {
    this.bs.folderPath.subscribe(data => this.assignFolderPath(data));
    data.map(d => {
      d.addOn2 = this.coreService.convertToTimeInbox(d.addOn);
      d.modOn2 = this.coreService.convertToTimeInbox(d.modOn);
    });
    if (this.folderPath === '/ECMRootFolder/Public Folders/') {
      this.documentFolders = [];
    }
    else {
      this.documentFolders = data;
    }

    if (data) {
      data.map((d, i) => {
        d.name = d.fileName;
      });
    }
    this.ds.savedFolderBrowse.folderResultsSavedBrowse = data;

  }

  getData(data: any, sidemenu: any) {
    this.sideMenu = sidemenu;
    this.selectedItem = data;
    if (data !== undefined) {
      if (data.length === 0 && sidemenu.isOpened) {
        sidemenu.toggle();
      }
      if (data.length >= 1 && !sidemenu.isOpened) {
        sidemenu.toggle();
      }
    }
  }


  refresh(docs) {
    let loop = 0;
    const folderId = localStorage.getItem('folderId');
    if (localStorage.getItem('unfileClicked') === 'true') {
      this.busy = this.cs.getDocumentFolders(folderId).subscribe(data => this.assignFolderDocs(data));
    }
    else {
      this.cs.getDocumentFolders(folderId).subscribe(data => this.assignFolderDocs(data));
    }

    docs.map((d, i) => {
      loop++;
      if (loop === docs.length) {
        docs.splice(0, docs.length);
        if (this.sideMenu.isOpened) {
          this.sideMenu.toggle();
        }

      }
    });
    localStorage.removeItem('unfileClicked');

  }

  refreshTable() {
    const folderId = localStorage.getItem('folderId');
    this.busy = this.cs.getDocumentFolders(folderId).subscribe(data => this.assignFolderDocs(data));
  }

  toggle() {
    if (this.sideMenu.isOpened !== false) {
      this.sideMenu.toggle();
    }
  }

  getBrowseUpdated(docs) {
    const folderId = localStorage.getItem('folderId');
    let loop = 0;
    docs.map((d, i) => {
      this.subscription.push(this.cs.fileInFolder(folderId, d.id)
        .subscribe(data => {
          if (data === 'OK') {
            this.growlService.showGrowl({
              severity: 'info',
              summary: 'Success', detail: 'Add To Folder Success'
            });
            loop++;
            if (loop === docs.length) {
              docs.splice(0, docs.length);
            }
          }
          else {
            this.growlService.showGrowl({
              severity: 'error',
              summary: 'Failure', detail: 'Add To Folder Failed'
            });
          }

        }));
    });
  }

  getMoveToFolder(docs) {
    let folderIdFrom;
    if (localStorage.getItem('folderIdForMoveConfirm') === undefined) {
      folderIdFrom = localStorage.getItem('folderIdForMove');
    }
    else {
      folderIdFrom = localStorage.getItem('folderIdForMoveConfirm');
    }

    const folderIdTo = localStorage.getItem('folderId');
    const moveToFolder = {'sourceFolder': folderIdFrom, 'targetFolder': folderIdTo, 'docIds': []};
    docs.map((d, i) => {
      moveToFolder.docIds.push(d.id);
    });

    if (folderIdFrom === folderIdTo) {
      this.growlService.showGrowl({
        severity: 'error',
        summary: 'Cant Move', detail: 'Source and Destination are same'
      });
    }
    else {
      this.subscription.push(this.ds.moveMultipleDocuments(moveToFolder)
        .subscribe(data => {
          if (data === 'OK') {
            this.growlService.showGrowl({
              severity: 'info',
              summary: 'Success', detail: 'Move To Folder Successful'
            });
            const folderId = localStorage.getItem('folderIdForMove');
            this.busy = this.cs.getDocumentFolders(folderId).subscribe(data2 => this.assignFolderDocs(data2));
            docs.splice(0, docs.length);
          }
          else {
            this.growlService.showGrowl({
              severity: 'error',
              summary: 'Failure', detail: 'Move To Folder Failed'
            });
          }
        }));
    }

  }


  addDocTrigger() {
    this.openDocVisible = true;
    this.bs.addDocPath.emit(localStorage.getItem('path'));
    this.bs.addDocId.emit(localStorage.getItem('folderId'));
  }

  onDocumentAdded() {
    this.bs.closeAddDocModel.emit('close');
    this.busy = this.cs.getDocumentFolders(this.assignedId).subscribe(data => this.assignFolderDocs(data));
    this.folderPath = localStorage.getItem('path');

  }


  exportToExcel() {
    // const fileName = 'Public Folders' + this.folderPath.replace(/\//g, '-') + '-' + this.coreService.formatDateForDelegate(new Date()) + '.xlsx';
    // this.coreService.exportToExcel(this.documentFolders, fileName, this.exportFields);

    const docFolders = JSON.parse(JSON.stringify(this.documentFolders));
    docFolders.map((doc, index) => {
      delete doc.name;
      delete doc.addOn2;
      delete doc.modOn2;
    });
    const subscription = this.ds.exportFolderDocuments(docFolders, (this.folderPath.replace(/\//g, '-')).slice(1)).subscribe(res => {
      const file = new Blob([res], {type: 'application/vnd.ms-excel'});
      console.log(res);
      const fileName = (this.folderPath.replace(/\//g, '-')).slice(1) + '-' + this.coreService.formatDateForDelegate(new Date()) + '.xlsx';
      saveAs(file, fileName);
    });
    this.coreService.progress = {busy: subscription, message: ''};
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
    this.app.layoutStatic = true;
    localStorage.removeItem('folderId');
    localStorage.removeItem('folderIdForMove');


  }


}
