import {Injectable} from '@angular/core';
import {Http} from '@angular/http';
import {Output, EventEmitter} from '@angular/core';
import {User} from '../models/user/user.model';
import * as global from '../global.variables';
import 'rxjs/Rx';
import 'rxjs/add/operator/timeout';
import {Observable} from 'rxjs/Observable';
import {AppModule} from '../app.module';
import {HttpClient} from "@angular/common/http";

@Injectable()
export class UserService {
  private base_url: string;
  private user: User;
  selectedTheme = 'bluegrey:moody';
  defaultView = 'dashboard';
  pageSize: any;
  defaultViewSubMenuExpanded: any;

  constructor(private http: HttpClient) {
    this.base_url = global.base_url;
    this.assignGeneralSettings();
  }

  getSysTimeStamp() {
    const sysDateTime = new Date();
    return sysDateTime.getTime();
  }

  saveAdminUser(adminUser): any {
    const url = `${global.base_url}UserService/saveAdminUser`;
    return this.http.post(url, adminUser, {responseType: 'text'});
  }
  saveExcludedUser(empNo,id): any {
    const url = `${global.base_url}UserService/saveExcludedUser?empNo=${empNo}&id=${id}&sysdatetime=${this.getSysTimeStamp()}`;
    return this.http.get(url, {responseType: 'text'});
  }
  getExcludedUsers(): any {
    const url = `${global.base_url}UserService/getExcludedUsers?sysdatetime=${this.getSysTimeStamp()}`;
    return this.http.get(url);
  }

  getAdminUsers(): any {
    const url = `${global.base_url}UserService/getAdminUsers?sysdatetime=${this.getSysTimeStamp()}`;
    return this.http.get(url);
  }


  assignGeneralSettings(cb?) {
    if (localStorage.getItem('user') !== null) {
      this.getUserSettings().subscribe((val: any) => {
        val.map((d, i) => {
          if (d.key === 'Default Theme') {
            this.selectedTheme = d.val;
            this.applyTheme();
          }
          if (d.key === 'Default View') {
            this.defaultView = d.val;
            localStorage.setItem('defaultView', this.defaultView);
          }
          if (d.key === 'Page Size') {
            this.pageSize = d.val;
          }
        });

        // this.applyTheme();
        if (cb) {
          cb();
        }
      });
    }
  }

  changeTheme(theme) {
    const themeLink: HTMLLinkElement = <HTMLLinkElement> document.getElementById('theme-css');
    themeLink.href = 'assets/theme/theme-' + theme + '.css';
  }

  changeLayout(theme) {
    const layoutLink: HTMLLinkElement = <HTMLLinkElement> document.getElementById('layout-css');
    layoutLink.href = 'assets/layout/css/layout-' + theme + '.css';
  }

  applyTheme(): any {
    const style = /(.+):(.+)/.exec(this.selectedTheme);
    this.changeTheme(style[1]);
    this.changeLayout(style[2]);
  }

  getCurrentUser(): any {
    return JSON.parse(localStorage.getItem('user'));
  }

  getUsers(): any {
    const user = this.getCurrentUser();
    const url = `${global.base_url}UserService/getUsers?userid=${user.EmpNo}&sysdatetime=${this.getSysTimeStamp()}`;
    return this.http.get(url);
  }

  getUserSettings(): any {
    const user = this.getCurrentUser();
    const appname = 'ECM';
    const url = `${global.base_url}UserService/getUserSettings?empNo=${user.EmpNo}&appid=${appname}&sysdatetime=${this.getSysTimeStamp()}`;
    return this.http.get(url);
  }

  getUserSearches(): any {
    const user = this.getCurrentUser();
    const appname = 'ECM';
    const url = `${global.base_url}UserService/getUserSearches?empNo=${user.EmpNo}&appid=${appname}&sysdatetime=${this.getSysTimeStamp()}`;
    return this.http.get(url);
  }

  getUserDelegation(): any {
    const user = this.getCurrentUser();
    const url = `${global.base_url}UserService/getUserDelegations?userid=${user.EmpNo}&usertype=USER&sysdatetime=${this.getSysTimeStamp()}`;
    return this.http.get(url);
  }

  getRoleDelegation(roleId): any {
    const user = this.getCurrentUser();
    const url = `${global.base_url}UserService/getUserDelegations?userid=${roleId}&usertype=ROLE&sysdatetime=${this.getSysTimeStamp()}`;
    return this.http.get(url);
  }

  saveRole(json: any): any {
    const url = `${global.base_url}UserService/saveRole`;
    return this.http.post(url, json, {responseType: 'text'});
  }

  deleteRole(roleId): any {
    const url = `${global.base_url}UserService/deleteRole?roleId=${roleId}&sysdatetime=${this.getSysTimeStamp()}`;
    return this.http.get(url, {responseType: 'text'});
  }

  saveReportUser(empNo, id, isadmin): any {
    const url = `${global.base_url}UserService/saveReportUser?empNo=${empNo}&id=${id}&isadmin=${isadmin}&sysdatetime=${this.getSysTimeStamp()}`;
    return this.http.get(url, {responseType: 'text'});

  }

  saveUser(json: any): any {

    const url = `${global.base_url}UserService/saveUser`;
    return this.http.post(url, json, {responseType: 'text'});
  }


  saveDelegation(json: any): any {
    const url = `${global.base_url}UserService/saveDelegation`;
    return this.http.post(url, json, {responseType: 'text'});
  }

  updateUserSettings(generalsettings: any): any {
    const url = `${global.base_url}UserService/updateUserSettings`;
    return this.http.post(url, generalsettings, {responseType: 'text'});
  }

  updateUserSearches(generalsettings: any): any {
    const url = `${global.base_url}UserService/updateUserSearches`;
    return this.http.post(url, generalsettings, {responseType: 'text'});
  }

  revokeDelegation(id: any): any {
    const url = `${global.base_url}UserService/revokeDelegation?id=${id}&sysdatetime=${this.getSysTimeStamp()}`;
    return this.http.get(url, {responseType: 'text'});
  }

  searchUsers(text: any): any {
    const user = this.getCurrentUser();
    const url = `${global.base_url}UserService/searchUsers?text=${text}&sysdatetime=${this.getSysTimeStamp()}`;
    return this.http.get(url);
  }

  searchEcmUsers(searchQueary: any): any {
    const url = `${global.base_url}UserService/searchECMUsers`;
    return this.http.post(url, searchQueary);
  }

  searchOrgECMUsers(searchQueary: any): any {
    const url = `${global.base_url}UserService/searchOrgECMUsers`;
    return this.http.post(url, searchQueary);
  }

  getRoleByOrgCode(empNo: any):any {
    const url = `${global.base_url}UserService/getRolesByOrgCode?empno=${empNo}`;
    return this.http.get(url);
  }

  searchUsersList(usertype: any, text: any, key: any, filter: any): any {
    const user = this.getCurrentUser();
    const url = `${global.base_url}UserService/searchUsers?key=${key}&text=${text}&usertype=${usertype}&filter=${filter}&sysdatetime=${this.getSysTimeStamp()}`;
    return this.http.get(url);
  }

  removeDistList(listId: any, empno: any): any {
    const url = `${global.base_url}UserService/removeUserList?empno=${empno}&list=${listId}&sysdatetime=${this.getSysTimeStamp()}`;
    return this.http.get(url, {responseType: 'text'});
  }

  getActiveRolesList(): any {
    const user = this.getCurrentUser();
    const url = `${global.base_url}UserService/getOrgRoles&sysdatetime=${this.getSysTimeStamp()}`;
    return this.http.get(url);
  }

  getTopRolesList(): any {
    const url = `${global.base_url}UserService/getTopOrgRole?sysdatetime=${this.getSysTimeStamp()}`;
    return this.http.get(url);
  }

  getSubRolesList(orgid: any): any {
    const url = `${global.base_url}UserService/getSubOrgRoles?orgid=${orgid}&sysdatetime=${this.getSysTimeStamp()}`;
    return this.http.get(url);
  }

  getRoles(): any {
    const user = this.getCurrentUser();
    const url = `${global.base_url}UserService/getRoles?userid=${user.userName}&sysdatetime=${this.getSysTimeStamp()}`;
    return this.http.get(url);
  }

  getRolesByType(typeId: number): any {
    const user = this.getCurrentUser();
    const url = `${global.base_url}UserService/getRolesByType?userid=${user.userName}&type=${typeId}&sysdatetime=${this.getSysTimeStamp()}`;
    return this.http.get(url);
  }

  getUserRoles(userid): any {
    const url = `${global.base_url}UserService/getUserRoles?userid=${userid}&sysdatetime=${this.getSysTimeStamp()}`;
    return this.http.get(url);
  }

  getRoleMembers(roleid): any {
    const user = this.getCurrentUser();
    const url = `${global.base_url}UserService/getRoleMembers?roleId=${roleid}&sysdatetime=${this.getSysTimeStamp()}`;
    return this.http.get(url);
  }

  addUserList(id: any, list: any): any {
    const user = this.getCurrentUser();
    const url = `${global.base_url}UserService/addUserList?userid=${user.EmpNo}&addedUser=${id}&type=${list}&sysdatetime=${this.getSysTimeStamp()}`;
    return this.http.get(url);
  }

  removeUserList(id: any, list: any): any {
    const user = this.getCurrentUser();
    const url = `${global.base_url}UserService/removeUserList?userid=${user.EmpNo}&removeUser=${id}&type=${list}&sysdatetime=${this.getSysTimeStamp()}`;
    return this.http.get(url);
  }

  logIn(username: any, password: any): any {
    const url = `${global.base_url}UserService/getUserDetails?userid=${username}&sysdatetime=${this.getSysTimeStamp()}`;
    return this.http.get(url);
  }

  exit(): any {
    // console.log('timeout')
    //  window.location.assign('http://192.168.8.101:9080/ECMService/success.html');
  }

  // new
  getUserLists(isglobal): any {
    const user = this.getCurrentUser();
    const url = `${global.base_url}UserService/getUserLists?empno=${user.EmpNo}&global=${isglobal}&sysdatetime=${this.getSysTimeStamp()}`;
    return this.http.get(url);
  }

  getListUsers(listId: any): any {
    const user = this.getCurrentUser();
    const url = `${global.base_url}UserService/getListUsers?empno=${user.EmpNo}&list=${listId}&sysdatetime=${this.getSysTimeStamp()}`;
    return this.http.get(url);
  }

  updateUserList(updatedUserList: any): any {
    const url = `${global.base_url}UserService/updateUserList?sysdatetime=${this.getSysTimeStamp()}`;
    return this.http.post(url, updatedUserList, {responseType: 'text'});
  }

  updateUserLists(updatedUserList: any): any {
    const url = `${global.base_url}UserService/updateUserList?sysdatetime=${this.getSysTimeStamp()}`;
    return this.http.post(url, updatedUserList, {responseType: 'text'});
  }

  addUserToRole(empNo: any, roleId: any): any {
    const url = `${global.base_url}UserService/addUserToRole?empNo=${empNo}&roleId=${roleId}&sysdatetime=${this.getSysTimeStamp()}`;
    return this.http.get(url, {responseType: 'text'});
  }

  removeUserFromRole(empNo: any, roleId: any): any {
    const url = `${global.base_url}UserService/removeUserFromRole?empNo=${empNo}&roleId=${roleId}&sysdatetime=${this.getSysTimeStamp()}`;
    return this.http.get(url, {responseType: 'text'});
  }

  getUserSupervisorTree(empNo): any {
    const url = `${global.base_url}UserService/getUserSupervisorTree?empNo=${empNo}&sysdatetime=${this.getSysTimeStamp()}`;
    return this.http.get(url);
  }

  getECMUsers(): any {
    const url = `${global.base_url}UserService/getUsers?sysdatetime=${this.getSysTimeStamp()}`;
    return this.http.get(url);
  }

  getReportUsers(): any {
    const url = `${global.base_url}UserService/getReportUsers?sysdatetime=${this.getSysTimeStamp()}`;
    return this.http.get(url);
  }

  validateDelegation(delid): any {
    const url = `${global.base_url}UserService/validateDelegation?delid=${delid}&sysdatetime=${this.getSysTimeStamp()}`;
    return this.http.get(url, {responseType: 'text'});
  }
}


