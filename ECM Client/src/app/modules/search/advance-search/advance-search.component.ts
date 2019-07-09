import {Component, OnDestroy, OnInit, QueryList, ViewChildren} from '@angular/core';
import {BreadcrumbService} from '../../../services/breadcrumb.service';

import {UserService} from '../../../services/user.service';
import {DocumentService} from '../../../services/document.service';
import {DomSanitizer, SafeResourceUrl} from '@angular/platform-browser';
import {CoreService} from '../../../services/core.service';
import * as global from '../../../global.variables';
import {ContentService} from "../../../services/content-service.service";
import {GrowlService} from "../../../services/growl.service";
import {BrowserEvents} from "../../../services/browser-events.service";
import {saveAs} from 'file-saver';
import {Subscription} from "rxjs/Subscription";
import {SearchComponent} from "../search.component";
import {SearchDocumentComponent} from "../../../components/generic-components/search-document/search.component";
import {LazyLoadEvent} from "primeng/primeng";
@Component({
  templateUrl: './advance-search.component.html',
})
export class AdvanceSearchComponent implements OnInit, OnDestroy {
  currentUser;
  searchObj: any = {};
  subscriptions: any = [];
 // selectedIndex = 0;
  viewer = false;
  docTitle: any;
  sideMenu: any;
  itemsPerPage: any =15;
  emptyMessage: any;
  screen: any = 'Advanced Search';
  public selectedItem: any;
  isSearchSelected=true;
  private attach_url: SafeResourceUrl;
  public colHeaders = [
    {field: 'creator', header: 'Created By'},
    {field: 'addOn', header: 'Added On', sortField: 'addOn2'},
    {field: 'modOn', header: 'Modified On', sortField: 'modOn2'},
    {field: 'modifier', header: 'Modified By'}];
  public exportFields: any[] = ['name','creator','addOn','modOn','modifier','format'];
  constructor(private breadcrumbService: BreadcrumbService, private sanitizer: DomSanitizer,
              private us: UserService, private documentService: DocumentService, private coreService: CoreService,private bs: BrowserEvents,
              private cs: ContentService, private growlService: GrowlService) {
    this.currentUser = this.us.getCurrentUser();
    this.breadcrumbService.setItems([
      {label: 'Search'},
      {label: 'Advance Search'}
    ]);

  }


  ngOnInit() {
    if(this.documentService.savedSearch.searchResultsSaved && this.documentService.savedSearch.searchResultsSaved.totalResults>0){
      this.isSearchSelected=false;
    }
    this.emptyMessage = global.no_doc_found;
    if(this.selectedItem){
         this.refresh(this.selectedItem);
        }
    this.searchObj = {
      documentClasses: [], searchTemplate: undefined, model: {
        contentSearch: {name: 'Content', symName: 'CONTENT', oper:'exact_match',dtype: 'STRING', mvalues: []},
        actionType: 'Default'
      }, actionTypes: [{label: 'Default', value: 'Default'},
        {label: 'Signature', value: 'Signature'}, {label: 'Initial', value: 'Initial'}],
      matchTypes: [{label: 'Exact match', value: 'exact_match'}, {label: 'All of the words', value: 'all_of_the_words'},
        {label: 'Any of the words', value: 'any_of_the_words'}],
    };
    this.us.getUserSettings().subscribe(val => {
      const res:any = val;
      this.assignPagination(res);
    } );
     const subscription = this.bs.changeFilterText.subscribe(data => this.refresh(data));
     this.addToSubscriptions(subscription);
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

  assignPagination(val) {
    if (val !== undefined) {
      val.map((d, i) => {
        if (d.key === 'Page Size') {
          if(d.val){
            this.itemsPerPage = parseInt(d.val,10);
            this.searchObj.pageSize = 50;
          }else{
            this.itemsPerPage = 15;
            this.searchObj.pageSize = 50;
          }

        }
      });
    }
  }

  addToCart(doc) {
    const subscription = this.documentService.addToCart(this.currentUser.EmpNo, doc.id).subscribe(res => {

    });
    this.addToSubscriptions(subscription);
  }

  downloadDoc(doc) {
    window.location.assign(this.documentService.downloadDocument(doc.id));
  }

  addToSubscriptions(subscription) {
    this.subscriptions.push(subscription);
  }

  clearSubscriptions() {
    this.subscriptions.map(s => {
      s.unsubscribe();
    });
  }

  onSearchComplete() {
   // this.selectedIndex = 1;
    //console.log(this.isSearchSelected);
    if(this.selectedItem){
       this.refresh(this.selectedItem);
    }
     this.isSearchSelected=false;
  }

  toggle() {
    this.sideMenu.toggle();
  }

  exportToExcel() {
    // const fileName = 'search-result-' + '-' + this.coreService.formatDateForDelegate(new Date()) + '.xlsx';
    // this.coreService.exportToExcel(this.searchObj.searchResult, fileName, this.exportFields);

    const searchResult = JSON.parse(JSON.stringify(this.searchObj.searchResult));
    searchResult.map((doc, index)=>{
      delete doc.name;
      delete doc.addOn2;
      delete doc.modOn2;
    });
    const subscription = this.documentService.exportFolderDocuments(searchResult,'search-result').subscribe(res => {
        const file = new Blob([res], {type: 'application/vnd.ms-excel'});
        const fileName = 'search-result' + '-' + this.coreService.formatDateForDelegate(new Date()) + '.xlsx';
        saveAs(file, fileName);
    });
  }

  changeTab(e) {
   // this.selectedIndex = e.index;
    if(this.selectedItem){
       this.refresh(this.selectedItem);
    }

  }

  getBrowseUpdated(docs) {
    const folderId = localStorage.getItem('folderId');
    let loop = 0;
    docs.map((d, i) => {
      this.subscriptions.push(this.cs.fileInFolder(folderId, d.id)
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
   getMoveToFolder(docs) {
     let folderIdFrom;
     if (localStorage.getItem('folderIdForMoveConfirm') === undefined) {
       folderIdFrom = localStorage.getItem('folderIdForMove');
     }
     else {
       this.documentService.getDocumentFolders(docs[0].id).subscribe(data => this.assignMoveDocFrom(data, docs));

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
       this.subscriptions.push(this.documentService.moveMultipleDocuments(moveToFolder)
         .subscribe(data => {
           if (data === 'OK') {
             this.growlService.showGrowl({
               severity: 'info',
               summary: 'Success', detail: 'Move To Folder Successful'
             });
             const folderId = localStorage.getItem('folderIdForMove');
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

  continueSearch(event){
    if (!event || !event.rows) {
      return;
    }
    const pageNo = Math.ceil(event.first / event.rows) + 1;
    const skip = this.searchObj.pageSize*(pageNo-1);
    console.log(skip);
    if(pageNo > 1 || skip === 0){
      const subscriptions = this.documentService.continueSearch(
        {continueData:this.searchObj.continueData,skip:skip,pageSize:this.searchObj.pageSize}).subscribe(data => {
          console.log(data);
          this.searchObj.continueData = data.continueData;
          data.row.map(d => {
            d.name = d.props[0].mvalues[0];
            d.addOn2 = this.coreService.convertToTimeInbox(d.addOn);
            d.modOn2 = this.coreService.convertToTimeInbox(d.modOn);
          });
        this.searchObj.searchResult = data.row;
        //this.onSearchComplete.emit();
      });
      this.coreService.progress = {busy: subscriptions, message: ''};
      this.addToSubscriptions(subscriptions);
    }
  }

  ngOnDestroy() {
    this.searchObj = {};
    this.clearSubscriptions();
    this.isSearchSelected=true;
    this.coreService.isAdvanced='Y'
  }

}
