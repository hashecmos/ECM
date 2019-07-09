import {Injectable} from '@angular/core';
import * as global from '../global.variables';
import 'rxjs/Rx';
import {UserService} from '../services/user.service';
import {HttpClient} from "@angular/common/http";
import {Observable} from 'rxjs/Rx';
import {ResponseContentType} from "@angular/http";
import {DocumentInfoModel} from "../models/document/document-info.model";
import {TreeNode} from "primeng/primeng";

@Injectable()
export class DocumentService {
  private current_user: any;
  private base_url: string;
  public cartItems: any[] = [];
  savedFolderBrowse={folderResultsSavedBrowse:[],folderPathSavedBrowse:'',folderTreeSaved:[],setSelectedFolder:[],selectedFolderId:''};
  savedFolderFav={folderResultsSavedFav:[],folderPathSavedFav:'',folderTreeSavedFav:[],setSelectedFolder:[],selectedFolderId:''};
  savedSearch={searchResultsSaved:{continueData:undefined,totalResults:0,row:[]},advanceSearchSaved:[],simpleSearchText:'',searchCriteria:'',et:{value:'',label:''},documentClassSaved:'',ets:{props:''}};
  constructor(private http: HttpClient, private us: UserService){
    this.current_user = us.getCurrentUser();
  }

  getSysTimeStamp(){
    const sysDateTime = new Date();
    return sysDateTime.getTime();
  }

  refreshCart(data):any{
    this.cartItems.splice(0, this.cartItems.length);
    Object.assign(this.cartItems, data);
  }

  addDocument(formdata: any):any{
    const url = `${global.base_url}DocumentService/addDocument`;
    return this.http.post(url, formdata,{responseType:'text'});
  }

  addToFavorites(empno: any, id: any):any{
    const url = `${global.base_url}DocumentService/addToFavorites?empno=${empno}&id=${id}&sysdatetime=${this.getSysTimeStamp()}`;
    return this.http.get(url,{responseType:'text'});
  }
  moveMultipleDocuments(moveToFolder):any{
    const url = `${global.base_url}DocumentService/moveMultipleDocuments`;
    return this.http.post(url, moveToFolder,{responseType:'text'});
  }
  getDocument(id: any):any{
    const url = `${global.base_url}DocumentService/getDocument?id=${id}&sysdatetime=${this.getSysTimeStamp()}`;
    return this.http.get(url);

  }
  getThisDocument(id: any):any{
    const url = `${global.base_url}DocumentService/getThisDocument?id=${id}&sysdatetime=${this.getSysTimeStamp()}`;
    return this.http.get(url);

  }

  getTeamDocuments(empno: any):any{
    const url = `${global.base_url}DocumentService/getTeamDocuments?empno=${empno}&sysdatetime=${this.getSysTimeStamp()}`;
    return this.http.get(url);

  }

  getLinks(id: any):any{
    const url = `${global.base_url}DocumentService/getLinks?docid=${id}&sysdatetime=${this.getSysTimeStamp()}`;
    return this.http.get(url);

  }

  checkOut(id: any):any{
    const url = `${global.base_url}DocumentService/checkOut?id=${id}&sysdatetime=${this.getSysTimeStamp()}`;
    return this.http.get(url,{responseType:'text'});

  }

  checkIn(formdata: any):any{
    const url = `${global.base_url}DocumentService/checkIn`;
    return this.http.post(url, formdata,{responseType:'text'});
  }

  linkDocuments(id1: any, id2: any):any{
    const url = `${global.base_url}DocumentService/linkDocuments?firstid=${id1}&secondid=${id2}&sysdatetime=${this.getSysTimeStamp()}`;
    return this.http.get(url,{responseType:'text'});

  }

  removeLink(id1: any, id2: any):any{
    const url = `${global.base_url}DocumentService/removeLink?firstid=${id1}&secondid=${id2}&sysdatetime=${this.getSysTimeStamp()}`;
    return this.http.get(url,{responseType:'text'});

  }

  removeFromFavorites(empno: any, id: any):any{
    const url = `${global.base_url}DocumentService/removeFromFavorites?empno=${empno}&id=${id}&sysdatetime=${this.getSysTimeStamp()}`;
    return this.http.get(url,{responseType:'text'});
  }

  getFavourites(empno: any):any{
    const url = `${global.base_url}DocumentService/getFavorites?empno=${empno}&sysdatetime=${this.getSysTimeStamp()}`;
    return this.http.get(url);
  }

  getRecent(empno: any):any{
    const url = `${global.base_url}DocumentService/getRecent?empno=${empno}&sysdatetime=${this.getSysTimeStamp()}`;
    return this.http.get(url);

  }

  getDocumentVersions(id: any):any{
    const url = `${global.base_url}DocumentService/getDocumentVersions?id=${id}&sysdatetime=${this.getSysTimeStamp()}`;
    return this.http.get(url);

  }

  getDocumentPermissions(id: any):any{
    const url = `${global.base_url}DocumentService/getDocumentPermissions?id=${id}&sysdatetime=${this.getSysTimeStamp()}`;
    return this.http.get(url);

  }
  getDocumentAdhocPermissions(id: any):any{
    const url = `${global.base_url}DocumentService/getDocumentAdhocPermissions?id=${id}&sysdatetime=${this.getSysTimeStamp()}`;
    return this.http.get(url);

  }

  setDocumentAdhocPermissions(json):any{
    const url = `${global.base_url}DocumentService/setDocumentAdhocPermissions`;
    return this.http.post(url,json,{responseType:'text'});

  }

  getDocumentHistory(id: any):any{
    const url = `${global.base_url}DocumentService/getDocumentHistory?docid=${id}&sysdatetime=${this.getSysTimeStamp()}`;
    return this.http.get(url);

  }

  getDocumentWorkflowHistory(id: any):any{
    const url = `${global.base_url}DocumentService/getDocumentWorkflowHistory?docid=${id}&sysdatetime=${this.getSysTimeStamp()}`;
    return this.http.get(url);

  }

  getCart(id: any):any{
    const url = `${global.base_url}DocumentService/getCart?empno=${id}&sysdatetime=${this.getSysTimeStamp()}`;
    return this.http.get(url);
  }



  addToCart(empNo: any, id: any):any{
    const url = `${global.base_url}DocumentService/addToCart?empno=${empNo}&id=${id}&sysdatetime=${this.getSysTimeStamp()}`;
    return this.http.get(url,{responseType:'text'});
  }

  checkForSameNameDoc(docs,name,id){
       let exists=false;
       docs.map(doc=>{
          if(name===doc.fileName && doc.id!==id){
                      exists=true;
                    }
       });

         return exists;

  }

  removeFromCart(empNo: any, id: any):any{
    const url = `${global.base_url}DocumentService/removeFromCart?empno=${empNo}&id=${id}&sysdatetime=${this.getSysTimeStamp()}`;
    return this.http.get(url,{responseType:'text'});
  }

  searchDocuments(request: any): any {
    const url = `${global.base_url}DocumentService/pagingSearch`;
    return this.http.post(url, request);
  }

  continueSearch(request: any): any {
    const url = `${global.base_url}DocumentService/continueSearch`;
    return this.http.post(url, request);
  }

   downloadDocumentPanel(id: any): any {
    const url = `${global.base_url}DocumentService/downloadDocument?id=${id}`;
     return this.http.get(url,{responseType:"blob"});
  }

  downloadDocument(id: any): any {
    const url = `${global.base_url}DocumentService/downloadDocument?id=${id}`;
    return url;

  }

  downloadThisDocument(id: any): any {
    const url = `${global.base_url}DocumentService/downloadDocument?id=${id}`;
    return url;
  }
  validateDocument(id: any): any {
    const url = `${global.base_url}DocumentService/validateDocument?id=${id}`;
    return url;
  }

   downloadPDF(id: any): any {
    const url = `${global.base_url}DocumentService/downloadThisDocument?id=${id}`;
     return this.http.get(url,{responseType:"blob"});
  }

  downloadMultipleDocument(doc): any {
    const url = `${global.base_url}DocumentService/downloadMultipleDocuments`;
    return this.http.post(url, doc,{responseType:"blob"
    });

  }

  getRequest(reqId: any):any{
    const url = `${global.base_url}ESignService/getRequest?id=${reqId}&sysdatetime=${this.getSysTimeStamp()}`;
    return this.http.get(url);
  }

  setAccessPolicy(docid: any, apid: any, apno: any):any{
    const url = `${global.base_url}DocumentService/setAccessPolicy?docid=${docid}&apid=${apid}&apno=${apno}&sysdatetime=${this.getSysTimeStamp()}`;
    return this.http.get(url,{responseType:'text'});
  }

  updateProperties(prop: any): any {
    const url = `${global.base_url}DocumentService/updateProperties`;
    return this.http.post(url, prop,{responseType:'text'});
  }

  getViewUrl(docid: any): any {
    const url = `${global.winxt_url}?id=%7B${docid.replace('{', '').replace('}', '')}%7D&objectStoreName=UAT&objectType=document`;
    return url;
  }

  getDocumentFolders(docid: any):any{
    const url = `${global.base_url}DocumentService/getDocumentFolders?id=${docid}&sysdatetime=${this.getSysTimeStamp()}`;
    return this.http.get(url);
  }
  getDocumentInfo(docid: any):any{
    const user = this.us.getCurrentUser();
    const url = `${global.base_url}DocumentService/getDocumentInfo?id=${docid}&empno=${user.EmpNo}&sysdatetime=${this.getSysTimeStamp()}`;
    return this.http.get(url);
  }

  verifyESign(docid: any, witemid: any):any{
    const user = this.us.getCurrentUser();
    const url = `${global.base_url}ESignService/verifyESign?docid=${docid}&witemid=${witemid}&sysdatetime=${this.getSysTimeStamp()}`;
    return this.http.get(url,{responseType:'text'});
  }

  verifyESignInInterval(docid: any, witemid: any):any{
    const user = this.us.getCurrentUser();
    //const url = `${global.base_url}ESignService/verifyESign?docid=${docid}&witemid=${witemid}&sysdatetime=${this.getSysTimeStamp()}`;
    return Observable.interval(15000).switchMap(() => this.http.get(`${global.base_url}ESignService/verifyESign?docid=${docid}&witemid=${witemid}&sysdatetime=${this.getSysTimeStamp()}`,{responseType:'text'}).map((data) => data));
  }

  exportFolderDocuments(json,path){
    const jsonObject = {setCount:json.length, folderPath:path, documents:json};
    const url = `${global.base_url}DocumentService/exportFolderDocuments`;
    return this.http.post(url, jsonObject,{responseType:"blob"});
  }
}
