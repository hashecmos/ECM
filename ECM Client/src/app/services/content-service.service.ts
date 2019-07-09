import {Injectable} from '@angular/core';
import {Http, HttpModule} from '@angular/http';
import {Output, EventEmitter} from '@angular/core';
import {User} from '../models/user/user.model';
import * as global from '../global.variables';
import 'rxjs/Rx';
import {UserService} from '../services/user.service';
import {HttpClient} from "@angular/common/http";


@Injectable()
export class ContentService {
  private base_url: string;
  private user: User;

  constructor(private http: HttpClient, private us: UserService){
    this.base_url = global.base_url;
    this.user = us.getCurrentUser();
  }
  getSysTimeStamp(){
    const sysDateTime = new Date();
    return sysDateTime.getTime();
  }

  getTopFolders() {
    const url = `${global.base_url}ContentService/getTopFolders?sysdatetime=${this.getSysTimeStamp()}`;
    return this.http.get(url);
  }

  getSubFolders(id):any{
    const url = `${global.base_url}ContentService/getSubfolders?id=${id}&sysdatetime=${this.getSysTimeStamp()}`;
    return this.http.get(url);
  }

  getSearchTemplates(): any {
    const url = `${global.base_url}ContentService/getSearchTemplates&sysdatetime=${this.getSysTimeStamp()}`;
    return this.http.get(url);
  }

  getSearchTemplate(id: any):any{
    const url = `${global.base_url}ContentService/getSearchTemplate?id=${id}&sysdatetime=${this.getSysTimeStamp()}`;
    return this.http.get(url);
  }

  getEntryTemplates(): any {
    const user = this.us.getCurrentUser();
    const url = `${global.base_url}ContentService/getEntryTemplates?empNo=${user.EmpNo}&sysdatetime=${this.getSysTimeStamp()}`;
    return this.http.get(url);
  }

  getEntryTemplatesByOrgId(orgId): any {
    const user = this.us.getCurrentUser();
    const url = `${global.base_url}ContentService/getEntryTemplatesByOrgId?orgId=${orgId}&sysdatetime=${this.getSysTimeStamp()}`;
    return this.http.get(url);
  }

  getAllEntryTemplates(): any {
    const url = `${global.base_url}ContentService/getAllEntryTemplates?sysdatetime=${this.getSysTimeStamp()}`;
    return this.http.get(url);
  }

  getEntryTemplate(id: any): any {
    const user = this.us.getCurrentUser();
    const url = `${global.base_url}ContentService/getEntryTemplate?id=${id}&empNo=${user.EmpNo}&sysdatetime=${this.getSysTimeStamp()}`;
    return this.http.get(url);
  }

  getEntryTemplateForSearch(id: any): any {
    const user = this.us.getCurrentUser();
    const url = `${global.base_url}ContentService/getEntryTemplateForSearch?id=${id}&empNo=${user.EmpNo}&sysdatetime=${this.getSysTimeStamp()}`;
    return this.http.get(url);
  }

  getEntryTemplatesForSearch(): any {
    const user = this.us.getCurrentUser();
    let url = '';
    if (user){
      url = `${global.base_url}ContentService/getEntryTemplatesForSearch?empNo=${user.EmpNo}&sysdatetime=${this.getSysTimeStamp()}`;

    }
    return this.http.get(url);
  }

  addFolderToFavorites(folderId: any):any{
    const user = this.us.getCurrentUser();
    const url = `${global.base_url}ContentService/addFolderToFavorites?empno=${user.EmpNo}&id=${folderId}&sysdatetime=${this.getSysTimeStamp()}`;
    return this.http.get(url,{responseType:'text'});
  }

  getDocumentFolders(docid: any):any{
    const url = `${global.base_url}ContentService/getFolderDocuments?id=${docid}&sysdatetime=${this.getSysTimeStamp()}`;
    return this.http.get(url);
  }

  unfileFromFolder(folderid, id):any{
    const url = `${global.base_url}DocumentService/unfileFromFolder?folderid=${folderid}&id=${id}&sysdatetime=${this.getSysTimeStamp()}`;
    return this.http.get(url,{responseType:'text'});
  }

  fileInFolder(folderid, id):any{
    const url = `${global.base_url}DocumentService/fileInFolder?folderid=${folderid}&id=${id}&sysdatetime=${this.getSysTimeStamp()}`;
    return this.http.get(url,{responseType:'text'});
  }

  getFolderPermissions(folderId: any):any{
    const url = `${global.base_url}ContentService/getFolderPermissions?id=${folderId}&sysdatetime=${this.getSysTimeStamp()}`;
    return this.http.get(url);
  }

  getFavoriteFolders() {
    const user = this.us.getCurrentUser();
    const url = `${global.base_url}ContentService/getFavoriteFolders?empno=${user.EmpNo}&sysdatetime=${this.getSysTimeStamp()}`;
    return this.http.get(url);
  }

  getAccessPrivileges(mask):any{
    const user = this.us.getCurrentUser();
    const url = `${global.base_url}ContentService/getAccessPrivileges?mask=${mask}&sysdatetime=${this.getSysTimeStamp()}`;
    return this.http.get(url);
  }

  removeFolderFromFavorites(folderid):any{
    const user = this.us.getCurrentUser();
    const url = `${global.base_url}ContentService/removeFolderFromFavorites?empno=${user.EmpNo}&id=${folderid}&sysdatetime=${this.getSysTimeStamp()}`;
    return this.http.get(url,{responseType:'text'});
  }

  getEntryTemplateByOrgId(orgId):any{
    const url = `${global.base_url}ContentService/getEntryTemplatesByOrgId?orgId=${orgId}&sysdatetime=${this.getSysTimeStamp()}`;
    return this.http.get(url);
  }
   moveFolderToFolder(sourceid: any, targetid: any):any{
    const url = `${global.base_url}ContentService/moveToFolder?sourceid=${sourceid}&targetid=${targetid}&sysdatetime=${this.getSysTimeStamp()}`;
    return this.http.get(url,{responseType:'text'});
  }
}
