import {Component, OnDestroy, OnInit} from '@angular/core';
import {UserService} from '../../../services/user.service';
import {User} from '../../../models/user/user.model';
import {BreadcrumbService} from '../../../services/breadcrumb.service';
import {CoreService} from '../../../services/core.service';
import {SelectItem} from 'primeng/primeng';
import {GrowlService} from '../../../services/growl.service';

@Component({
  selector: 'app-ecm-users',
  templateUrl: './ecm-users.component.html',
  styleUrls: ['./ecm-users.component.css']
})
export class EcmUsersComponent implements OnInit, OnDestroy {
  ecmUserList:User[];
  colHeaders: any[];
  itemsPerPage: any = 15;
  esignSelect: SelectItem[];
  adminSelect:SelectItem[];
  initialSelect: SelectItem[];
  private subscriptions: any[] = [];
  userModel= new User();
  viewuser=false;
  private allusers:any[];
  constructor(private us: UserService,private coreService:CoreService,private breadcrumbService: BreadcrumbService ,
              private growlService: GrowlService) { }

 refresh(){
     const subscription = this.us.getECMUsers().subscribe(data=>this.assignUsers(data));
    this.coreService.progress = {busy: subscription, message: '', backdrop: true};
    this.addToSubscriptions(subscription);
  }
  ngOnInit() {
    this.us.getUserSettings().subscribe(val => {
      const res:any = val;
      this.assignPagination(res);
    });
    this.esignSelect = [];
    this.initialSelect = [];
    this.adminSelect = [];
     this.breadcrumbService.setItems([
       {label: 'Admin'},
      {label: 'ECM-Users'}
    ]);
    this.esignSelect.push({label: 'Yes', value: 1});
    this.esignSelect.push({label: 'No', value: 0});
    this.initialSelect.push({label: 'Yes', value: 1});
    this.initialSelect.push({label: 'No', value: 0});
    this.adminSelect.push({label: 'No', value: 'N'});
    this.adminSelect.push({label: 'Yes', value: 'Y'});

    const subscription = this.us.getECMUsers().subscribe(data=>this.assignUsers(data));
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
            this.itemsPerPage = 15;
          }
        }
      });
    }
  }
  assignUsers(data){
    this.ecmUserList=data;
    this.colHeaders = [
      {field: 'id', header: 'Id'},
      {field: 'userName', header: 'User Name'},
      {field: 'fulName', header: 'Full Name'},
      {field: 'title', header: 'Title'},
      {field: 'KocId', header: 'Koc Id'},
      {field: 'orgCode', header: 'Org Code'},
      {field: 'mail', header: 'Mail'},
    ];
  }
  clearSubscriptions() {
    this.subscriptions.map(s => {
      s.unsubscribe();
    });
  }

  addToSubscriptions(subscription) {
    this.subscriptions.push(subscription);
  }
  editUser(dat,i){
    this.userModel=dat;

  }

  saveUser(){
    this.us.saveUser(this.userModel).subscribe(data=>this.successSave(),err=>this.failureSave());
  }
  successSave(){
    this.growlService.showGrowl({
          severity: 'info',
          summary: 'Success', detail: 'Saved Successfully'
        });
  }
  failureSave(){
      this.growlService.showGrowl({
          severity: 'error',
          summary: 'Failure', detail: 'Save Failed'
        });
  }
  exportToExcel(){
     let array=[];
    this.colHeaders.map(d=>{
      array.push(d.field);
    });
    this.coreService.exportToExcel( this.ecmUserList,'ECM_Users.xlsx',array)
  }
  viewUsers(data){
    this.allusers=data;
    this.viewuser=true;
  }

  ngOnDestroy() {
    this.clearSubscriptions();
    this.esignSelect = [];
    this.initialSelect = [];
    this.ecmUserList=[];
    this.viewuser=false;
  }

}
