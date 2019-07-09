import {Component, OnInit} from '@angular/core';
import {BreadcrumbService} from '../../../services/breadcrumb.service';
import {AdminService} from '../../../services/admin.service';
import {CoreService} from '../../../services/core.service';
import {UserService} from "../../../services/user.service";

@Component({
  selector: 'app-ecm-admin-logs',
  templateUrl: './ecm-admin-logs.component.html',
  styleUrls: ['./ecm-admin-logs.component.css']
})
export class EcmAdminLogsComponent implements OnInit {
  public adminLogs: any = [];
  public itemsPerPage: any = 15;
  public colHeaders: any[] =[];
  details:any;
  openDetails=false;
  viewLogs = false;
  private allLogs:any[];
   private subscriptions: any[] = [];
  constructor(private breadcrumbService: BreadcrumbService,private coreService:CoreService, private adminService: AdminService, private us: UserService) {
  }

  ngOnInit() {
    this.us.getUserSettings().subscribe(val => {
      const res:any = val;
      this.assignPagination(res);
    });
    this.breadcrumbService.setItems([
      {label: 'Admin'},
      {label: 'ECM Admin Logs'}
    ]);
     const subscription = this.adminService.getAdminLogs().subscribe(data=>this.assignLogs(data));
     this.coreService.progress = {busy: subscription, message: '', backdrop: true};
      this.addToSubscriptions(subscription);
  }
  viewErrors(data){
    this.allLogs=data;
    this.viewLogs=true;
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
  assignLogs(data){
    this.adminLogs=data;
    this.colHeaders = [
      {field: 'id', header: 'Id', hidden: false}, {field: 'type', header: 'Type', hidden: false},
      {field: 'username', header: 'User Name', hidden: false}, {field: 'timeStamp', header: 'Log Date', hidden: false},
      {field: 'details', header: 'Details', hidden: false}

    ];
  }
  openInfoError(data){
    this.adminService.getLogDetails(data.id).subscribe(data=>this.assignDetails(data));

  }
  assignDetails(data){
    this.details=data;
    this.openDetails=true;
  }
  refresh(){
    this.adminService.getAdminLogs().subscribe(data=>this.assignLogs(data));
  }
    clearSubscriptions() {
    this.subscriptions.map(s => {
      s.unsubscribe();
    });
  }

  addToSubscriptions(subscription) {
    this.subscriptions.push(subscription);
  }
  exportToExcel(){
    let array=[];
    this.colHeaders.map(d=>{
      array.push(d.field);
    });
    this.coreService.exportToExcel( this.adminLogs,'ErrorLogs.xlsx',array)
  }

  ngOnDestroy() {
    this.clearSubscriptions();
    this.openDetails=false;
    this.viewLogs = false;
  }

}
