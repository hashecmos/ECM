import { Injectable } from '@angular/core';
import { Http, ResponseContentType} from '@angular/http';
import * as global from '../global.variables';
import 'rxjs/Rx';


@Injectable()
export class ReportService {
  private base_url: string;

  constructor(private http: Http) {
    this.base_url = global.base_url;
  }
  getTopLevelOrgUnit() {
    const url = `${global.base_url}AdministrationService/getTopLevelOrgUnit`;
    return this.http.get(url);
  }
  getSubOrgUnits(orgCode) {
    const url = `${global.base_url}ReportService/getSubOrgUnits?orgcode=${orgCode}`;
    return this.http.get(url).map(res => res.json());
  }

  getOrgWorkitemCounts(orgcode, status, fromdate, todate) {
     const url = `${global.base_url}ReportService/getOrgWorkitemCounts?orgcode=${orgcode}&status=${status}&fromdate=${fromdate}&todate=${todate}`;
    return this.http.get(url);
  }

  getOrgSentitemCounts(orgcode, status, fromdate, todate) {
     const url = `${global.base_url}ReportService/getOrgSentitemCounts?orgcode=${orgcode}&status=${status}&fromdate=${fromdate}&todate=${todate}`;
    return this.http.get(url);
  }
  getReportInboxItems(orgcode, status, fromdate, todate) {
     const query = {
      orgCode: orgcode,
      fromDate: fromdate,
      toDate: todate,
      repStatus: status,
      pageNo: 0
    };
     const url = `${global.base_url}ReportService/getReportInboxItems`;
    return this.http.post(url, query);
  }
    
  getReportSentItems(orgcode, status, fromdate, todate) {
     const query = {
      orgCode: orgcode,
      fromDate: fromdate,
      toDate: todate,
      repStatus: status,
      pageNo: 0
    };
     const url = `${global.base_url}ReportService/getReportSentItems`;
    return this.http.post(url, query);
  }
  /*saveDelegation(query: any) {
    const url = `${global.base_url}UserService/saveDelegation`;
    return this.http.post(url, query);
  }
  revokeDelegation(id: any) {
    const url = `${global.base_url}UserService/getRevokeDelegation?id=${id}`;
    return this.http.get(url);
  }*/

  exportOrgWorkitemCounts(orgcode, status, fromdate, todate, selectedOption) {
    const query = {
      orgCode: orgcode,
      fromDate: fromdate,
      toDate: todate,
      status: status,
      exportType: selectedOption
    };
    const url = `${global.base_url}ReportService/exportOrgWorkitemCounts`;
    return this.http.post(url, query, { responseType: ResponseContentType.Blob });


  }

  exportOrgSentitemCounts(orgCode, status, fromDate, toDate,selectedOption){
     const query = {
      orgCode: orgCode,
      fromDate: fromDate,
      toDate: toDate,
      status: status,
      exportType: selectedOption
    };
    const url = `${global.base_url}ReportService/exportOrgSentitemCounts`;
    return this.http.post(url, query, { responseType: ResponseContentType.Blob });
  }
  
  exportReportInboxItems(orgCode, status, fromDate, toDate,selectedOption) {
     const query = {
      orgCode: orgCode,
      fromDate: fromDate,
      toDate: toDate,
      status: status,
      exportType: selectedOption
    };
     const url = `${global.base_url}ReportService/exportReportInboxItems`;
    return this.http.post(url, query, { responseType: ResponseContentType.Blob });
  }
  
  exportReportSentItems(orgCode, status, fromDate, toDate,selectedOption) {
     const query = {
      orgCode: orgCode,
      fromDate: fromDate,
      toDate: toDate,
      status: status,
      exportType: selectedOption
    };
     const url = `${global.base_url}ReportService/exportReportSentItems`;
    return this.http.post(url, query, { responseType: ResponseContentType.Blob });
  }
  
  getOrgDocumentCounts(orgcode, fromdate, todate) {
     const url = `${global.base_url}ReportService/getOrgDocumentCounts?orgcode=${orgcode}&fromdate=${fromdate}&todate=${todate}`;
    return this.http.get(url);
  }
  
  exportOrgDocumentCounts(orgCode, status, fromDate, toDate,selectedOption) {
     const query = {
      orgCode: orgCode,
      fromDate: fromDate,
      toDate: toDate,
      exportType: selectedOption
    };
     const url = `${global.base_url}ReportService/exportOrgDocumentCounts`;
    return this.http.post(url, query, { responseType: ResponseContentType.Blob });
  }
  getOrgESignItems(orgcode, status, fromdate, todate) {
     const url = `${global.base_url}ReportService/getOrgESignItems?orgcode=${orgcode}&status=${status}&fromdate=${fromdate}&todate=${todate}`;
    return this.http.get(url);
  }
  exportOrgESignItems(orgCode, status, fromDate, toDate,selectedOption) {
     const query = {
      orgCode: orgCode,
      fromDate: fromDate,
      toDate: toDate,
      status: status,
      exportType: selectedOption
    };
     const url = `${global.base_url}ReportService/exportOrgESignItems`;
    return this.http.post(url, query, { responseType: ResponseContentType.Blob });
  }
  getUserSupervisorTree(empNo) {
    const url = `${global.base_url}UserService/getUserSupervisorTree?empNo=${empNo}`;
    return this.http.get(url).map(res => res.json());
  }
  getSubRolesList(orgid: any) {
    const sysDateTime = new Date();
    const fulldatetime = sysDateTime.getTime();
    const url = `${global.base_url}UserService/getSubOrgRoles?orgid=${orgid}&sysdatetime=${fulldatetime}`;
    return this.http.get(url).map(res => res.json());
  }
  }
