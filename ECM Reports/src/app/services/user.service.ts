import { Injectable } from '@angular/core';
import { Http} from '@angular/http';
import { Output, EventEmitter } from '@angular/core';
import {User} from '../models/user.model'
import * as global from '../global.variables';
import 'rxjs/Rx';


@Injectable()
export class UserService {
  private base_url: string
  private user: User;

  constructor(private http: Http) {
    this.base_url = global.base_url;
  }

  getCurrentUser(): any {
    return JSON.parse(localStorage.getItem('user'))
  }

  getUsers() {
    const user = this.getCurrentUser();
    const url = `${global.base_url}UserService/getUsers?userid=${user.EmpNo}`;
    return this.http.get(url).map(res => res.json());
  }
  getUserDelegation() {
    const user = this.getCurrentUser();
    const url = `${global.base_url}UserService/getUserDelegations?userid=${user.EmpNo}&usertype=USER`;
    return this.http.get(url).map(res => res.json());
  }
  getRoleDelegation() {
    const user = this.getCurrentUser();
    const url = `${global.base_url}UserService/getUserDelegations?userid=${user.EmpNo}&usertype=ROLE`;
    return this.http.get(url).map(res => res.json());
  }
  saveDelegation(query: any) {
    const url = `${global.base_url}UserService/saveDelegation`;
    return this.http.post(url, query);
  }
  revokeDelegation(id: any) {
    const url = `${global.base_url}UserService/getRevokeDelegation?id=${id}`;
    return this.http.get(url);
  }

  getUserList(type: any) {
    const user = this.getCurrentUser();
    const url = `${global.base_url}UserService/getUserList?userid=${user.EmpNo}&type=${type}`;
    return this.http.get(url).map(res => res.json());
  }

  getActiveRolesList() {
    const user = this.getCurrentUser();
    const url = `${global.base_url}UserService/getOrgRoles`;
    return this.http.get(url).map(res => res.json());
  }

  getRoles() {
    const user = this.getCurrentUser();
    const url = `${global.base_url}UserService/getRoles?userid=${user.userName}`;
    return this.http.get(url).map(res => res.json());
  }

  addUserList(id: any, list: any) {
    const user = this.getCurrentUser();
    const url = `${global.base_url}UserService/addUserList?userid=${user.EmpNo}&addedUser=${id}&type=${list}`;
    return this.http.get(url);
  }
  removeUserList(id: any, list: any) {
    const user = this.getCurrentUser();
    const url = `${global.base_url}UserService/removeUserList?userid=${user.EmpNo}&removeUser=${id}&type=${list}`;
    return this.http.get(url);
  }

  logIn(username: any, password: any): any {
    const url = `${global.base_url}UserService/getUserDetails?userid=${username}`;
    return this.http.get(url).map(res => res.json());
    }

}
