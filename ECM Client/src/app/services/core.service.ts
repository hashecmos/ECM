import {Injectable} from '@angular/core';
import * as json2xls from 'json2xls';
import {saveAs} from 'file-saver';
import 'rxjs/Rx';
import {Observable} from "rxjs/Observable";

@Injectable()
export class CoreService {
  public progress:any={};
  public progress2:any={};
  isAdvanced='N';
  constructor() {
    const source = Observable.of(1).subscribe();
    this.progress={busy:source,message:'',backdrop:true};
  }

  convertToTimeInbox(date){
    const d=/(\d+)\/(\d+)\/(\d+)\s+(\d+):(\d+)\s+(.+)/.exec(date);
    const d2=[];
    let val=null;
    if(d && d!==null){
      for(let i=1;i<6;i++){
        d2[i]=parseInt(d[i],null);
      }
      if(d[6].toLowerCase()==='pm' && d2[4]<12){
      d2[4]=d2[4]+12;
    }else if(d[6].toLowerCase()==='am' && d2[4]===12){
        d2[4]=0;
      }

      val=new Date(d2[3],(d2[2]-1),d2[1],d2[4],d2[5],0).getTime();
    }
    return val;

  }

  convertToTimeFolder(date){
    const d=/(\d+)\/(\d+)\/(\d+)\s+(\d+):(\d+):(\d+)/.exec(date);
    const d2=[];
    let val=null;
    if(d && d!==null){
      for(let i=1;i<7;i++){
        d2[i]=parseInt(d[i],null);
      }

      val=new Date(d2[3],(d2[2]-1),d2[1],d2[4],d2[5],d2[6]).getTime();
    }

    return val;

  }

  formatDate(date) {
    const d = new Date(date);
    let str = '';
    if (d.getDate() < 10) {
      str = str + '0' + d.getDate();
    } else {
      str = str + d.getDate();
    }
    if (d.getMonth() < 9) {
      str = str + '0' + (d.getMonth() + 1)
    } else {
      str = str + (d.getMonth() + 1)
    }
    str = str + d.getFullYear() + 'T000000Z';
    return str;
  }
   formatDateForSearch(date) {
    const d = new Date(date);
    let str = '';
    if (d.getMonth() < 9) {
      str = str + '0' + (d.getMonth() + 1)
    } else {
      str = str + (d.getMonth() + 1)
    }
    if (d.getDate() < 10) {
      str = str + '0' + d.getDate();
    } else {
      str = str + d.getDate();
    }
    str =  d.getFullYear() + str + 'T210000Z';
    return str;
  }

  formatDateForDelegate(date) {
    const d = new Date(date);
    let str = '';
    if (d.getDate() < 10) {
      str = str + '0' + d.getDate();
    } else {
      str = str + d.getDate();
    }
    if (d.getMonth() < 9) {
      str = '0' + (d.getMonth() + 1) + "-" + str
    } else {
      str = (d.getMonth() + 1) + "-" + str
    }
    str = d.getFullYear() + "-" + str;
    return str;
  }
   formatDateForDelegateDDMMYY(date) {
     var initial = date.split(/\//);
     let str ='';
      str=( [ initial[2],initial[1], initial[0],  ].join('-')); //=> 'mm/dd/yyyy'
     return str;
  }

  formatDateForFinishBefore(date) {
    const d = new Date(date);
    let str = '';
    if (d.getDate() < 10) {
      str = str + '0' + d.getDate();
    } else {
      str = str + d.getDate();
    }
    if (d.getMonth() < 9) {
      str = str + "/" + '0' + (d.getMonth() + 1)
    } else {
      str = str + "/" + (d.getMonth() + 1)
    }
    str = str + "/" + d.getFullYear();
    return str;
  }

  formatDateForLaunch(date) {
    const d = new Date(date);
    let str = '';
    let suffix = 'AM';
    if (d.getHours() >= 12) {
      suffix = 'PM';
    }
    if (d.getDate() < 10) {
      str = str + '0' + d.getDate();
    } else {
      str = str + d.getDate();
    }
    if (d.getMonth() < 9) {
      str = str + '-0' + (d.getMonth() + 1)
    } else {
      str = str + '-' + (d.getMonth() + 1)
    }
    str = str + '-' + d.getFullYear() + ' ';

    if (d.getHours() < 10) {
      str = str + '0' + d.getHours();
    }
    else if (d.getHours() > 12 && d.getHours() < 22) {
      str = str + '0' + (d.getHours() - 12);
    } else if (d.getHours() > 21) {
      str = str + (d.getHours() - 12);
    } else {
      str = str + d.getHours();
    }
    str = str + ':';
    if (d.getMinutes() < 9) {
      str = str + '0' + d.getMinutes();
    } else {
      str = str + d.getMinutes();
    }

    str = str + ' ' + suffix;

    return str;
  }

  formatDateForFileName(date) {
    const d = new Date(date);
    let str = '';
    if (d.getDate() < 10) {
      str = str + '0' + d.getDate();
    } else {
      str = str + d.getDate();
    }
    if (d.getMonth() < 9) {
      str = '0' + (d.getMonth() + 1) + str
    } else {
      str = (d.getMonth() + 1) + str
    }
    str = d.getFullYear() + str;
    return str;
  }

  exportToExcel(data, fileName, exportFields) {
    const xls = json2xls(data,{fields: exportFields});
    const file = new Blob([this.s2ab(xls)], {type: 'application/vnd.ms-excel'});
    saveAs(file, fileName);
  }

  s2ab(s) {
    const buf = new ArrayBuffer(s.length);
    const view = new Uint8Array(buf);
    for (let i = 0; i !== s.length; ++i){view[i] = s.charCodeAt(i) & 0xFF;}
    return buf;
  }


}


