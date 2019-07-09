import {Component, EventEmitter, Input, OnInit, Output} from '@angular/core';
import * as $ from 'jquery';
import {BrowserEvents} from '../../../services/browser-events.service';
import {DocumentService} from '../../../services/document.service';
import {Subscription} from 'rxjs/Subscription';
import {User} from '../../../models/user/user.model';
import {UserService} from '../../../services/user.service';
import {MenuItem, Message, TreeNode} from 'primeng/primeng';
import * as global from '../../../global.variables';
import {Router} from '@angular/router';
import {ContentService} from '../../../services/content-service.service';
import {AppComponent} from '../../../app.component';
import {DocumentSecurityModel} from '../../../models/document/document-security.model';
import {GrowlService} from '../../../services/growl.service';
import {CoreService} from '../../../services/core.service';
import {WorkflowService} from "../../../services/workflow.service";

@Component({
  selector: 'tree',
  templateUrl: './tree.component.html',
  styleUrls: ['./tree.component.css']
})
export class TreeComponent implements OnInit {
  cmItems: MenuItem[];
  folderPath: any;
  viewMoveTree: boolean;
  viewSecurity: boolean;
  folderList: TreeNode[];
  selectedFolder: any;
  folderTitle: any;
  index: any;
  privilage: any;
  busy: Subscription;
  @Input() public clearSelectedDocs: any = false;
  @Input() public changeView = true;
  public folderSecurity: DocumentSecurityModel[];
  private subscriptions: any[] = [];

  constructor(public cs: ContentService, public ds: DocumentService, private bs: BrowserEvents, private coreService: CoreService,
              private growlService: GrowlService, private contentServive: ContentService, private ws: WorkflowService) {
  }

  ngOnInit() {
    if (this.ds.savedFolderBrowse.folderTreeSaved && this.ds.savedFolderBrowse.folderTreeSaved.length>0) {
      this.folderList = this.ds.savedFolderBrowse.folderTreeSaved;
    } else {
      this.cs.getTopFolders().subscribe(data => this.getMainFolders(data));
    }
    if (this.ds.savedFolderBrowse.setSelectedFolder) {
      this.selectedFolder = this.ds.savedFolderBrowse.setSelectedFolder;
      localStorage.setItem('folderId',this.ds.savedFolderBrowse.selectedFolderId);
    }

    this.viewSecurity = false;
    this.viewMoveTree = false;
  }

  showPrivilages(data) {
    this.contentServive.getAccessPrivileges(data.accessMask).subscribe(val => this.assignPrivilages(val))
  }

  assignPrivilages(data) {
    this.privilage = data;
  }

  addFolderFav(selectedFolder) {
    this.cs.addFolderToFavorites(selectedFolder.data.id).subscribe(data => this.addFavSuccess(), error => this.addFavFailed());
  }

  addFavSuccess() {
    this.growlService.showGrowl({
      severity: 'info',
      summary: 'Success', detail: 'Added Folder To Favourites'
    });
  }

  addFavFailed() {
    this.growlService.showGrowl({
      severity: 'error',
      summary: 'Failure', detail: 'Failed To Add To Favourites'
    });
  }

  onContextMenu(folder) {
    localStorage.setItem('folderIdForMove', folder.node.data.id);
    if (folder.node.level === '1') {
      this.cmItems = [
        {label: 'Add To Favourites', icon: 'ui-icon-star', command: (event) => this.addFolderFav(this.selectedFolder)},
        {label: 'View Security', icon: 'ui-icon-https', command: (event) => this.viewSecurities(this.selectedFolder)}
      ];
    }
    else {
      this.cmItems = [
        {label: 'Add To Favourites', icon: 'ui-icon-star', command: (event) => this.addFolderFav(this.selectedFolder)},
        {
          label: 'Move To Folder',
          icon: 'ui-icon-open-in-browser',
          command: (event) => this.moveFolderToFolder(this.selectedFolder)
        },
        {label: 'View Security', icon: 'ui-icon-https', command: (event) => this.viewSecurities(this.selectedFolder)}
      ];
    }
  }

  viewSecurities(selectedFolder) {
    this.folderTitle = this.selectedFolder.data.name;
    this.viewSecurity = true;
    this.viewMoveTree = false;
    this.cs.getFolderPermissions(selectedFolder.data.id).subscribe(data => this.assignPermissions(data));
  }

  assignPermissions(data) {
    this.folderSecurity = data;
  }

  moveFolderToFolder(selectedFolder) {
    this.viewMoveTree = true;
    this.viewSecurity = false;
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
    this.cs.getTopFolders().subscribe(data => this.getMainFolders(data));
  }

  moveFailed() {
    this.growlService.showGrowl({
      severity: 'error',
      summary: 'Failure', detail: 'Move To Folder Failed'
    });
  }


  ngAfterContentInit() {

  }


  getMainFolders(data) {
    if (data[0].id !== undefined) {
      localStorage.setItem('folderId', data[0].id);
    }

    if (localStorage.getItem('folderIdForMove') === null) {
      localStorage.setItem('folderIdForMove', data[0].id);
    }
    if (this.changeView === true) {
      this.folderPath = data[0].path;
      this.bs.folderPath.emit(this.folderPath);
      // const subscription= this.cs.getDocumentFolders(data[0].id).subscribe(res => this.assignFolderDoc(res));
      // this.coreService.progress = {busy: subscription, message: '', backdrop: true};
      // this.addToSubscriptions(subscription);
    }

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
            'leaf': false
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
            'leaf': false
          });

        }
      }
    });
    this.folderList = topFolder;

  }

  assignFolderDoc(data) {
    this.bs.sendFolderDocs.emit(data);
  }

  nodeSelect(event) {
    if (this.ds.savedFolderBrowse.selectedFolderId === this.selectedFolder.data.id) {
      return;
    }
    localStorage.setItem('folderId', event.node.data.id);
    localStorage.setItem('path', event.node.data.path);
    this.ds.savedFolderBrowse.folderPathSavedBrowse = event.node.data.path;
    if (this.clearSelectedDocs !== true) {
      this.bs.clearSelectedDocs.emit();
    }
    if (this.changeView === true) {
      this.bs.sendFolderDocs.emit([]);
      this.folderPath = event.node.data.path;
      this.bs.folderPath.emit(this.folderPath);
      localStorage.setItem('folderIdForMove', event.node.data.id);
      localStorage.setItem('folderIdForMoveConfirm', event.node.data.id);
      this.busy = this.cs.getDocumentFolders(event.node.data.id).subscribe(data => this.assignFolderDoc(data));
      this.ds.savedFolderBrowse.selectedFolderId = event.node.data.id;
    }

  }

  nodeExpand(event) {
    if (this.changeView === true) {
      // this.folderPath = event.node.data.path;
      // this.bs.folderPath.emit(this.folderPath);
      //this.busy = this.cs.getDocumentFolders(event.node.data.id).subscribe(data => this.assignFolderDoc(data));
    }

    this.cs.getSubFolders(event.node.data.id).subscribe(data => this.assignSubFolders(event.node, data));

  }


  assignSubFolders(parent, data) {
    this.index++;
    const subFolder = [];
    data.map((d, i) => {
      if (d != null) {
        if (d.type === 'PermissionsFolder' || d.type === 'PermissionFolder') {
          subFolder.push({
            label: d.name,
            data: d,
            'level': '2',
            'expandedIcon': 'ui-icon-folder-open',
            'collapsedIcon': 'ui-icon-folder-shared',
            'leaf': false
          });
        }
        else {
          subFolder.push({
            label: d.name,
            data: d,
            'level': '2',
            'expandedIcon': 'ui-icon-folder-open',
            'collapsedIcon': 'ui-icon-folder',
            'leaf': false
          });

        }
      }
    });
    parent.children = subFolder;
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
    this.ds.savedFolderBrowse.folderTreeSaved = this.folderList;
    this.ds.savedFolderBrowse.setSelectedFolder =this.selectedFolder;
  }


}

