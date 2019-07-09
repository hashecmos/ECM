import {NewsModel} from '../../../models/admin/news.model';
import {NewsService} from '../../../services/news.service';
import {Component, OnInit, ViewEncapsulation} from '@angular/core';
import * as global from '../../../global.variables';
import {BreadcrumbService} from '../../../services/breadcrumb.service';
import {CoreService} from '../../../services/core.service';
import {GrowlService} from '../../../services/growl.service';
import {Subscription} from 'rxjs';
import {ConfirmationService} from 'primeng/primeng';
import {User} from "../../../models/user/user.model";
import {UserService} from "../../../services/user.service";

@Component({
  selector: 'app-news',
  templateUrl: './news.component.html',
  styleUrls: ['./news.component.css'],
   encapsulation: ViewEncapsulation.None,
})
export class NewsComponent implements OnInit {
  public newsModel = new NewsModel();
  public newsList: any;
  isCreate = true;
  active: any;
  expire: any;
  news: string;
  subject: string;
  emptyMessage: any;
  today: Date;
  private user: User;
  private subscriptions: Subscription[] = [];
  public itemsPerPage: any = 10;
   private allnews:any[];
   viewnews = false;
  constructor(private ns: NewsService,private userService: UserService,private confirmationService: ConfirmationService, private coreService: CoreService, private growlService: GrowlService, private breadcrumbService: BreadcrumbService) {
  }
   refresh(){
      const subscription= this.ns.getAllNews().subscribe(data => this.assignNews(data));
      this.coreService.progress = {busy: subscription, message: '', backdrop: true};
      this.addToSubscriptions(subscription);
  }

  ngOnInit() {
    this.userService.getUserSettings().subscribe(val => {
      const res:any = val;
      this.assignPagination(res);
    });
    this.today = new Date();
    this.emptyMessage = global.no_news;
    this.user = this.userService.getCurrentUser();
    this.breadcrumbService.setItems([
      {label: 'Admin'},
      {label: 'News'}
    ]);
     const subscription= this.ns.getAllNews().subscribe(data => this.assignNews(data));
      this.coreService.progress = {busy: subscription, message: '', backdrop: true};
      this.addToSubscriptions(subscription);
  }
  assignPagination(val) {
    if (val !== undefined) {
      val.map((d, i) => {
        if (d.key === 'Page Size') {
          if(d.val){
            this.itemsPerPage = parseInt(d.val,10);
          }else{
            this.itemsPerPage = 10;
          }
        }
      });
    }
  }
  assignNews(data) {
    data.map((d, i) => {
      if (d.activeDate !== undefined) {
        d.activeDate = this.coreService.formatDateForDelegate(d.activeDate);
      }
      if (d.expiryDate !== undefined) {
        d.expiryDate = this.coreService.formatDateForDelegate(d.expiryDate);
      }
       if (d.createdDate !== undefined) {
        d.createdDate = this.coreService.formatDateForDelegate(d.createdDate);
      }
      if (d.modifiedDate !== undefined) {
        d.modifiedDate = this.coreService.formatDateForDelegate(d.modifiedDate);
      }
    });
    this.newsList = data;

  }

  createNews() {
    this.newsModel.activeDate = this.active;
    this.newsModel.expiryDate = this.expire;
    this.newsModel.message = this.news;
    this.newsModel.subject = this.subject;
    this.newsModel.createdBy=this.user.fulName;

      if (this.newsModel.message !== undefined && this.newsModel.message !== '' && this.newsModel.subject !== undefined && this.newsModel.subject !== '' && this.newsModel.activeDate !== undefined && this.newsModel.expiryDate !== undefined) {
        if(this.news.length<=500) {
          this.subscriptions.push(this.ns.saveNews(this.newsModel).subscribe(data => this.saveNewsSuccess(data), err => this.saveFailed()));
        }
        else
          {
          this.growlService.showGrowl({
          severity: 'error',
          summary: 'Max Length Exceeded', detail: 'Text Entered For News Exceeds Maximum Length'
        });
        }
      }
      else {
        this.growlService.showGrowl({
          severity: 'error',
          summary: 'Fill All Inputs', detail: 'Please Fill All Inputs To Save'
        });
        //this.subscriptions.push(this.ns.getAllNews().subscribe(data=>this.assignNews(data)));
      }


  }
  confirm(event) {
    this.confirmationService.confirm({
      message: 'Are you sure that you want to remove?',
      accept: () => {
        //Actual logic to perform a confirmation
        this.removeNews(event.id);
      }
    });
  }
  removeNews(dat){
    this.ns.removeNews(dat).subscribe(data=>this.removeSuccess(),err=>this.removeFailed())
  }
  removeSuccess(){
     this.growlService.showGrowl({
          severity: 'info',
          summary: 'Success', detail: 'Removed News Successfully'
        });
     this.ns.getAllNews().subscribe(data => this.assignNews(data));
  }
  removeFailed(){
    this.growlService.showGrowl({
          severity: 'error',
          summary: 'Failure', detail: 'Remove Failed'
        });
  }


  saveNewsSuccess(data) {
    if (this.isCreate) {
      this.growlService.showGrowl({
        severity: 'info',
        summary: 'Success', detail: 'News Created'
      });
    }
    else {
      this.growlService.showGrowl({
        severity: 'info',
        summary: 'Success', detail: 'News Saved'
      });
    }

    this.subscriptions.push(this.ns.getAllNews().subscribe(res => this.assignNews(res)));
    this.expire = undefined;
    this.news = undefined;
    this.subject = undefined;
    this.active = undefined;
    this.isCreate = true;
  }

  saveFailed() {
    this.growlService.showGrowl({
      severity: 'error',
      summary: 'Failure', detail: 'Failed To Save News'
    });
  }
  viewAllNews(data){
     this.viewnews = true;
    this.allnews=data;

  }

  editNews(selnews) {
    this.isCreate = false;
    //  this.newsModel=selnews;
    this.newsModel.id = selnews.id;
    this.newsModel.subject = this.subject = selnews.subject;
    this.newsModel.message = this.news = selnews.message;
    this.newsModel.modifiedBy=this.user.fulName;
    var fromDate = new Date(selnews.activeDate);
    var toDate = new Date(selnews.expiryDate);
    this.active = fromDate;
    this.expire = toDate;

  }
  addToSubscriptions(subscription) {
    this.subscriptions.push(subscription);
  }
   exportToExcel(){
     let array=[];
    this.newsList.map(d=>{
      array=Object.keys(d);
    });
    console.log(array);
    array.shift();
    this.coreService.exportToExcel( this.newsList,'News.xlsx',array)
  }

  clearSubscriptions() {
    this.subscriptions.map(s => {
      s.unsubscribe();
    });
  }


  ngOnDestroy() {
    this.isCreate = true;
     this.clearSubscriptions();
     this.viewnews = false;
  }
}
