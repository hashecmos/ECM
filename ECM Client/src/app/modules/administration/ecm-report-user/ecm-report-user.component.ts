import { Component, OnInit } from '@angular/core';
import {BreadcrumbService} from '../../../services/breadcrumb.service';
import {UserService} from '../../../services/user.service';
import {GrowlService} from '../../../services/growl.service';
import {CoreService} from '../../../services/core.service';
import {User} from '../../../models/user/user.model';
import {ConfirmationService, SelectItem} from 'primeng/primeng';

@Component({
  selector: 'app-ecm-report-user',
  templateUrl: './ecm-report-user.component.html',
  styleUrls: ['./ecm-report-user.component.css']
})
export class EcmReportUserComponent implements OnInit {
  ecmUserList:User[];
  colHeaders: any[];
  itemsPerPage: any = 15;
  esignSelect: SelectItem[];
  initialSelect: SelectItem[];
  private subscriptions: any[] = [];
  userModel= new User();
  criteria: SelectItem[];
  selectedcriteria: string;
  searchText: any;
  showEditUser=false;
  searchStarted: boolean;
  public SelectedUserList = [];
  public isReportAdmin;
  public isExcludeOperators;
  public searchQueary = { userName:undefined,mail:undefined,title:undefined,phone:undefined,orgCode:undefined,
                            empNo:undefined,userType:undefined,filter: ''};
  constructor(private us: UserService,private confirmationService: ConfirmationService,private coreService:CoreService,private breadcrumbService: BreadcrumbService ,
              private growlService: GrowlService) { }
  refresh(){
   const subscription = this.us.getReportUsers().subscribe(data=>this.assignUsers(data));
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
    this.criteria = [];
    this.selectedcriteria = 'NAME';
    this.criteria.push({label: 'Email', value: 'EMAIL'});
    this.criteria.push({label: 'Name', value: 'NAME'});
    this.criteria.push({label: 'Designation', value: 'TITLE'});
    this.criteria.push({label: 'Phone', value: 'PHONE'});
    this.criteria.push({label: 'Org Code', value: 'ORGCODE'});
    this.criteria.push({label: 'Koc No', value: 'KOCNO'});
     this.breadcrumbService.setItems([
      {label: 'Admin'},
      {label: 'ECM Report Users'}
    ]);
    this.esignSelect.push({label: 'ACTIVE', value: 1});
    this.esignSelect.push({label: 'INACTIVE', value: 0});
    const subscription = this.us.getReportUsers().subscribe(data=>this.assignUsers(data));
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
      {field: 'KocId', header: 'KOC Id'},
    ];
  }
  clearResult() {
    this.searchStarted = false;
    //this.searchText = '';
    this.searchQueary = { userName:undefined,mail:undefined,title:undefined,phone:undefined,orgCode:undefined,
                            empNo:undefined,userType:undefined,filter: ''};
  }
  searchUsers() {
    this.searchStarted = true;
/*    const subscription = this.us.searchUsersList('USER', this.searchText, this.selectedcriteria,'').subscribe(data => {
      this.SelectedUserList = data;
    });*/
    let formValid = true;
    this.searchQueary.userType = 'USER';
    if((this.searchQueary.userName !== undefined && this.searchQueary.userName !== '' && this.searchQueary.userName !== null) ||
        (this.searchQueary.title !== undefined && this.searchQueary.title !== '' && this.searchQueary.title !== null) ||
        (this.searchQueary.mail !== undefined && this.searchQueary.mail !== '' && this.searchQueary.mail !== null) ||
        (this.searchQueary.empNo !== undefined && this.searchQueary.empNo !== '' && this.searchQueary.empNo !== null) ||
        (this.searchQueary.orgCode !== undefined && this.searchQueary.orgCode !== '' && this.searchQueary.orgCode !== null) ||
        (this.searchQueary.phone !== undefined && this.searchQueary.phone !== '' && this.searchQueary.phone !== null)){
    } else {
      formValid = false;
      this.growlService.showGrowl({
            severity: 'error',
            summary: 'Warning', detail: 'Fill Any One Field To Search'
      });
    }
    if(formValid){
      const subscription = this.us.searchEcmUsers(this.searchQueary).subscribe(data => {
        if(data.length === 0){
            this.growlService.showGrowl({
              severity: 'error',
              summary: 'Failure', detail: 'No Results Found'
            });
        }
        this.SelectedUserList = data;
      });
      this.coreService.progress = {busy: subscription, message: '', backdrop: true};
      this.addToSubscriptions(subscription)
    }

  }
  addUser(e){
  this.us.saveReportUser(e.EmpNo,0,this.isReportAdmin?'Y':'N').subscribe(data=>this.addUserSuccess(data),err=>this.addUserFailed());
  this.showEditUser=false;
  this.SelectedUserList=[];
  //this.searchText='';
  this.searchQueary = { userName:undefined,mail:undefined,title:undefined,phone:undefined,orgCode:undefined,
                            empNo:undefined,userType:undefined,filter: ''};
  }
  addUserSuccess(val){
     if(val==='User Exists'){
        this.growlService.showGrowl({
          severity: 'error',
          summary: 'Already Exist', detail: 'User Already Exist'
        });
     }
     else{
       this.growlService.showGrowl({
          severity: 'info',
          summary: 'Success', detail: 'Add User Successful'
        });
     }

     const subscription = this.us.getReportUsers().subscribe(data=>this.assignUsers(data));
     this.addToSubscriptions(subscription);


  }
  addUserFailed(){
    this.growlService.showGrowl({
          severity: 'error',
          summary: 'Failure', detail: 'Add User Failed'
        });
  }
  clearSubscriptions() {
    this.subscriptions.map(s => {
      s.unsubscribe();
    });
  }

  addToSubscriptions(subscription) {
    this.subscriptions.push(subscription);
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
   confirm(event) {
    this.confirmationService.confirm({
      message: 'Are you sure that you want to remove?',
      accept: () => {
        //Actual logic to perform a confirmation
        this.removeUser(event);
      }
    });
  }
  removeUser(dat){
    this.us.saveReportUser(dat.EmpNo,dat.id,'N').subscribe(data=>this.removeSuccess(),err=>this.removeFailed())
  }
  removeSuccess(){
    this.us.getReportUsers().subscribe(data=>this.assignUsers(data));
    this.growlService.showGrowl({
          severity: 'info',
          summary: 'Success', detail: 'Removed Successfully'
        });
  }
  removeFailed(){
    this.growlService.showGrowl({
          severity: 'error',
          summary: 'Failure', detail: 'Remove Failed'
        });
  }
  closeModel(){
     this.SelectedUserList=[];
     //this.searchText='';
     this.searchQueary = { userName:undefined,mail:undefined,title:undefined,phone:undefined,orgCode:undefined,
                            empNo:undefined,userType:undefined,filter: ''};
     this.isReportAdmin = false;
  }

  exportToExcel(){
    let array=[];
    this.colHeaders.map(d=>{
      array.push(d.field);
    });
    this.coreService.exportToExcel( this.ecmUserList,'ECM_Report_Users.xlsx',array)
  }
  ngOnDestroy() {
    this.clearSubscriptions();
    this.ecmUserList=[];
    this.SelectedUserList=[];
  }
}
