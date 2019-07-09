import {Component, OnInit, Output, EventEmitter} from '@angular/core';
import {BreadcrumbService} from "../../../services/breadcrumb.service";
import {Subscription} from 'rxjs/Rx';
import {DocumentService} from '../../../services/document.service';
import {UserService} from '../../../services/user.service';
import {User} from '../../../models/user/user.model';
import {DocumentInfoModel} from '../../../models/document/document-info.model';
import {BrowserEvents} from '../../../services/browser-events.service';
import * as global from '../../../global.variables';
import {CoreService} from '../../../services/core.service';
import {ContentService} from "../../../services/content-service.service";
import {GrowlService} from "../../../services/growl.service";
import {saveAs} from 'file-saver';
@Component({
  selector: 'app-teamshared-docs',
  templateUrl: './teamshared-docs.component.html',
  styleUrls: ['./teamshared-docs.component.css']
})
export class TeamsharedDocsComponent implements OnInit {
  public selectedItem: any;
  public colHeaders: any[];
  public itemsPerPage: any;
  showpanel = true;
  screen: any = 'Team';
  emptyMessage: any;
  public user = new User();
  private subscription: Subscription[] = [];
  public teamDocs: DocumentInfoModel[];
  @Output() showPanel = new EventEmitter();
  public busy: Subscription;
  sideMenu: any;
 public exportFields: any[] = ['name','creator','addOn','modOn','modifier','format','verNo'];
  constructor(private breadcrumbService: BreadcrumbService, private ds: DocumentService, private us: UserService,private coreService: CoreService,
              private cs: ContentService, private growlService: GrowlService) {
    this.user = this.us.getCurrentUser();
    this.emptyMessage = global.no_doc_found;
    this.breadcrumbService.setItems([
      {label: 'Shortcuts'},
      {label: 'Team Shared Docs'}
    ]);
     this.us.getUserSettings().subscribe(val => {
      const res:any = val;
      this.assignPagination(res);
    } );

  }
    assignPagination(val) {
    if (val !== undefined) {
      val.map((d, i) => {
        if (d.key === 'Page Size') {
          if(d.val){
            this.itemsPerPage = parseInt(d.val,10);
          }else{
            this.itemsPerPage = 15;
          }

        }
      });

    }
  }


  getData(data: any, sidemenu) {
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

  ngOnInit() {
   const subscription=this.ds.getTeamDocuments(this.user.EmpNo)
      .subscribe(data => this.assignTeamDocs(data));
    this.coreService.progress={busy:subscription,message:''};
      this.addToSubscriptions(subscription);
  }
   addToSubscriptions(subscription) {
    this.subscription.push(subscription);
  }



  assignTeamDocs(data) {
    data.map((d, i) => {
      d.name = d.fileName;
      d.addOn2=this.coreService.convertToTimeInbox(d.addOn);
      d.modOn2=this.coreService.convertToTimeInbox(d.modOn);
    });
    this.teamDocs = data;
    this.colHeaders = [
      {field: 'creator', header: 'Created By'},
      {field: 'addOn', header: 'Added On',sortField:'addOn2'},
      {field: 'modOn', header: 'Modified On',sortField:'modOn2'},
      {field: 'modifier', header: 'Modified By'}];
    this.colHeaders.push();

  }

  refresh(docs) {
    let loop = 0;
    docs.map((d, i) => {
      loop++;
      if (loop === docs.length) {
        docs.splice(0, docs.length);
        if (this.sideMenu.isOpened) {
          this.sideMenu.toggle();
        }

      }
    });
  }

  refreshTable() {
    this.subscription.push(this.ds.getTeamDocuments(this.user.EmpNo)
      .subscribe(data => this.assignTeamDocs(data)));
  }

  getTeamUpdated(docs) {
    let loop = 0;
    docs.map((d, i) => {
      this.subscription.push(this.ds.removeFromFavorites(this.user.EmpNo, d.id)
        .subscribe(data => {
          loop++;
          if (loop === docs.length) {
            docs.splice(0, docs.length);
            this.subscription.push(this.ds.getTeamDocuments(this.user.EmpNo)
              .subscribe(val => this.assignTeamDocs(val)));
            if (this.sideMenu.isOpened) {
              this.sideMenu.toggle();
            }

          }
        }));
    });
  }

  toggle() {
    this.sideMenu.toggle();
  }

  clearSubscriptions() {
    this.subscription.map(s => {
      s.unsubscribe();
    });
  }
  exportToExcel() {
    // const fileName = 'teamshared docs' + '-' + this.coreService.formatDateForDelegate(new Date()) + '.xlsx';
    // this.coreService.exportToExcel(this.teamDocs, fileName, this.exportFields);
    const teamDoc = JSON.parse(JSON.stringify(this.teamDocs));
    teamDoc.map((doc, index)=>{
      delete doc.name;
      delete doc.addOn2;
      delete doc.modOn2;
    });
    const subscription = this.ds.exportFolderDocuments(teamDoc,'Team shared documents').subscribe(res => {
        const file = new Blob([res], {type: 'application/vnd.ms-excel'});
        const fileName = 'teamshared_docs' + '-' + this.coreService.formatDateForDelegate(new Date()) + '.xlsx';
        saveAs(file, fileName);
    });
    this.coreService.progress={busy:subscription,message:''};
    this.addToSubscriptions(subscription);
  }
  getBrowseUpdated(docs) {
    const folderId = localStorage.getItem('folderId');
    let loop = 0;
    docs.map((d, i) => {
      this.subscription.push(this.cs.fileInFolder(folderId, d.id)
        .subscribe(data => {
           if(data==='OK'){
              this.growlService.showGrowl({
              severity: 'info',
              summary: 'Success', detail: 'Add To Folder Success'
            });
          loop++;
          if (loop === docs.length) {
            docs.splice(0, docs.length);
          }
           }
           else{
             this.growlService.showGrowl({
              severity: 'error',
              summary: 'Failure', detail: 'Add To Folder Failed'
            });
           }

        }));
    });
  }
   getMoveToFolder(docs){
    let folderIdFrom;
    if(localStorage.getItem('folderIdForMoveConfirm')===undefined){
        folderIdFrom=localStorage.getItem('folderIdForMove');
    }
    else {
       this.ds.getDocumentFolders(docs[0].id).subscribe(data => this.assignMoveDocFrom(data, docs));

     }
  }
  assignMoveDocFrom(data,docs){
    let folderIdFrom;
    if(data.length===1){
     folderIdFrom= data[0].id;
    }
     else{
      folderIdFrom=localStorage.getItem('folderIdForMoveConfirm');
    }
     const folderIdTo = localStorage.getItem('folderId');
    const moveToFolder = { 'sourceFolder': folderIdFrom, 'targetFolder': folderIdTo, 'docIds':[]};
    docs.map((d, i) => {
      moveToFolder.docIds.push(d.id);
    });
     if(folderIdFrom===folderIdTo){
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
             docs.splice(0, docs.length);
             const subscription=this.ds.getTeamDocuments(this.user.EmpNo)
      .subscribe(data => this.assignTeamDocs(data));
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
  ngOnDestroy() {
    this.clearSubscriptions();
    this.teamDocs = [];
    this.user = undefined;
    this.colHeaders = [];


  }


}
