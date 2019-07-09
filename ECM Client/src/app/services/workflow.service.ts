import {Injectable} from '@angular/core';
import {Http, Headers, ResponseContentType} from '@angular/http';
import {Output, EventEmitter} from '@angular/core';
import * as global from '../global.variables';
import 'rxjs/Rx';
import {UserService} from '../services/user.service';
import {HttpClient} from "@angular/common/http";
import {DocumentInfoModel} from "../models/document/document-info.model";

@Injectable()
export class WorkflowService {
  private current_user: any;
  public inboxSelectedUserTab: any;
  public sentSelectedUserTab: any;
  public archiveSelectedUserTab: any;
  inboxMenu={label: 'Inbox', icon: 'work', routerLink: ['/workflow/inbox'],badge:0,command: (event) => {
              localStorage.removeItem('openWkItem')
            }};
  draftMenu= {label: 'Drafts', icon: 'drafts', routerLink: ['/workflow/draft'], routerLinkActiveOptions: {exact: true},badge:0};
  public delegateId;
  public pageNoSelected;
  public first;
  constructor(private http: HttpClient, private us: UserService){
    this.current_user = us.getCurrentUser();
  }

  getSysTimeStamp(){
    const sysDateTime = new Date();
    return sysDateTime.getTime();
  }

  launchWorkflow(workflow: any): any {
    const url = `${global.base_url}WorkflowService/launchWorkflow`;
    return this.http.post(url, workflow);
  }

  addUserWorkitem(workitem: any): any {
    const url = `${global.base_url}WorkflowService/addUserWorkitem`;
    return this.http.post(url, workitem,{responseType:'text'});
  }

  finishWorkitem(id: any): any {
    const roleId = 0;
    const url = `${global.base_url}WorkflowService/finishWorkitem?witmid=${id}&empNo=${this.current_user.EmpNo}&roleId=${roleId}&sysdatetime=${this.getSysTimeStamp()}`;
    return this.http.get(url,{responseType:'text'});
  }

  archiveWorkitem(id: any): any {
    const roleId = 0;
    const url = `${global.base_url}WorkflowService/archiveWorkitem?witmid=${id}&empNo=${this.current_user.EmpNo}&roleId=${roleId}&sysdatetime=${this.getSysTimeStamp()}`;
    return this.http.get(url,{responseType:'text'});
  }


  archiveSentitem(id: any): any {
    let roleId = 0;
    const url = `${global.base_url}WorkflowService/archiveSentitem?sitmid=${id}&empNo=${this.current_user.EmpNo}&roleId=${roleId}&sysdatetime=${this.getSysTimeStamp()}`;
    return this.http.get(url,{responseType:'text'});
  }

  getEntryTemplateForSearchId(id: any): any {
    const user = this.us.getCurrentUser();
    const url = `${global.base_url}ContentService/getEntryTemplateForSearch?id=${id}&empNo=${user.EmpNo}&sysdatetime=${this.getSysTimeStamp()}`;
    return this.http.get(url);
  }
  validateWorkitem(id: any): any {
    const user = this.us.getCurrentUser();
    const url = `${global.base_url}WorkflowService/validateWorkitem?id=${id}&sysdatetime=${this.getSysTimeStamp()}`;
    return this.http.get(url,{responseType:'text'});
  }
  getEntryTemplatesForSearch(): any {
    const user = this.us.getCurrentUser();
    const url = `${global.base_url}ContentService/getEntryTemplatesForSearch?empNo=${user.EmpNo}&sysdatetime=${this.getSysTimeStamp()}`;
    return this.http.get(url);
  }

  getEntryTemplates(): any {
    const user = this.us.getCurrentUser();
    const url = `${global.base_url}ContentService/getEntryTemplates?empNo=${user.EmpNo}&sysdatetime=${this.getSysTimeStamp()}`;
    return this.http.get(url);
  }

  getEntryTemplatesId(id: any): any {
    const user = this.us.getCurrentUser();
    const url = `${global.base_url}ContentService/getEntryTemplate?id=${id}&empNo=${user.EmpNo}&sysdatetime=${this.getSysTimeStamp()}`;
    return this.http.get(url);
  }

  forwardWorkflow(workitem: any): any {
    const url = `${global.base_url}WorkflowService/forwardWorkitem`;
    return this.http.post(url, workitem,{responseType:'text'});
  }

  replyWorkflow(workitem: any): any {
    const url = `${global.base_url}WorkflowService/replyWorkitem`;
    return this.http.post(url, workitem,{responseType:'text'});
  }

  replyAllWorkflow(workitem: any): any {
    const url = `${global.base_url}WorkflowService/replyAllWorkitem`;
    return this.http.post(url, workitem,{responseType:'text'});
  }

  recallSentitem(id: any): any {
    const url = `${global.base_url}WorkflowService/recallSentItem?sitmid=${id}&empNo=${this.current_user.EmpNo}&sysdatetime=${this.getSysTimeStamp()}`;
    return this.http.get(url,{responseType:'text'});
  }

  recallWorkitem(workItem: any): any {
    const url = `${global.base_url}WorkflowService/recallWorkitems`;
    return this.http.post(url, workItem,{responseType:'text'});
  }

  readWorkitem(id: any): any {
    const url = `${global.base_url}WorkflowService/readWorkitem?witmid=${id}&empNo=${this.current_user.EmpNo}&sysdatetime=${this.getSysTimeStamp()}`;
    return this.http.get(url);
  }

  getWorkflow(id: any): any {
    const url = `${global.base_url}WorkflowService/getWorkflow?id=${id}&sysdatetime=${this.getSysTimeStamp()}`;
    return this.http.get(url);
  }

  getWorkitem(id: any, empID: any): any {
    const url = `${global.base_url}WorkflowService/getWorkitemDetails?witmid=${id}&empNo=${empID}&sysdatetime=${this.getSysTimeStamp()}`;
    return this.http.get(url);
  }

  getWorkitemHistory(id: any): any {
    const user = this.us.getCurrentUser();
    const url = `${global.base_url}WorkflowService/getWorkitemHistory?witmid=${id}&empNo=${user.EmpNo}&sysdatetime=${this.getSysTimeStamp()}`;
    return this.http.get(url);
  }
  getSentItemHistory(id: any): any {
    const user = this.us.getCurrentUser();
    const url = `${global.base_url}WorkflowService/getSentitemHistory?sitmid=${id}&empNo=${user.EmpNo}&sysdatetime=${this.getSysTimeStamp()}`;
    return this.http.get(url);
  }

  getUserInbox(empNo: any, pageNo: any,sort?:string,order?:string): any {
    let url = `${global.base_url}WorkflowService/getUserInbox?empNo=${empNo}&pageNo=${pageNo}&sysdatetime=${this.getSysTimeStamp()}`;
    if(sort && order){
      url=url+'&sort='+sort+'&order='+order;
    }
    return this.http.get(url);
  }

  getDrafts(empNo: any, type: any): any {
    const url = `${global.base_url}WorkflowService/getDraftItems?userid=${empNo}&usertype=${type}&sysdatetime=${this.getSysTimeStamp()}`;
    return this.http.get(url);
  }

  getUserArchiveInbox(empNo: any, pageNo: any,sort?:string,order?:string): any {
    let url = `${global.base_url}WorkflowService/getUserArchiveInbox?empNo=${empNo}&pageNo=${pageNo}&sysdatetime=${this.getSysTimeStamp()}`;
    if(sort && order){
      url=url+'&sort='+sort+'&order='+order;
    }
    return this.http.get(url);
  }

  getUserArchiveSentItems(empNo: any, pageNo: any,sort?:string,order?:string): any {
    let url = `${global.base_url}WorkflowService/getUserArchiveSentItems?empNo=${empNo}&pageNo=${pageNo}&sysdatetime=${this.getSysTimeStamp()}`;
    if(sort && order){
      url=url+'&sort='+sort+'&order='+order;
    }
    return this.http.get(url);
  }

  getRoleArchiveInbox(role: any, empNo: any, pageNo: any,sort?:string,order?:string): any {
    let url = `${global.base_url}WorkflowService/getRoleArchiveInbox?roleId=${role}&empNo=${empNo}&pageNo=${pageNo}&sysdatetime=${this.getSysTimeStamp()}`;
    if(sort && order){
      url=url+'&sort='+sort+'&order='+order;
    }
    return this.http.get(url);
  }

  getRoleArchiveSentItems(role: any, empNo: any, pageNo: any,sort?:string,order?:string): any {
    let url = `${global.base_url}WorkflowService/getRoleArchiveSentItems?roleId=${role}&empNo=${empNo}&pageNo=${pageNo}&sysdatetime=${this.getSysTimeStamp()}`;
    if(sort && order){
      url=url+'&sort='+sort+'&order='+order;
    }
    return this.http.get(url);
  }

  getActions(empNo: any): any {
    const url = `${global.base_url}WorkflowService/getActions?empNo=${empNo}&sysdatetime=${this.getSysTimeStamp()}`;
    return this.http.get(url);
  }

  getUserWorkflows(empNo: any): any {
    const url = `${global.base_url}WorkflowService/getUserWorkflows?empNo=${empNo}&sysdatetime=${this.getSysTimeStamp()}`;
    return this.http.get(url);
  }

  getUserSentItems(empNo: any, pageNo: any,sort?:string,order?:string): any {
    let url = `${global.base_url}WorkflowService/getUserSentItems?empNo=${empNo}&pageNo=${pageNo}&sysdatetime=${this.getSysTimeStamp()}`;
    if(sort && order){
      url=url+'&sort='+sort+'&order='+order;
    }
    return this.http.get(url);
  }

  getWorkitemStats(Id: any, userType: any, reportType: any, itemType: any, workitemType): any {
    const url = `${global.base_url}WorkflowService/getWorkitemStats?userId=${Id}&userType=${userType}&reportType=${reportType}&itemType=${itemType}&dType=${workitemType}&sysdatetime=${this.getSysTimeStamp()}`;
    return this.http.get(url);
  }

  getSentItemsWorkitems(wiID: any, empNo: any, status: any): any {
    const url = `${global.base_url}WorkflowService/getSentItemWorkItems?witmid=${wiID}&empNo=${empNo}&status=${status}&sysdatetime=${this.getSysTimeStamp()}`;
    return this.http.get(url);
  }

  getRoleSentItems(roleId: any, empNo: any, pageNo: any,sort?:any,order?:any): any {
    let url = `${global.base_url}WorkflowService/getRoleSentItems?roleId=${roleId}&empNo=${empNo}&pageNo=${pageNo}&sysdatetime=${this.getSysTimeStamp()}`;
    if(sort && order){
      url=url+'&sort='+sort+'&order='+order;
    }
    return this.http.get(url);
  }

  getRoleInbox(roleId: any, empNo: any, pageNo: any,sort?:string,order?:string): any {
    let url = `${global.base_url}WorkflowService/getRoleInbox?empNo=${empNo}&roleId=${roleId}&pageNo=${pageNo}&sysdatetime=${this.getSysTimeStamp()}`;
    if(sort && order){
      url=url+'&sort='+sort+'&order='+order;
    }
    return this.http.get(url);
  }

  getSearchTemplateDocuments(search: any):any{
    const url = `${global.base_url}WorkflowService/getSearchTemplateDocuments`;
    return this.http.post(url, search);
  }

  searchInbox(query: any):any{
    const url = `${global.base_url}WorkflowService/searchInbox`;
    return this.http.post(url, query);
  }

  exportInbox(query: any):any{
    const url = `${global.base_url}WorkflowService/exportInbox`;
    return this.http.post(url, query,{responseType:"blob"});
  }

  searchSentUser(query: any):any{
    const url = `${global.base_url}WorkflowService/searchSentItems`;
    return this.http.post(url, query);
  }

  searchActionedItems(query: any):any{
    const url = `${global.base_url}WorkflowService/searchActionedItems`;
    return this.http.post(url, query);
  }

  exportSent(query: any):any{
    const url = `${global.base_url}WorkflowService/exportSentItems`;
    return this.http.post(url, query,{responseType:"blob"});
  }

  exportActioned(query: any):any{
    const url = `${global.base_url}WorkflowService/exportActioned`;
    return this.http.post(url, query,{responseType:"blob"});
  }

  getInboxFilterUsers(userid: any, usertype: any, status: any): any {
    const url = `${global.base_url}WorkflowService/getInboxFilterUsers?userid=${userid}&usertype=${usertype}&status=${status}&sysdatetime=${this.getSysTimeStamp()}`;
    return this.http.get(url);
  }

  getSentitemFilterUsers(userid: any, usertype: any, status: any): any {
    const url = `${global.base_url}WorkflowService/getSentitemFilterUsers?userid=${userid}&usertype=${usertype}&status=${status}&sysdatetime=${this.getSysTimeStamp()}`;
    return this.http.get(url);
  }

  finishWorkitemBefore(empNo: any, roleId: any, bDate: any): any {
    const user = this.us.getCurrentUser();
    const url = `${global.base_url}WorkflowService/finishWorkitemBefore?empNo=${empNo}&roleId=${roleId}&bDate=${bDate}&sysdatetime=${this.getSysTimeStamp()}`;
    return this.http.get(url,{responseType:'text'});
  }

  archiveWorkitemBefore(empNo: any, roleId: any, bDate: any): any {
    const user = this.us.getCurrentUser();
    const url = `${global.base_url}WorkflowService/archiveWorkitemBefore?empNo=${empNo}&roleId=${roleId}&bDate=${bDate}&sysdatetime=${this.getSysTimeStamp()}`;
    return this.http.get(url,{responseType:'text'});
  }

  archiveSentitemBefore(empNo: any, roleId: any, bDate: any): any {
    const user = this.us.getCurrentUser();
    const url = `${global.base_url}WorkflowService/archiveSentitemBefore?empNo=${empNo}&roleId=${roleId}&bDate=${bDate}&sysdatetime=${this.getSysTimeStamp()}`;
    return this.http.get(url,{responseType:'text'});
  }

  getWorkitemProgress(workitemId): any {
    const user = this.us.getCurrentUser();
    const url = `${global.base_url}WorkflowService/getWorkitemProgress?witmid=${workitemId}&sysdatetime=${this.getSysTimeStamp()}`;
    return this.http.get(url);
  }
  getUserNewWorkitems(): any {
    const user = this.us.getCurrentUser();
    const url = `${global.base_url}WorkflowService/getUserNewWorkitems?empNo=${user.EmpNo}&sysdatetime=${this.getSysTimeStamp()}`;
    return this.http.get(url);
  }

  removeWorkitemProgress(id): any {
    const url = `${global.base_url}WorkflowService/removeWorkitemProgress?id=${id}&sysdatetime=${this.getSysTimeStamp()}`;
    return this.http.get(url,{responseType:'text'});
  }

  addWorkitemProgress(message: string, empNo: any, workitemId: any): any {
    const sysDateTime = new Date();
    const url = `${global.base_url}WorkflowService/saveWorkitemProgress`;
    return this.http.post(url,{message:message,empNo:empNo,workitemId:workitemId},{responseType:'text'});
  }
  updateInboxCount(){
    const user = this.us.getCurrentUser();
    this.getUserNewWorkitems().subscribe(
        data => {
          this.inboxMenu.badge = data.totalCount;
        });

  }
  updateDraftsCount(){
       const user = this.us.getCurrentUser();
       this.getDrafts(user.EmpNo, 'USER').subscribe(data=>
      {
       this.draftMenu.badge = data.length;
      });
  }

}
