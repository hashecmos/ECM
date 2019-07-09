import {Component, Input, OnDestroy, Renderer} from '@angular/core';
import {MenuItem, TreeNode} from 'primeng/primeng';
import {ContentService} from '../../../services/content-service.service';
import {DocumentService} from '../../../services/document.service';
import {AppComponent} from '../../../app.component';
import {DocumentInfoModel} from '../../../models/document/document-info.model';
import {BrowserEvents} from '../../../services/browser-events.service';
import {BreadcrumbService} from '../../../services/breadcrumb.service';
import {Subscription} from 'rxjs/Rx';
import * as global from '../../../global.variables';
import {GrowlService} from '../../../services/growl.service';
import {CoreService} from "../../../services/core.service";
import {UserService} from "../../../services/user.service";
import {saveAs} from 'file-saver';

@Component({
  selector: 'favourite-folders',
  templateUrl: './favourite-folders.component.html',
  providers: [ContentService],
})
export class FavouriteFoldersComponent implements OnDestroy {
  folderList: TreeNode[];
  public tableData: any[];
  public selectedItem: any;
  emptyMessage: any;
  documentFolders: DocumentInfoModel[];
  public colHeaders: any[];
  public itemsPerPage: any;
  screen: any = 'Browse';
  loading: boolean;
  public sideMenu: any;
  folderPath: any;
  public busy: Subscription;
  private subscription: Subscription[] = [];
  index: any;
  favFolders = true;
  cmItems: MenuItem[];
  selectedFolder: any;
  viewMoveTree = false;
  openDocVisible = false;
  assignedPath: any;
  assignedId: any;
  @Input() public clearSelectedDocs: any = false;
  public exportFields: any[] = ['name', 'creator', 'addOn', 'modOn', 'modifier', 'format', 'verNo'];

  constructor(private breadcrumbService: BreadcrumbService, public cs: ContentService, public app: AppComponent,
              public ds: DocumentService, private bs: BrowserEvents, private growlService: GrowlService, public us: UserService,
              private coreService: CoreService) {
    this.breadcrumbService.setItems([
      {label: 'Folders'},
      {label: 'Favourite Folders'}
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
    this.colHeaders.push();
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
    if (this.ds.savedFolderFav.folderResultsSavedFav) {
      this.documentFolders = this.ds.savedFolderFav.folderResultsSavedFav;

    }
  }

  ngOnInit() {
    if( this.ds.savedFolderFav.setSelectedFolder){
      this.selectedFolder= this.ds.savedFolderFav.setSelectedFolder;
      localStorage.setItem('folderId',this.ds.savedFolderFav.selectedFolderId);
    }
    localStorage.setItem('split-pane', null);
    this.emptyMessage = global.no_doc_found;
    setTimeout(() => this.setPanelOverlay(), 0);
    if (this.ds.savedFolderFav.folderTreeSavedFav && this.ds.savedFolderFav.folderTreeSavedFav.length > 0) {
      this.folderList = this.ds.savedFolderFav.folderTreeSavedFav;
      if (this.ds.savedFolderFav.folderPathSavedFav) {
        this.folderPath = this.ds.savedFolderFav.folderPathSavedFav;
      }
    } else {
      const subscription = this.cs.getFavoriteFolders().subscribe(data => this.getFavFolders(data));
      this.coreService.progress = {busy: subscription, message: '', backdrop: true};
      this.addToSubscriptions(subscription);
    }
    this.bs.closeAddDocModel.subscribe(data => this.assignModelClose(data));

  }

  assignModelClose(data) {
    if (data === 'close') {
      this.openDocVisible = false;
    }
  }

  addDocTrigger() {
    this.openDocVisible = true;
    this.bs.addDocPath.emit(localStorage.getItem('path'));
    this.bs.addDocId.emit(localStorage.getItem('folderId'));
  }


  addToSubscriptions(subscription) {
    this.subscription.push(subscription);
  }

  setPanelOverlay() {
    if (this.app.layoutStatic === true) {
      this.app.layoutStatic = false;
    }
  }

  getFavFolders(data) {
    if (data.length > 0) {
      // if (this.ds.folderPathSavedFav) {
      //   this.folderPath = this.ds.folderPathSavedFav;
      // }
      // else {
        if (data[0].path !== undefined) {
          this.folderPath = data[0].path;
        }
     // }

      localStorage.setItem('folderId', data[0].id);
      localStorage.setItem('folderIdForMove', data[0].id);
      const favFolder = [];
      // const subscription = this.cs.getDocumentFolders(data[0].id).subscribe(res => this.assignFolderDocs(res));
      data.map((d, i) => {
        if (d != null) {
          if (d.type === 'Folder') {
            favFolder.push({
              label: d.name,
              data: d,
              'level': '1',
              'expandedIcon': 'ui-icon-folder-open',
              'collapsedIcon': 'ui-icon-folder',
              'children': [],
              'leaf': false
            });
          }
          else {
            favFolder.push({
              label: d.name,
              data: d,
              'level': '1',
              'expandedIcon': 'ui-icon-folder-open',
              'collapsedIcon': 'ui-icon-folder-shared',
              'children': [],
              'leaf': false
            });
          }
        }
      });
      this.folderList = favFolder;
      this.loading = false;

    }
    else {
      this.favFolders = false;
      localStorage.removeItem('folderId');
      this.documentFolders = [];
      this.folderList = [];
      this.loading = false;
      this.folderPath = '';
      this.ds.savedFolderFav.folderResultsSavedFav=[];
      this.ds.savedFolderFav.folderTreeSavedFav=[];
      this.ds.savedFolderFav.folderPathSavedFav='';


    }

  }

  // assignFolderPath(data) {
  //   this.folderPath = data;
  //   this.assignedPath=data;
  //   localStorage.setItem('path', data);
  //   this.assignedId=localStorage.getItem('folderId');
  //   console.log(this.assignedId);
  // }
  onDocumentAdded() {
    this.bs.closeAddDocModel.emit('close');
    this.busy = this.cs.getDocumentFolders(this.assignedId).subscribe(data => this.assignFolderDocs(data));
    this.folderPath = localStorage.getItem('path');

  }

  assignFolderDocs(data) {
    data.map(d => {
      d.name = d.fileName;
      d.addOn2 = this.coreService.convertToTimeInbox(d.addOn);
      d.modOn2 = this.coreService.convertToTimeInbox(d.modOn);
    });
    this.documentFolders = data;
    this.ds.savedFolderFav.folderResultsSavedFav = data;
  }

  getData(data: any, sidemenu: any) {
    this.sideMenu = sidemenu;
    this.selectedItem = data;
    if (data !== null && data !== undefined) {
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
    this.cs.getDocumentFolders(folderId).subscribe(data => this.assignFolderDocs(data));
  }

  toggle() {
    this.sideMenu.toggle();
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


  nodeSelect(event) {
     if (this.ds.savedFolderFav.selectedFolderId === this.selectedFolder.data.id) {
      return;
    }
    this.folderPath = event.node.data.path;
    this.ds.savedFolderFav.folderPathSavedFav = this.folderPath;
    this.bs.folderPath.emit(this.folderPath);
    localStorage.setItem('folderId', event.node.data.id);
    localStorage.setItem('folderIdForMove', event.node.data.id);
    localStorage.setItem('folderIdForMoveConfirm', event.node.data.id);
    this.assignedPath = event.node.data.path;
    localStorage.setItem('path', event.node.data.path);
    this.assignedId = event.node.data.id;
    if (this.clearSelectedDocs !== true) {
      this.bs.clearSelectedDocs.emit();
    }
    this.busy = this.cs.getDocumentFolders(event.node.data.id).subscribe(data => this.assignFolderDocs(data));
    this.ds.savedFolderFav.selectedFolderId = event.node.data.id;
  }

  nodeExpand(event) {
    // this.folderPath = event.node.data.path;
    // this.bs.folderPath.emit(this.folderPath);
    this.cs.getSubFolders(event.node.data.id).subscribe(data => this.assignSubFolders(event.node, data));
  }

  assignSubFolders(parent, data) {
    this.index++;
    const subFolder = [];
    data.map((d, i) => {
      if (d != null) {
        if (d.type === 'Folder') {
          subFolder.push({
            label: d.name,
            data: d,
            'level': '2',
            'expandedIcon': 'ui-icon-folder-open',
            'collapsedIcon': 'ui-icon-folder',
            'leaf': false
          });
        }
        else {
          subFolder.push({
            label: d.name,
            data: d,
            'level': '2',
            'expandedIcon': 'ui-icon-folder-open',
            'collapsedIcon': 'ui-icon-folder-shared',
            'leaf': false
          });
        }
      }
    });
    parent.children = subFolder;
    if (this.index === 1) {

    }

  }

  onContextMenu(folder) {
    localStorage.setItem('folderIdForMove', folder.node.data.id);
    if (folder.node.level === '1') {
      this.cmItems = [
        {
          label: 'Remove Favourites',
          icon: 'ui-icon-star-border',
          command: (event) => this.removeFolderFav(this.selectedFolder)
        },
      ];
    }
    else {
      this.cmItems = [
        {
          label: 'Move To Folder',
          icon: 'ui-icon-open-in-browser',
          command: (event) => this.moveFolderToFolder(this.selectedFolder)
        },
        {
          label: 'Remove Favourites',
          icon: 'ui-icon-star-border',
          command: (event) => this.removeFolderFav(this.selectedFolder)
        },
      ];
    }

  }

  moveFolderToFolder(selectedFolder) {
    this.viewMoveTree = true;
  }

  moveConfirm() {
    const source = localStorage.getItem('folderIdForMove');
    const target = localStorage.getItem('folderId');
    this.cs.moveFolderToFolder(source, target).subscribe(data => this.moveSuccess(), err => this.moveFailed())
  }

  moveSuccess() {
    this.growlService.showGrowl({
      severity: 'info',
      summary: 'Success', detail: 'Move To Folder Success'
    });
    this.viewMoveTree = false;
    this.subscription.push(this.cs.getFavoriteFolders().subscribe(data => this.getFavFolders(data)));
  }

  moveFailed() {
    this.growlService.showGrowl({
      severity: 'error',
      summary: 'Failure', detail: 'Move To Folder Failed'
    });
  }

  removeFolderFav(selectedFolder) {
    this.cs.removeFolderFromFavorites(selectedFolder.data.id).subscribe(data => this.remFavSuccess(), error => this.remFavFailed());
  }

  remFavSuccess() {
    this.growlService.showGrowl({
      severity: 'info',
      summary: 'Success', detail: 'Folder Removed From Favourites'
    });
    this.cs.getFavoriteFolders().subscribe(data => this.getFavFolders(data));
  }

  remFavFailed() {
    this.growlService.showGrowl({
      severity: 'error',
      summary: 'Failure', detail: 'Failed To Remove From Favourites'
    });
  }

  clearSubscriptions() {
    this.subscription.map(s => {
      s.unsubscribe();
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
              summary: 'Failure', detail: 'Move To Folder Folder'
            });
          }
        }));
    }
  }

  exportToExcel() {
    // const fileName = 'Favourite Folders' + this.folderPath.replace(/\//g, '-') + '-' + this.coreService.formatDateForDelegate(new Date()) + '.xlsx';
    // this.coreService.exportToExcel(this.documentFolders, fileName, this.exportFields);

    const docFolders = JSON.parse(JSON.stringify(this.documentFolders));
    docFolders.map((doc, index) => {
      delete doc.name;
      delete doc.addOn2;
      delete doc.modOn2;
    });
    const subscription = this.ds.exportFolderDocuments(docFolders, (this.folderPath.replace(/\//g, '-')).slice(1)).subscribe(res => {
      const file = new Blob([res], {type: 'application/vnd.ms-excel'});
      const fileName = (this.folderPath.replace(/\//g, '-')).slice(1) + '-' + this.coreService.formatDateForDelegate(new Date()) + '.xlsx';
      saveAs(file, fileName);
    });
    this.coreService.progress = {busy: subscription, message: ''};
    this.addToSubscriptions(subscription);
  }


  ngOnDestroy() {
    this.clearSubscriptions();
    this.documentFolders = [];
    this.colHeaders = [];
    this.cmItems = [];
    this.app.layoutStatic = true;
    localStorage.removeItem('folderId');
    localStorage.removeItem('path');
    localStorage.removeItem('folderIdForMove');
    this.viewMoveTree = false;
    this.ds.savedFolderFav.folderTreeSavedFav = this.folderList;
    this.ds.savedFolderFav.setSelectedFolder = this.selectedFolder;

  }


}
