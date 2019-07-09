import {Injectable} from '@angular/core';
import {Http} from '@angular/http';
import {Output, EventEmitter} from '@angular/core';
import {User} from '../models/user/user.model';
import * as global from '../global.variables';
import 'rxjs/Rx';
import {UserService} from '../services/user.service';
import {HttpClient} from "@angular/common/http";


@Injectable()
export class ConfigurationService {
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
  getAllConfigurations(scope): any {
    const url = `${global.base_url}ConfigurationService/getConfigurationsForUpdate?appId=ECM&scope=${scope}&sysdatetime=${this.getSysTimeStamp()}`;
    return this.http.get(url);
  }

  updateConfigurationRow(val: any):any{
    const url = `${global.base_url}ConfigurationService/updateConfigurations?sysdatetime=${this.getSysTimeStamp()}`;
    return this.http.post(url, val,{responseType:'text'});
  }
}
