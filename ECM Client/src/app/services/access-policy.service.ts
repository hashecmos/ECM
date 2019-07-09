import {Injectable} from '@angular/core';
import {Http} from '@angular/http';
import {Output, EventEmitter} from '@angular/core';
import {User} from '../models/user/user.model';
import * as global from '../global.variables';
import 'rxjs/Rx';
import {UserService} from '../services/user.service';
import {HttpClient} from "@angular/common/http";


@Injectable()
export class AccessPolicyService {
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

  getAccessPolicyPermissions(id: any, objType: any):any{
    const url = `${global.base_url}AccessPolicyService/getAccessPolicyPermissions?objId=${id}&objType=${objType}&sysdatetime=${this.getSysTimeStamp()}`;
    return this.http.get(url);
  }

  getAccessPolicies(empno): any {
    const url = `${global.base_url}AccessPolicyService/getAllAccessPolicies?empNo=${empno}&sysdatetime=${this.getSysTimeStamp()}`;
    return this.http.get(url);
  }

  getAllAccessPolicies(): any {
    const url = `${global.base_url}AccessPolicyService/getAllAccessPolicies?sysdatetime=${this.getSysTimeStamp()}`;
    return this.http.get(url);
  }

  addAccessPolicy(jsonstring: any):any{
    const url = `${global.base_url}AccessPolicyService/addAccessPolicy?sysdatetime=${this.getSysTimeStamp()}`;
    return this.http.post(url, jsonstring,{responseType:'text'});
  }

  setPermissions(jsonstring: any):any{
    const url = `${global.base_url}AccessPolicyService/setAccessPolicyPermissions?sysdatetime=${this.getSysTimeStamp()}`;
    return this.http.post(url, jsonstring,{responseType:'text'});
  }

  getAccessPoliciesByOrgId(orgId):any{
    const url = `${global.base_url}AccessPolicyService/getAccessPoliciesByOrgId?orgId=${orgId}&sysdatetime=${this.getSysTimeStamp()}`;
    return this.http.get(url);
  }

  addAccessPolicyMapping(etId, apId):any{
    const url = `${global.base_url}AccessPolicyService/addAccessPolicyMapping?etVsId=${etId}&apId=${apId}&sysdatetime=${this.getSysTimeStamp()}`;
    return this.http.get(url,{responseType:'text'});
  }

  getAccessPolicyMappings(orgId):any{
    const url = `${global.base_url}AccessPolicyService/getAccessPolicyMappings?orgId=${orgId}&sysdatetime=${this.getSysTimeStamp()}`;
    return this.http.get(url);
  }

  removeAccessPolicyMapping(mappingid):any{
    const url = `${global.base_url}AccessPolicyService/removeAccessPolicyMapping?mappingid=${mappingid}&sysdatetime=${this.getSysTimeStamp()}`;
    return this.http.get(url,{responseType:'text'});
  }

  removeAccessPolicy(apid){
    const url = `${global.base_url}AccessPolicyService/removeAccessPolicy?apid=${apid}&sysdatetime=${this.getSysTimeStamp()}`;
    return this.http.get(url,{responseType:'text'});
  }
}
