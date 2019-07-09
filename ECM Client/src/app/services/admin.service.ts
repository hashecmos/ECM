import {Injectable} from '@angular/core';
import {Http} from '@angular/http';
import {Output, EventEmitter} from '@angular/core';
import {User} from '../models/user/user.model';
import * as global from '../global.variables';
import 'rxjs/Rx';
import {UserService} from '../services/user.service';
import {HttpClient} from "@angular/common/http";


@Injectable()
export class AdminService {
  private base_url: string;
  private user: User;

  constructor(private http: HttpClient, private us: UserService) {
    this.base_url = global.base_url;
    this.user = us.getCurrentUser();
  }

  getSysTimeStamp() {
    const sysDateTime = new Date();
    return sysDateTime.getTime();
  }

  getAdminLogs(): any {
    const url = `${global.base_url}AdministrationService/getAdminLogs?sysdatetime=${this.getSysTimeStamp()}`;
    return this.http.get(url);
  }

  getLookups(): any {
    const url = `${global.base_url}AdministrationService/getLookups?sysdatetime=${this.getSysTimeStamp()}`;
    return this.http.get(url);
  }

  getLookupsByOrgUnit(orgId): any {
    const url = `${global.base_url}AdministrationService/getLookupsByOrgId?orgid=${orgId}&sysdatetime=${this.getSysTimeStamp()}`;
    return this.http.get(url);
  }

  getNextECMNo(): any {
    const url = `${global.base_url}AdministrationService/getNextECMNo?sysdatetime=${this.getSysTimeStamp()}`;
    return this.http.get(url, {responseType: 'text'});
  }

  getTopLevelOrgUnit(): any {
    const url = `${global.base_url}AdministrationService/getTopLevelOrgUnit?sysdatetime=${this.getSysTimeStamp()}`;
    return this.http.get(url);
  }

  getSubLevelOrgUnits(orgId): any {
    const url = `${global.base_url}AdministrationService/getSubLevelOrgUnits?orgId=${orgId}&sysdatetime=${this.getSysTimeStamp()}`;
    return this.http.get(url);
  }

  getLookupValues(id: any): any {
    const url = `${global.base_url}AdministrationService/getLookupValues?id=${id}&sysdatetime=${this.getSysTimeStamp()}`;
    return this.http.get(url);
  }

  searchLDAPUsers(searchText: any): any {
    const url = `${global.base_url}AdministrationService/searchLDAPUsers?user=${searchText}&sysdatetime=${this.getSysTimeStamp()}`;
    return this.http.get(url);
  }

  searchLDAPGroups(searchText: any): any {
    const url = `${global.base_url}AdministrationService/searchLDAPGroups?group=${searchText}&sysdatetime=${this.getSysTimeStamp()}`;
    return this.http.get(url);
  }

  saveLookup(name: any, id: any): any {
    const url = `${global.base_url}AdministrationService/saveLookup?name=${name}&id=${id}&sysdatetime=${this.getSysTimeStamp()}`;
    return this.http.get(url, {responseType: 'text'});
  }

  updateLookupValues(saveArray: any): any {
    const url = `${global.base_url}AdministrationService/updateLookupValues?sysdatetime=${this.getSysTimeStamp()}`;
    return this.http.post(url, saveArray, {responseType: 'text'});
  }

  addLookupMapping(orgUnit: any, templid: any, prop: any, lookupId: any): any {
    const url = `${global.base_url}AdministrationService/addLookupMapping?id=${lookupId}&orgUnit=${orgUnit}&templid=${templid}&prop=${prop}&sysdatetime=${this.getSysTimeStamp()}`;
    return this.http.get(url, {responseType: 'text'});
  }

  removeLookupMapping(orgUnit: any, templid: any, prop: any): any {
    const url = `${global.base_url}AdministrationService/removeLookupMapping?orgUnit=${orgUnit}&templid=${templid}&prop=${prop}&sysdatetime=${this.getSysTimeStamp()}`;
    return this.http.get(url, {responseType: 'text'});
  }

  getLookupMappings(): any {
    const url = `${global.base_url}AdministrationService/getLookupMappings?sysdatetime=${this.getSysTimeStamp()}`;
    return this.http.get(url);
  }

  searchOrgUnits(text: any): any {
    const url = `${global.base_url}AdministrationService/searchOrgUnits?text=${text}&sysdatetime=${this.getSysTimeStamp()}`;
    return this.http.get(url);
  }

  getIntegrations(): any {
    const url = `${global.base_url}IntegrationService/getIntegrations?sysdatetime=${this.getSysTimeStamp()}`;
    return this.http.get(url);
  }

  saveIntegrations(json): any {
    const url = `${global.base_url}IntegrationService/saveIntegration?sysdatetime=${this.getSysTimeStamp()}`;
    return this.http.post(url, json, {responseType: 'text'});
  }

  deleteIntegrations(id): any {
    const url = `${global.base_url}IntegrationService/deleteIntegration?id=${id}&sysdatetime=${this.getSysTimeStamp()}`;
    return this.http.get(url, {responseType: 'text'});
  }

  getOrgUnitsByEntryTemplate(etId: any): any {
    const url = `${global.base_url}AdministrationService/getOrgUnitsByEntryTemplate?etId=${etId}&sysdatetime=${this.getSysTimeStamp()}`;
    return this.http.get(url);
  }

  getLogs(): any {
    const url = `${global.base_url}AdministrationService/getLogs?sysdatetime=${this.getSysTimeStamp()}`;
    return this.http.get(url);
  }

  getLogDetails(logid): any {
    const url = `${global.base_url}AdministrationService/getLogDetails?logid=${logid}&sysdatetime=${this.getSysTimeStamp()}`;
    return this.http.get(url, {responseType: 'text'});
  }

  addEntryTemplateMapping(orgid: any, etId: any, isvisible: any, etVsId: any): any {
    const url = `${global.base_url}AdministrationService/addEntryTemplateMapping?orgid=${orgid}&etId=${etId}&isvisible=${isvisible}&etVsId=${etVsId}&sysdatetime=${this.getSysTimeStamp()}`;
    return this.http.get(url, {responseType: 'text'});
  }

  removeEntryTemplateMapping(orgId: any, etId: any): any {
    const url = `${global.base_url}AdministrationService/removeEntryTemplateMapping?orgId=${orgId}&etId=${etId}&sysdatetime=${this.getSysTimeStamp()}`;
    return this.http.get(url, {responseType: 'text'});
  }

  removeLookup(id): any {
    const url = `${global.base_url}AdministrationService/removeLookup?id=${id}&sysdatetime=${this.getSysTimeStamp()}`;
    return this.http.get(url, {responseType: 'text'});
  }

  removeLookupValue(id, lookupid): any {
    const url = `${global.base_url}AdministrationService/removeLookupValue?id=${id}&lookupid=${lookupid}&sysdatetime=${this.getSysTimeStamp()}`;
    return this.http.get(url, {responseType: 'text'});
  }

  getLookupsByOrgId(orgid): any {
    const url = `${global.base_url}AdministrationService/getLookupsByOrgId?orgid=${orgid}&sysdatetime=${this.getSysTimeStamp()}`;
    return this.http.get(url);
  }

  getLookupMappingsByOrg(orgid, etvsid): any {
    const url = `${global.base_url}AdministrationService/getLookupMappingsByOrg?orgid=${orgid}&etvsid=${etvsid}&sysdatetime=${this.getSysTimeStamp()}`;
    return this.http.get(url);
  }


}
