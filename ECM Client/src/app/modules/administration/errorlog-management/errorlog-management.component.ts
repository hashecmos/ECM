import {Component, OnInit} from '@angular/core';
import {BreadcrumbService} from '../../../services/breadcrumb.service';
import {AdminService} from '../../../services/admin.service';
import {CoreService} from '../../../services/core.service';
import {UserService} from "../../../services/user.service";

@Component({
  selector: 'app-errorlog-management',
  templateUrl: './errorlog-management.component.html',
  styleUrls: ['./errorlog-management.component.css']
})
export class ErrorlogManagementComponent implements OnInit {
  public errorLogs: any = [];
  public itemsPerPage: any = 15;
  public colHeaders: any[] =[];
  details:any;
  openDetails=false;
  viewerror = false;
  private allerrors:any[];
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
      {label: 'ECM Error Logs'}
    ]);
     const subscription = this.adminService.getLogs().subscribe(data=>this.assignLogs(data));
     this.coreService.progress = {busy: subscription, message: '', backdrop: true};
      this.addToSubscriptions(subscription);
  }
  viewErrors(data){
    this.allerrors=data;
    this.viewerror=true;
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
    this.errorLogs=data;
    this.colHeaders = [
      {field: 'id', header: 'Id', hidden: false}, {field: 'type', header: 'Error Type', hidden: false},
      {field: 'summary', header: 'Summary', hidden: false}, {field: 'timeStamp', header: 'Log Date', hidden: false},
      {field: 'context', header: 'Context', hidden: false}, {field: 'servername', header: 'Server Name', hidden: false},
      {field: 'appname', header: 'App Name', hidden: false},
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
    this.adminService.getLogs().subscribe(data=>this.assignLogs(data));
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
    this.coreService.exportToExcel( this.errorLogs,'ErrorLogs.xlsx',array)
  }

  ngOnDestroy() {
    this.clearSubscriptions();
    this.openDetails=false;
    this.viewerror = false;
  }

}
