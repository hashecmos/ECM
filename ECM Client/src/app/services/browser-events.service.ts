import {Injectable, Output, EventEmitter, Renderer, Input} from '@angular/core';
import {Subject} from 'rxjs/Subject';

@Injectable()
export class BrowserEvents {
  // source Subjects
  emitclick = new EventEmitter<any>();
  removeCartClick = new EventEmitter<any>();
  folderclick = new EventEmitter<any>();
  folderdefault = new EventEmitter<any>();
  topLinkesClicked = new EventEmitter<any>();
  fileInClick = new EventEmitter<any>();
  addDocClick = new EventEmitter<any>();
  folderPath = new EventEmitter<any>();
  addDocFolderId = new EventEmitter<any>();
  sideNavChange = new EventEmitter<any>();
  docsSelected = new EventEmitter<any>();
  skipDocLaunch = new EventEmitter<any>();
  sendFolderDocs = new EventEmitter<any>();
  addDocPath = new EventEmitter<any>();
  addDocId = new EventEmitter<any>();
  closeAddDocModel = new EventEmitter<any>();
  clearSelectedDocs = new EventEmitter<any>();
  private searchTextChanged = new Subject<any>();
  openDocInfoPanel = new EventEmitter<any>();
  changeFilterText = new EventEmitter<any>();
  openedWkitem = new EventEmitter<any>();
  switchBackContentSearch = new EventEmitter<any>();



  // Observable streams
  emitClick$ = this.emitclick.asObservable();
  folderclick$ = this.folderclick.asObservable();
  folderdefault$ = this.folderdefault.asObservable();
  removeCartClick$ = this.removeCartClick.asObservable();
  topLinkesClicked$ = this.topLinkesClicked.asObservable();
  fileInClick$ = this.fileInClick.asObservable();
  addDocClick$ = this.addDocClick.asObservable();
  folderPath$ = this.folderPath.asObservable();
  addDocFolderId$ = this.addDocFolderId.asObservable();
  sideNavChange$ = this.sideNavChange.asObservable();
  docsSelected$ = this.docsSelected.asObservable();
  skipDocLaunch$ = this.skipDocLaunch.asObservable();
  sendFolderDocs$ = this.sendFolderDocs.asObservable();
  addDocPath$ = this.addDocPath.asObservable();
  addDocId$ = this.addDocId.asObservable();
  closeAddDocModel$ = this.closeAddDocModel.asObservable();
  clearSelectedDocs$ = this.clearSelectedDocs.asObservable();
  searchTextChanged$ = this.searchTextChanged.asObservable();
  openDocInfoPanel$ = this.openDocInfoPanel.asObservable();
  changeFilterText$ = this.changeFilterText.asObservable();
  openedWkitem$ = this.openedWkitem.asObservable();
  switchBackContentSearch$ = this.switchBackContentSearch.asObservable();


  // Service Methods
  switchBackToContentSearch(change: any) {
    this.switchBackContentSearch.next(change);
  }
  openedWkitemId(change: any) {
    this.openedWkitem.next(change);
  }
  changeFilter(change: any) {
    this.changeFilterText.next(change);
  }

  emitClickScreen(change: any) {
    this.emitclick.next(change);
  }

  emitClickFolder(change: any) {
    this.folderclick.next(change);
  }

  emitDefaultFolder(change: any) {
    this.folderdefault.next(change);
  }

  removeCart(id: any) {
    this.removeCartClick.next(id);
  }

  emitTopLinkesClicked() {
    this.topLinkesClicked.next();
  }

  emitfileInClicked(change: any) {
    this.fileInClick.next(change);
  }

  emitAddDocClick() {
    this.addDocClick.next();
  }

  emitFolderPath(change: any) {
    this.folderPath.next(change);
  }

  emitaddDocFolderId(change: any) {
    this.addDocFolderId.next(change);
  }

  sideNavActionChange(change: any) {
    this.sideNavChange.next(change);
  }

  docSelected(change: any) {
    this.docsSelected.next(change);
  }

  launchSkipped(change: any) {
    this.skipDocLaunch.next(change);
  }

  sendDocs(change: any) {
    this.sendFolderDocs.next(change);
  }

  addDocPaths(change: any) {
    this.addDocPath.next(change);
  }

  addDocIds(change: any) {
    this.addDocId.next(change);
  }

  closeModel(change: any) {
    this.closeAddDocModel.next(change);
  }

  clearSelectedDoc(change: any) {
    this.clearSelectedDocs.next(change);
  }

  onSearchTextChanged(change: any) {
    this.searchTextChanged.next(change);
  }

  openDocInfoPanels(change: any) {
    this.openDocInfoPanel.next(change);
  }
}
