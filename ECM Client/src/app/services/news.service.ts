import {Injectable} from '@angular/core';
import {Http, Response} from '@angular/http';
import {Output, EventEmitter} from '@angular/core';
import {User} from '../models/user/user.model';
import * as global from '../global.variables';
import 'rxjs/Rx';
import {HttpClient} from "@angular/common/http";


@Injectable()
export class NewsService {
  private base_url: string;
  private user: User;

  constructor(private http: HttpClient){
  }
  getSysTimeStamp(){
    const sysDateTime = new Date();
    return sysDateTime.getTime();
  }
  getNews(userLogin: any):any{
    const url = `${global.base_url}NewsService/getNews?userid=${userLogin}&sysdatetime=${this.getSysTimeStamp()}`;
    return this.http.get(url);
  }

  saveNews(newsArray):any{
    const url = `${global.base_url}NewsService/saveNews?sysdatetime=${this.getSysTimeStamp()}`;
    return this.http.post(url, newsArray,{responseType:'text'});
  }

  getAllNews() {
    const url = `${global.base_url}NewsService/getAllNews?sysdatetime=${this.getSysTimeStamp()}`;
    return this.http.get(url);
  }
  removeNews(id) {
    const url = `${global.base_url}NewsService/removeNews?id=${id}&sysdatetime=${this.getSysTimeStamp()}`;
    return this.http.get(url,{responseType:'text'});
  }
}
