import {Component, OnDestroy, OnInit} from '@angular/core';
import {BreadcrumbService} from "../../services/breadcrumb.service";
import {UserService} from "../../services/user.service";
import {User} from "../../models/user/user.model";
import {CoreService} from "../../services/core.service";
import {Subscription} from 'rxjs/Subscription';
import {ReportService} from "../../services/report.service";
import {MenuItem} from "primeng/primeng";
import {saveAs} from 'file-saver';
import {AdminService} from "../../services/admin.service";

@Component({
  selector: 'app-report-component',
  templateUrl: './report.component.html'
})

export class ReportComponent implements OnInit, OnDestroy {
  public activeIndex: any[] = [0];
  public report: any = {roles: {}, search: {}, options: {reportType: [], category: {}, dailyChecked: false, searchType:[], roleList:[], allTotal:0, chartTotal:0}};
  public user = new User();
  subscriptions: Subscription[] = [];
  roleTreeExpandedIcon = 'ui-icon-people-outline';
  roleTreeCollapsedIcon = 'ui-icon-people';
  private tmpRoleTree = [];
  private reportCount: any[] = [];
  public chartOptions: any = {
    scaleShowVerticalLines: false,
    responsive: true,
    maintainAspectRatio: false,
    plugins: {
      datalabels: {
        align: 'center',
        anchor: 'center',
        backgroundColor: null,
        borderColor: null,
        borderRadius: 4,
        borderWidth: 1,
        color: '#000000',
        font: {
          size: 11,
          weight: 600
        },
        offset: 4,
        padding: 0,
        formatter: function (value) {
          return value
        }
      }
    },
    scales: {
       xAxes: [
        {
          ticks: {
            min: 0, // it is for ignoring negative step.
            beginAtZero: true,
            callback: function(value, index, values) {
                if (Math.floor(value) === value) {
                    return value;
                }
            }
          }
        }
      ]
    }
  };
  public chartLabels: string[] = [];
  public chartType = 'horizontalBar';
  public chartLegend = false;
  public reportChartData = false;
  public chartData: any[] = [{data: []}];
  public chartColors = [{backgroundColor: []}];
  public exportBtnItems: MenuItem[] = [];
  public searchQuery: any = {};
  public today = new Date();
  public eSignDocData: any[] = [];
  public colHeaders: any[] = [];
  public pageSize: any = 10;
  constructor(private breadcrumbService: BreadcrumbService, private us: UserService, private coreService: CoreService, private rs: ReportService,
              private as: AdminService) {
  }
  ngOnInit() {
    this.us.getUserSettings().subscribe(val => {
      const res:any = val;
      this.assignPagination(res);
    });
    this.report.options.searchType = [{label: 'User Name', value: 'userName'}, {label: 'KOC ID', value: 'empNo'}];
    this.report.options.reportType = [{label: 'Workflow', value: 'workflow'}, {label: 'Documents', value: 'doc'},
                                      {label: 'eSign Documents', value: 'eSignDoc'}, {label: 'All', value: 'all'}];
    this.report.options.category = {workflow: [{label: 'Received', value: 'received'}, {label: 'Sent', value: 'sent'}],
                                    doc: [{label: 'Created', value: 'created'}], eSignDoc: [], all: []};

    this.colHeaders = [{field: 'docTitle', header: 'Document Title'},{field: 'reqDate', header: 'Request Date', sortField: 'reqDate2'},
      {field: 'signDate', header: 'Signed Date', sortField: 'signDate2'}, {field: 'empName', header: 'User Name'},
      {field: 'empNo', header: 'KOC ID'}, {field: 'status', header: 'Status'}, {field: 'orgCode', header: 'Organization Code'},
      {field: 'orgName', header: 'Organization Name'}];

    this.breadcrumbService.setItems([
      {label: 'Reports'}
    ]);
    this.user = this.us.getCurrentUser();
/*    if(this.user.isReportAdmin === 'Y'){
      this.gerTopOrgTree();
    } else {*/
      this.gerSupervisorTree();
    //}
    this.exportBtnItems.push({
      'label': 'PDF', command: event => {
        this.exportToPdf();
      }
    }, {
      'label': 'Excel', command: event => {
        this.exportToExcel();
      }
    });
    this.getRoleList();
  }

  getRoleList(){
    let service = 'getRoleByOrgCode';
    if(this.user.isReportAdmin  === 'Y'){
      service = 'getRoles';
    }
    const subscription = this.us[service](service==='getRoleByOrgCode'?this.user.EmpNo:this.user.userName).subscribe(res => {
      res.map((role)=>{
        this.report.options.roleList.push({label:role.name, value:{id:role.id, orgCode:role.orgCode}})
      });
    });
  }

  assignPagination(val) {
    if (val !== undefined) {
      val.map((d, i) => {
        if (d.key === 'Page Size') {
          if(d.val){
            this.pageSize = parseInt(d.val,10);
          }else{
            this.pageSize = 10;
          }
        }
      });
    }
  }
/*  gerTopOrgTree() {
    const subscription = this.us.getTopRolesList().subscribe(res => {
      const tmpRoles = [];
      res.map((head)=>{
        tmpRoles.push({
          label: head.headRoleName,
          data: head,
          expandedIcon: this.roleTreeExpandedIcon,
          collapsedIcon: this.roleTreeCollapsedIcon,
          leaf: false,
          expanded: false,
          selectable:true
        });
      });

      this.report.roles.roleTree = tmpRoles;
    }, err => {
    });
    this.coreService.progress = {busy: subscription, message: '', backdrop: true};
    this.addToSubscriptions(subscription);
  }*/

  gerSupervisorTree() {
    this.tmpRoleTree = [];
    this.report.roles.roleTree = [];
    const subscription = this.us.getUserSupervisorTree(this.user.EmpNo).subscribe(res => {
      res.map((head) => {
        if (head.parent === 0) {
          this.tmpRoleTree.push({
            label: head.headRoleName,
            data: head,
            expandedIcon: this.roleTreeExpandedIcon,
            collapsedIcon: this.roleTreeCollapsedIcon,
            leaf: false,
            expanded: !(this.user.orgCode === head.orgCode),
            selectable: this.user.orgCode === head.orgCode
          });
        }
      });
      if (res.length > 1) {
        this.setChildren(this.tmpRoleTree[0], res, 1);
      } else {
        this.report.roles.roleTree = this.tmpRoleTree;
      }
    }, err => {

    });
    this.coreService.progress = {busy: subscription, message: '', backdrop: true};
    this.addToSubscriptions(subscription);
  }

  setChildren(parent, children, index) {
    let newParent;
    if (!parent.children) {
      parent.children = [];
      parent.children.push({
        label: children[index].headRoleName,
        data: children[index],
        expandedIcon: this.roleTreeExpandedIcon,
        collapsedIcon: this.roleTreeCollapsedIcon,
        leaf: false,
        expanded: true,
        selectable: this.user.orgCode === children[index].orgCode || parent.selectable
      });
      newParent = parent.children[0];
    } else {
      parent.children.map(c => {
        if (c.data.id === children[index].id) {
          c.expanded = true;
          newParent = c;
        }
      });
    }
    if (index < children.length - 1) {
      this.setChildren(newParent, children, index + 1);
    } else {
      if (index === children.length - 1) {
        newParent.expanded = false;
      }
      this.report.roles.roleTree = this.tmpRoleTree;
    }
  }

  onNodeExpanded(event) {
    if(event.node.selectable){
      this.getSubOrgUnits(event.node)
    }
  }

  getSubOrgUnits(parent) {
    const subscription = this.rs.getSubOrgUnits(parent.data.orgCode).subscribe((res: any) => {
      parent.children = [];
      res.map((d) => {
        parent.children.push({
          label: d.desc,
          data: d,
          expandedIcon: this.roleTreeExpandedIcon,
          collapsedIcon: this.roleTreeCollapsedIcon,
          leaf: false,
          selectable: true
        });
      });
    });
    this.coreService.progress = {busy: subscription, message: '', backdrop: true};
    this.addToSubscriptions(subscription);
  }

  roleSelected(node) {
    this.searchQuery.userType = ' ';
    this.report.search.orgCode = node.node.data.orgCode;
    this.report.search.orgUnitSelected = undefined;
    this.report.search.orgUnitSearchText = undefined;
    this.report.search.userSearchText = undefined;
    this.report.search.searchType = undefined;
    this.clearRoleSelection();
  }

  getRoleMembers(role) {
    if (!role.members) {
      let RoleNameString = '';
      let roleId;
      if (role.headRoleId) {
        roleId = role.headRoleId
      } else if (role.id) {
        roleId = role.id
      }
      const subscription = this.us.getRoleMembers(roleId).subscribe((res: any) => {
        for (const RName of res) {
          if (RName.name !== undefined) {
            RoleNameString = RoleNameString + '\n' + '<i class=material-icons style=font-size:.95em;>person</i>' + ' ' + RName.name;
          }
        }
        role.members = RoleNameString.slice(1);
      }, err => {

      });
      this.addToSubscriptions(subscription);
    }
  }

  resetFromDatePicker(event) {
    this.report.search.fromDate = undefined;
  }

  resetToDatePicker(event) {
    this.report.search.toDate = undefined;
  }

  fromDateSelected(event){
     this.report.search.minDate = new Date(event);
  }

  clearOrgSelection(){
    this.report.search.orgCode = undefined;
    this.refreshTree();
  }

  clearRoleSelection(){
    this.report.search.roleSearchText=undefined;
    this.searchQuery.userType = undefined;
    this.searchQuery.EmpNo = undefined;
    this.searchQuery.orgCode = undefined;
  }

  getReport() {
    if(this.report.search.orgCode!==null && this.report.search.orgCode!==undefined || this.report.search.orgUnitSelected!==null && this.report.search.orgUnitSelected!==undefined){
      this.searchQuery.orgCode = this.report.search.orgCode;
      if(this.report.search.orgUnitSelected){
        this.searchQuery.orgCode = this.report.search.orgUnitSelected;
      }
      delete this.searchQuery.EmpNo;
      delete this.searchQuery.id;
    }
    if(this.report.search.excludeOperators){
      this.searchQuery.exOperator = 'Y';
    }else{
      delete this.searchQuery.exOperator;
    }
    this.searchQuery.fromDate = this.coreService.formatDateForFinishBefore(this.report.search.fromDate);
    if (this.report.search.toDate) {
      this.searchQuery.toDate = this.coreService.formatDateForFinishBefore(this.report.search.toDate);
    } else {
      const today = new Date();
      this.searchQuery.toDate = this.coreService.formatDateForFinishBefore(today);
    }
    let subscription;
    if(this.report.search.reportType==='workflow' && this.report.search.category === 'received'){
          subscription = this.rs.getOrgWorkitemCount(this.searchQuery).subscribe(res => {
          this.prepareChart(res);
          this.activeIndex = [1];
          this.reportChartData = true;
        });
    } else if(this.report.search.reportType==='workflow' && this.report.search.category === 'sent'){
          subscription = this.rs.getOrgSentitemCount(this.searchQuery).subscribe(res => {
          this.prepareChart(res);
          this.activeIndex = [1];
          this.reportChartData = true;
        });
    } else if(this.report.search.reportType === 'doc'){
          this.searchQuery.userName = this.user.fulName;
          subscription = this.rs.getOrgDocumentCount(this.searchQuery).subscribe(res => {
          this.prepareChart(res);
          this.activeIndex = [1];
          this.reportChartData = true;
        });
    } else if(this.report.search.reportType === 'eSignDoc'){
          subscription = this.rs.getOrgESignItems(this.searchQuery).subscribe(res => {
          this.assignDate(res);
          this.reportCount = [];
          this.activeIndex = [1];
          this.reportChartData = false;
        });
    } else if(this.report.search.reportType === 'all'){
          subscription = this.rs.getOrgAllReportCount(this.searchQuery).subscribe(res => {
          this.assignAllData(res);
          this.eSignDocData = [];
          this.activeIndex = [1];
          this.reportChartData = false;
        });
    }
    this.coreService.progress = {busy: subscription, message: '', backdrop: true};
    this.addToSubscriptions(subscription);
  }

  assignAllData(res){
    res.map((item)=>{
      item.details.map((detail)=>{
        this.report.options.allTotal+=detail.count;
      });
    });
    this.reportCount = res;
  }

  assignDate(data) {
    data.map((d) => {
      d.signDate2 = this.coreService.convertToTimeInbox(d.signDate);
      d.reqDate2 = this.coreService.convertToTimeInbox(d.reqDate);
    });
    this.eSignDocData = data;
  }

  onTabOpen(event) {
    if(event.index === 0){
      this.activeIndex = [0,1];
    }
  }

  prepareChart(data) {
    this.chartLabels = [];
    this.chartData[0].data = [];
    this.chartColors[0].backgroundColor = [];
    this.reportCount = data;
    this.report.options.chartTotal = 0;
    data.map((unit)=>{
      this.report.options.chartTotal+=unit.count;
    });
    data.map((d) => {
      if(d.count>0){
        this.chartLabels.push(d.desc);
        this.chartData[0].data.push(d.count);
        this.chartColors[0].backgroundColor.push(this.rs.getRandomMaterialColor());
      }
    });
  }

  exportToPdf() {
    this.searchQuery.exportType = 'pdf';
    this.searchQuery.userName = this.user.fulName;
    const mimeType = 'application/pdf';
    let fileName = 'Inbox_Workflow_Report' + '.pdf';
    let serviceURL = 'exportOrgWorkitemCount';
    if(this.report.search.reportType==='workflow' && this.report.search.category === 'received'){
        fileName = 'Inbox_Workflow_Report' + '.pdf';
        serviceURL = 'exportOrgWorkitemCount';
    } else if(this.report.search.reportType==='workflow' && this.report.search.category === 'sent'){
        fileName = 'Sent_Workflow_Report' + '.pdf';
        serviceURL = 'exportOrgSentitemCount';
    } else if(this.report.search.reportType === 'doc'){
        fileName = 'Documents_Report' + '.pdf';
        serviceURL = 'exportOrgDocumentCount';
    } else if(this.report.search.reportType === 'eSignDoc'){
        fileName = 'eSign_Documents_Report' + '.pdf';
        serviceURL = 'exportOrgESignItems';
    } else if(this.report.search.reportType === 'all'){
        fileName = 'All_Report' + '.pdf';
        serviceURL = 'exportOrgAllReportCount';
    }
    this.rs[serviceURL](this.searchQuery).subscribe(res => {
      const file = new Blob([res], {type: mimeType});
      saveAs(file, fileName);
    });
  }

  exportToExcel() {
    this.searchQuery.exportType = 'excel';
    this.searchQuery.userName = this.user.fulName;
    const mimeType = 'application/vnd.ms-excel';
    let fileName = 'Inbox_Workflow_Report' + '.xlsx';
    let serviceURL = 'exportOrgWorkitemCount';
    if(this.report.search.reportType==='workflow' && this.report.search.category === 'received'){
        fileName = 'Inbox_Workflow_Report' + '.xlsx';
        serviceURL = 'exportOrgWorkitemCount';
    } else if(this.report.search.reportType==='workflow' && this.report.search.category === 'sent'){
        fileName = 'Sent_Workflow_Report' + '.xlsx';
        serviceURL = 'exportOrgSentitemCount';
    } else if(this.report.search.reportType === 'doc'){
        fileName = 'Documents_Report' + '.xlsx';
        serviceURL = 'exportOrgDocumentCount';
    } else if(this.report.search.reportType === 'eSignDoc'){
        fileName = 'eSign_Documents_Report' + '.xlsx';
        serviceURL = 'exportOrgESignItems';
    } else if(this.report.search.reportType === 'all'){
        fileName = 'All_Report' + '.xlsx';
        serviceURL = 'exportOrgAllReportCount';
    }
    this.rs[serviceURL](this.searchQuery).subscribe(res => {
      const file = new Blob([res], {type: mimeType});
      saveAs(file, fileName);
    });
  }

  checkedChanged(event){
    this.report.search.fromDate = undefined;
    this.report.search.toDate = undefined;
    if(event){
      this.report.search.fromDate = new Date();
    }
  }

  searchUsers(event) {
    const searchQuery: any = {
      userType : 'USER',
      filter : '',
      orgCode : this.user.orgCode
    };
    if(this.user.isReportAdmin  === 'Y'){
      delete searchQuery.orgCode
    }
    searchQuery[this.report.search.searchType] = event.query;
    const subscription = this.us.searchOrgECMUsers(searchQuery).subscribe(data => {
        this.report.search.searchSuggestions = data;
      });
    this.coreService.progress = {busy: subscription, message: '', backdrop: true};
    this.addToSubscriptions(subscription)
  }
  usersSelected(event) {
      this.searchQuery.userType = 'USER';
      this.searchQuery.EmpNo = event.EmpNo;
      this.searchQuery.orgCode = event.orgCode;
      this.report.search.orgUnitSelected = undefined;
  }
  onSearchTypeChanged(event){
      this.report.search.userSearchText = undefined;
  }
  searchRole(event){
    const searchQuery: any = {
      userType : 'ROLE',
      filter : '',
      orgCode : this.user.orgCode
    };
    searchQuery.userName = event.query;
    const subscription = this.us.searchEcmUsers(searchQuery).subscribe(data => {
        this.report.search.roleSearchSuggestions = data;
      });
    this.coreService.progress = {busy: subscription, message: '', backdrop: true};
    this.addToSubscriptions(subscription)
  }
  searchRoleSelected(event){
      console.log(event);
      this.searchQuery.userType = 'ROLE';
      this.searchQuery.EmpNo = event.id;
      this.searchQuery.orgCode = event.orgCode;
      this.report.search.orgUnitSelected = undefined;
  }
  searchOrgUnit(event) {
    const subscription =  this.as.searchOrgUnits(event.query).subscribe(data => {
        this.report.search.orgUnitSearchSuggestions = data;
      });
    this.coreService.progress = {busy: subscription, message: '', backdrop: true};
    this.addToSubscriptions(subscription);
  }
  searchOrgUnitSelected(selected) {
    this.searchQuery.userType = ' ';
    this.report.search.orgUnitSelected = selected.orgCode;
  }
  clearSearchobject(){
    this.report.search = {};
    this.searchQuery = {};
    this.refreshTree();
  }

  refreshTree(){
    /*if(this.user.isReportAdmin  === 'Y'){
      this.gerTopOrgTree();
    } else{*/
      this.gerSupervisorTree();
    //}
  }

  addToSubscriptions(subscription) {
    this.subscriptions.push(subscription);
  }
  clearSubscriptions() {
    this.subscriptions.map(s => {
      s.unsubscribe();
    });
  }

  ngOnDestroy() {
    this.clearSubscriptions();
    this.subscriptions = [];
    this.report.roles.roleTree = [];
    this.tmpRoleTree = [];
    this.report.search = {};
    this.searchQuery = {};
  }
}
