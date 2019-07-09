import {Injectable} from '@angular/core';
import * as global from '../global.variables';
import 'rxjs/Rx';
import {HttpClient} from "@angular/common/http";

@Injectable()
export class ReportService {

  constructor(private http: HttpClient) {
  }

  getSysTimeStamp(){
    const sysDateTime = new Date();
    return sysDateTime.getTime();
  }

  getSubOrgUnits(orgCode) {
    const url = `${global.base_url}ReportService/getSubOrgUnits?orgcode=${orgCode}&sysdatetime=${this.getSysTimeStamp()}`;
    return this.http.get(url);
  }

  getOrgWorkitemCount(query) {
    const url = `${global.base_url}ReportService/getOrgWorkitemCount`;
    return this.http.post(url, query);
  }

  getOrgSentitemCount(query) {
    const url = `${global.base_url}ReportService/getOrgSentitemCount`;
    return this.http.post(url, query);
  }

  getReportInboxItems(query) {
    const url = `${global.base_url}ReportService/getReportInboxItems`;
    return this.http.post(url, query);
  }

  searchInboxItems(query) {
    const url = `${global.base_url}WorkflowService/searchInbox`;
    return this.http.post(url, query);
  }

  getReportSentItems(query) {
    const url = `${global.base_url}ReportService/getReportSentItems`;
    return this.http.post(url, query);
  }

  exportOrgWorkitemCount(query) {
    const url = `${global.base_url}ReportService/exportOrgWorkitemCount`;
    return this.http.post(url, query, {responseType:"blob"});
  }

  exportOrgSentitemCount(query){
    const url = `${global.base_url}ReportService/exportOrgSentitemCount`;
    return this.http.post(url, query, {responseType:"blob"});
  }

  exportReportInboxItems(query) {
    const url = `${global.base_url}ReportService/exportReportInboxItems`;
    return this.http.post(url, query, {responseType:"blob"});
  }

  exportReportSentItems(query) {
    const url = `${global.base_url}ReportService/exportReportSentItems`;
    return this.http.post(url, query, {responseType:"blob"});
  }

  getOrgDocumentCount(query) {
    const url = `${global.report_url}ReportService/getOrgDocumentCount`;
    return this.http.post(url, query);
  }

  exportOrgDocumentCount(query) {
    const url = `${global.report_url}ReportService/exportOrgDocumentCount`;
    return this.http.post(url, query,{responseType:"blob"});
  }

  getOrgESignItems(query) {
    const url = `${global.base_url}ReportService/getOrgESignItems`;
    return this.http.post(url, query);
  }

  exportOrgESignItems(query) {
    const url = `${global.base_url}ReportService/exportOrgESignItems`;
    return this.http.post(url, query,{responseType:"blob"});
  }

  getOrgAllReportCount(query){
    const url = `${global.report_url}ReportService/getOrgAllReportCount`;
    return this.http.post(url, query);
  }

  exportOrgAllReportCount(query){
    const url = `${global.report_url}ReportService/exportOrgAllReportCount`;
    return this.http.post(url, query,{responseType:"blob"});
  }

  getRandomMaterialColor() {
    return this.materialColor();
  }

  materialColor() {
    // colors from https://github.com/egoist/color-lib/blob/master/color.json
    const colors = {
      "red": {
        "100": "#ffcdd2",
        "300": "#e57373",
        "700": "#d32f2f",
        "a400": "#ff1744",
      },
      "pink": {
        "100": "#f8bbd0",
        "300": "#f06292",
        "a200": "#ff4081",
      },
      "purple": {
        "100": "#e1bee7",
        "a200": "#e040fb",
      },
      "deepPurple": {
        "100": "#d1c4e9",
        "400": "#7e57c2",
        "a700": "#6200ea"
      },
      "indigo": {
        "200": "#9fa8da",
      },
      "blue": {
        "100": "#bbdefb",
        "600": "#1e88e5",
      },
      "lightBlue": {
        "100": "#b3e5fc",
        "700": "#0288d1",
        "900": "#01579b",
      },
      "cyan": {
        "800": "#00838f",
        "a100": "#84ffff",
      },
      "teal": {
        "200": "#80cbc4"
      },
      "green": {
        "200": "#a5d6a7",
      },
      "lightGreen": {
        "a700": "#64dd17"
      },
      "lime": {
        "100": "#f0f4c3",
        "hex": "#cddc39",
      },
      "yellow": {
        "100": "#fff9c4",
        "a400": "#ffea00",
      },
      "amber": {
        "a100": "#ffe57f",

      },
      "orange": {
        "200": "#ffcc80",
        "a700": "#ff6d00"
      },
      "deepOrange": {
        "a100": "#ff9e80"
      },
      "brown": {
        "100": "#d7ccc8",
      },
      "grey": {
        "600": "#757575",
      },
      "blueGrey": {
        "300": "#90a4ae",
      }
    }
    // pick random property
    //var property = pickRandomProperty(colors);
    const colorList = colors[this.pickRandomProperty(colors)];
    const newColorKey = this.pickRandomProperty(colorList);
    const newColor = colorList[newColorKey];
    return newColor;
  }

  randomColor() {
    return "#" + // start with a leading hash
      Math.random() // generates random number
        .toString(16) // changes that number to base 16 as a string
        .substr(2, 6); // gets 6 characters and excludes the leading "0."
  }

  pickRandomProperty(obj) {
    let result;
    let count = 0;
    for(const prop in obj){
      if(Math.random() < 1 / ++count){
        result = prop;
      }
    }
    return result;
  }


}
