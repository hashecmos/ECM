import {Component, OnDestroy, OnInit} from '@angular/core';
import {BreadcrumbService} from '../../../services/breadcrumb.service';

import {UserService} from '../../../services/user.service';
import {DocumentService} from '../../../services/document.service';
import {ActivatedRoute, ParamMap} from '@angular/router';
import {BrowserEvents} from '../../../services/browser-events.service';
import {CoreService} from '../../../services/core.service';
import * as global from '../../../global.variables';
import {ContentService} from "../../../services/content-service.service";
import {GrowlService} from "../../../services/growl.service";
import {saveAs} from 'file-saver';
@Component({
  templateUrl: './simple-search.component.html',
})
export class SimpleSearchComponent implements OnInit, OnDestroy {
  currentUser;
  searchObj: any = {};
  subscriptions: any = [];
  //selectedIndex = 0;
  sideMenu: any;
  isSearchSelected=true;
  public selectedItem: any;
  screen: any = 'Simple Search';
  public colHeaders = [
    {field: 'creator', header: 'Created By'},
    {field: 'addOn', header: 'Added On', sortField: 'addOn2'},
    {field: 'modOn', header: 'Modified On', sortField: 'modOn2'},
    {field: 'modifier', header: 'Modified By'}];
  private itemsPerPage: any = 15;
  private emptyMessage: string;
  public exportFields: any[] = ['name','creator','addOn','modOn','modifier','format'];
  constructor(private breadcrumbService: BreadcrumbService, private us: UserService,
              private documentService: DocumentService, private route: ActivatedRoute,
              private browserEvents: BrowserEvents, private coreService: CoreService,
              private cs: ContentService, private growlService: GrowlService) {
    this.currentUser = this.us.getCurrentUser();
    this.breadcrumbService.setItems([
      {label: 'Search'},
      {label: 'Simple Search'}
    ]);
  }

  ngOnInit() {
    this.emptyMessage = global.no_doc_found;
    this.searchObj = {
      documentClasses: [], searchTemplate: undefined, model: {
        name: 'KOC Document', symName: 'KOCDocument', type: 'Document', props: [],
        contentSearch: {name: 'Content', symName: 'CONTENT', dtype: 'STRING', mvalues: []}
      }, actionTypes: [{label: 'Default', value: 'Default'},
        {label: 'Signature', value: 'Signature'}, {label: 'Initial', value: 'Initial'}],
      matchTypes: [{label: 'Exact match', value: 'EXACT'}, {label: 'All of the words', value: 'ALL'},
        {label: 'Any of the words', value: 'ANY'}],
    };

    this.route.paramMap
      .subscribe((params: any) => {
          this.isSearchSelected=true;
          if(this.selectedItem){
         this.refresh(this.selectedItem);
        }
        if (params.params.query && params.params.oper) {
          this.searchObj.model.contentSearch.mvalues[0] = params.params.query;
          this.searchObj.model.contentSearch.oper = params.params.oper;
          this.browserEvents.onSearchTextChanged(params);
        } else {
           this.searchObj.model.contentSearch.mvalues[0] = [];
           this.searchObj.model.contentSearch.oper = '';
           this.browserEvents.onSearchTextChanged(params);
        }



      });
    this.us.getUserSettings().subscribe(val => this.assignPagination(val));
     const subscription = this.browserEvents.changeFilterText.subscribe(data => this.refresh(data));
     this.coreService.progress = {busy: subscription, message: ''};
     this.addToSubscriptions(subscription);


  }

  assignPagination(val) {
    if (val !== undefined) {
      val.map((d, i) => {
        if (d.key === 'Page Size') {
          if(d.val !== '' && d.val){
            this.itemsPerPage = JSON.parse(d.val);
            this.searchObj.pageSize = 50;
          }
          else{
            this.itemsPerPage=15;
            this.searchObj.pageSize = 50;
          }

        }
      });
    }
  }

  onSearchComplete() {
    this.documentService.savedSearch.simpleSearchText=this.searchObj.model.contentSearch.mvalues[0];
    this.documentService.savedSearch.searchCriteria= this.searchObj.model.contentSearch.oper
   // this.selectedIndex = 1;
    if(this.selectedItem){
       this.refresh(this.selectedItem);
    }
    this.isSearchSelected=false;

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

  ngOnDestroy() {
    this.searchObj = {};
    this.clearSubscriptions();
    this.isSearchSelected=true;
    this.coreService.isAdvanced='N'

  }

  toggle() {
    this.sideMenu.toggle();
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

  changeTab(e) {
    console.log('search');
   // this.selectedIndex = e.index;
    if(this.selectedItem){
       this.refresh(this.selectedItem);
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
   getMoveToFolder(docs){
    let folderIdFrom;
    if(localStorage.getItem('folderIdForMoveConfirm')===undefined){
        folderIdFrom=localStorage.getItem('folderIdForMove');
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

}
