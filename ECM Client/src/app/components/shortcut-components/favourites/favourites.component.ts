import {Component, OnInit, Output, EventEmitter, Input, ViewChild, OnDestroy} from '@angular/core';
import {BreadcrumbService} from "../../../services/breadcrumb.service";
import {DocumentService} from '../../../services/document.service';
import {Subscription} from 'rxjs/Rx';
import {User} from '../../../models/user/user.model';
import {UserService} from '../../../services/user.service';
import {DocumentInfoModel} from '../../../models/document/document-info.model';
import {BrowserEvents} from '../../../services/browser-events.service';
import {Message} from 'primeng/primeng';
import {GrowlService} from '../../../services/growl.service';
import * as global from '../../../global.variables';
import {DataTableComponent} from '../../generic-components/datatable/datatable.component';
import {CoreService} from '../../../services/core.service';
import {ContentService} from "../../../services/content-service.service";
import {saveAs} from 'file-saver';

@Component({
  selector: 'app-favourites',
  templateUrl: './favourites.component.html',
  styleUrls: ['./favourites.component.css']
})
export class FavouritesComponent implements OnInit,OnDestroy {
  public selectedItem: any;
  private subscription: Subscription[] = [];
  public itemsPerPage: any;
  emptyMessage: any;
  screen: any = 'Favourites';
  public user = new User();
  sideMenu: any;
  public busy: Subscription;
  public favDocuments: DocumentInfoModel[];
  @Output() showPanel = new EventEmitter();
  colHeaders:any[] = [
      {field: 'creator', header: 'Created By'},
      {field: 'addOn', header: 'Added On',sortField:'addOn2'},
      {field: 'modOn', header: 'Modified On',sortField:'modOn2'},
      {field: 'modifier', header: 'Modified By'}];
  public exportFields: any[] = ['name','creator','addOn','modOn','modifier','format','verNo'];
  constructor(private breadcrumbService: BreadcrumbService, private ds: DocumentService, private us: UserService,
              private bs: BrowserEvents, private growlService: GrowlService, private coreService: CoreService,
              private cs: ContentService) {
    this.user = this.us.getCurrentUser();
    this.emptyMessage = global.no_doc_found;
    this.breadcrumbService.setItems([
      {label: 'Shortcuts'},
      {label: 'Favourites'}
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
     const subscription=this.ds.getFavourites(this.user.EmpNo)
      .subscribe(data => this.assignFavourites(data));
      this.coreService.progress={busy:subscription,message:''};
      this.addToSubscriptions(subscription);
  }
   addToSubscriptions(subscription) {
    this.subscription.push(subscription);
  }

  toggle() {
    this.sideMenu.toggle();
  }

  assignFavourites(data) {
    data.map((d, i) => {
      d.name = d.fileName;
      d.addOn2=this.coreService.convertToTimeInbox(d.addOn);
      d.modOn2=this.coreService.convertToTimeInbox(d.modOn);
    });
    this.favDocuments = data;
  }

  getFavUpdated(docs) {
    let loop = 0;
    docs.map((d, i) => {
      this.subscription.push(this.ds.removeFromFavorites(this.user.EmpNo, d.id)
        .subscribe(data => {
          if (data === 'OK') {
            this.growlService.showGrowl({
              severity: 'info',
              summary: 'Success', detail: 'Removed From Favourites'
            });
          }
          else {
            this.growlService.showGrowl({
              severity: 'error',
              summary: 'Failure', detail: 'Failed To Remove From Favourites'
            });
          }
          loop++;
          if (loop === docs.length) {
            docs.splice(0, docs.length);
            this.subscription.push(this.ds.getFavourites(this.user.EmpNo)
              .subscribe(res => this.assignFavourites(res)));
            if (this.sideMenu.isOpened) {
              this.sideMenu.toggle();
            }

          }
        }));
    });

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
    this.refreshTable();
  }

  refreshTable() {
    this.subscription.push(this.ds.getFavourites(this.user.EmpNo)
      .subscribe(res => this.assignFavourites(res)));
  }

  clearSubscriptions() {
    this.subscription.map(s => {
      s.unsubscribe();
    });
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

  exportToExcel() {
    // const fileName = 'favourite docs' + '-' + this.coreService.formatDateForDelegate(new Date()) + '.xlsx';
    // this.coreService.exportToExcel(this.favDocuments, fileName,this.exportFields);
    const favDoc = JSON.parse(JSON.stringify(this.favDocuments));
    favDoc.map((doc, index)=>{
      delete doc.name;
      delete doc.addOn2;
      delete doc.modOn2;
    });
    const subscription = this.ds.exportFolderDocuments(favDoc,'favourite documents').subscribe(res => {
        const file = new Blob([res], {type: 'application/vnd.ms-excel'});
        const fileName = 'favourite_docs' + '-' + this.coreService.formatDateForDelegate(new Date()) + '.xlsx';
        saveAs(file, fileName);
    });
    this.coreService.progress={busy:subscription,message:''};
    this.addToSubscriptions(subscription);
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
              const subscription=this.ds.getFavourites(this.user.EmpNo)
      .subscribe(data => this.assignFavourites(data));
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
    this.favDocuments = [];
    this.user = undefined;
    this.colHeaders = [];
  }

}
