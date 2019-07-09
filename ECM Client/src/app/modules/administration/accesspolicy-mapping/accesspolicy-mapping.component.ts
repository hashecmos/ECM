import {BreadcrumbService} from '../../../services/breadcrumb.service';
import {Component, OnDestroy, OnInit} from '@angular/core';
import {UserService} from '../../../services/user.service';
import {GrowlService} from '../../../services/growl.service';
import {ConfirmationService} from 'primeng/primeng';
import {ContentService} from '../../../services/content-service.service';
import {AccessPolicyService} from '../../../services/access-policy.service';
import {CoreService} from '../../../services/core.service';
import {AdminService} from "../../../services/admin.service";

@Component({
  selector: 'app-accesspolicy-mapping',
  templateUrl: './accesspolicy-mapping.component.html',
  styleUrls: ['./accesspolicy-mapping.component.css']
})
export class AccessPolicyMappingComponent implements OnInit, OnDestroy {
  private roleData: any = {roles: {model: {}}};
  roleTreeExpandedIcon = 'ui-icon-people-outline';
  roleTreeCollapsedIcon = 'ui-icon-people';
  private subscriptions: any[] = [];
  private currentUser: any;
  private tmpRoleTree: any[];
  private pageSize:any = 15;
  public suggestionsResults: any[] = [];
  public selectedOrgUnit: any;
  tempOrgId:any;
  constructor(private userService: UserService, private growlService: GrowlService,private coreService:CoreService,
              private breadcrumbService: BreadcrumbService, private accessPolicyService: AccessPolicyService,private as: AdminService,
              private confirmationService: ConfirmationService, private contentService: ContentService) {
    this.roleData.roles = {
      selectCriterions: [
        {label: 'Email', value: 'EMAIL'},
        {label: 'Name', value: 'NAME'},
        {label: 'Designation', value: 'TITLE'},
        {label: 'Phone', value: 'PHONE'},
        {label: 'Org Code', value: 'ORGCODE'},
        {label: 'Koc No', value: 'KOCNO'}],
      result: undefined, model: {selectedCriterion: 'NAME'}
    };


  }
  refresh(){
    this.getAccessPolicyMappings(this.tempOrgId);
  }

  ngOnInit() {
    this.userService.getUserSettings().subscribe(val => {
      const res:any = val;
      this.assignPagination(res);
    });
    this.currentUser = this.userService.getCurrentUser();
    this.breadcrumbService.setItems([
      {label: 'Admin'},
      {label: 'Access Policy Mapping'}
    ]);
    this.getOrgRole();
  }
  assignPagination(val) {
    if (val !== undefined) {
      val.map((d, i) => {
        if (d.key === 'Page Size') {
          if(d.val){
            this.pageSize = parseInt(d.val,10);
          }else{
            this.pageSize = 15;
          }
        }
      });
    }
  }

  getOrgRole() {
    const subscription = this.as.getTopLevelOrgUnit().subscribe(res => {
      const response = res;
      const resparray=[];
      resparray.push(res);
      this.tmpRoleTree = [];
      resparray.map((head)=>{
        this.tmpRoleTree.push({
          label: head.desc,
          data: head,
          expandedIcon: this.roleTreeExpandedIcon,
          collapsedIcon: this.roleTreeCollapsedIcon,
          leaf: false,
          expanded: false,
          selectable: head.orgCode ? true : false
        });
      });

      this.roleData.roles.roleTree = this.tmpRoleTree;
      //this.getSubOrgRoles(this.tmpRoleTree[0], true);
    }, err => {
    });
    this.coreService.progress = {busy: subscription, message: '', backdrop: true};
    this.addToSubscriptions(subscription);
  }

  getAccessPolicyMappings(orgId) {
    this.tempOrgId=orgId;
    const subscription=this.accessPolicyService.getAccessPolicyMappings(orgId).subscribe(res => {
      this.roleData.roles.accessPolicyMappings = res;
    }, err => {

    });
    this.coreService.progress = {busy: subscription, message: '', backdrop: true};
    this.addToSubscriptions(subscription);
  }

  getEntryTemplates(orgId) {
    const subscription=this.contentService.getEntryTemplateByOrgId(orgId)
      .subscribe(res => {
        this.roleData.roles.entryTemplates = [];
        const response: any = res;
        response.map(r => {
          this.roleData.roles.entryTemplates.push({label: r.symName, value: r});
        });
        /*if (this.roleData.roles.entryTemplates[0]) {
          this.roleData.roles.model.selectedEntryTemplate = this.roleData.roles.entryTemplates[0].value;
        }*/

      });
    this.coreService.progress = {busy: subscription, message: '', backdrop: true};
    this.addToSubscriptions(subscription);
  }

  getUserSupervisorTree() {
    const subscription=this.userService.getUserSupervisorTree(this.currentUser.EmpNo).subscribe((res:any) => {
      if (res.length > 1) {
        this.setChildren(this.tmpRoleTree[0], res, 1);
      }
      else {
        this.roleData.roles.roleTree = this.tmpRoleTree;
      }
    }, err => {

    });
    this.coreService.progress = {busy: subscription, message: '', backdrop: true};
    this.addToSubscriptions(subscription);
    }

  removeMappingConfirm(mapping) {
    this.confirmationService.confirm({
      message: 'Are you sure that you want to remove mapping ?',
      key: 'removeMappingConfirmation',
      accept: () => {
        this.removeMapping(mapping);
      }
    });
  }

  removeMapping(mapping) {
    const subscription=this.accessPolicyService.removeAccessPolicyMapping(mapping.id)
      .subscribe(res => {

        this.growlService.showGrowl({
          severity: 'info',
          summary: 'Info', detail: 'Mapping Removed Successfully'
        });
        this.getAccessPolicyMappings(this.roleData.roles.selectedRole.data.id);
      }, err => {
        this.growlService.showGrowl({
          severity: 'error',
          summary: 'Error', detail: 'Error In Removing Mapping'
        });
      });
    this.coreService.progress = {busy: subscription, message: '', backdrop: true};
    this.addToSubscriptions(subscription);
  }

  getAccessPolicies(orgId) {
    const subscription=this.accessPolicyService.getAccessPoliciesByOrgId(orgId)
      .subscribe(res => {
        this.roleData.roles.accessPolicies = [];
        const response: any = res;
        response.map(r => {
          this.roleData.roles.accessPolicies.push({label: r.name, value: r});
        });
       /* if (this.roleData.roles.accessPolicies[0]) {
          this.roleData.roles.model.selectedAccessPolicy = this.roleData.roles.accessPolicies[0].value;
        }*/

      }, err => {

      });
    this.coreService.progress = {busy: subscription, message: '', backdrop: true};
    this.addToSubscriptions(subscription);
  }

  expandNode(event) {
    this.getSubOrgRoles(event.node, false);
  }


  existsInList(user) {
    let exists = false;
    if (this.roleData.roles.selectedRole) {
      this.roleData.roles.selectedRole.children.map(c => {
        if (user.EmpNo === c.data.empNo) {
          user.disabled = true;
          exists = true;
        }
      });
    } else {
      exists = true;
    }

    return exists;


  }

  getRoleMembers(role) {
    if (!role.members) {
      let RoleNameString = '';
      const subscription = this.userService.getRoleMembers(role.id).subscribe(res => {
        for (const RName of res) {
          if (RName.name !== undefined) {
            RoleNameString = RoleNameString + '\n' + RName.name;
          }
        }
        role.members = RoleNameString.slice(1);

      }, err => {

      });
      this.coreService.progress = {busy: subscription, message: '', backdrop: true};
    this.addToSubscriptions(subscription);
    }

  }


  manageMapping(event) {
    this.roleData.roles.selectedRole = event.node;
    this.getAccessPolicyMappings(event.node.data.id);
    this.getEntryTemplates(event.node.data.id);
    this.getAccessPolicies(event.node.data.id);
  }

  addMapping() {
    console.log('se01 ' + this.roleData.roles.model.selectedEntryTemplate);
    console.log('se02 ' + this.roleData.roles.model.selectedAccessPolicy);
    const subscription=this.accessPolicyService.addAccessPolicyMapping(this.roleData.roles.model.selectedEntryTemplate.vsid,
      this.roleData.roles.model.selectedAccessPolicy.id).subscribe(res => {
       if(res==='OK'){
         this.growlService.showGrowl({
        severity: 'info',
        summary: 'Info', detail: 'Mapping Added Successfully'
      });
       }else{
         this.growlService.showGrowl({
        severity: 'info',
        summary: 'Info', detail: 'Mapping Already Exist.It Is Updated'
      });
       }

      this.getAccessPolicyMappings(this.roleData.roles.selectedRole.data.id);
    }, err => {

      this.growlService.showGrowl({
        severity: 'error',
        summary: 'Error', detail: 'Error In Mapping'
      });
    });
    this.coreService.progress = {busy: subscription, message: '', backdrop: true};
    this.addToSubscriptions(subscription);
  }


  getSubOrgRoles(parent, init) {
    const subscription = this.as.getSubLevelOrgUnits(parent.data.id).subscribe((res:any) => {
      parent.children = [];
      res.map(d => {
        parent.children.push({
          label: d.desc,
          data: d,
          expandedIcon: this.roleTreeExpandedIcon,
          collapsedIcon: this.roleTreeCollapsedIcon,
          leaf: false,
          selectable: d.orgCode? true : false
        });
      });
      if(init){
        this.getUserSupervisorTree();
      }


    }, err => {

    });
     this.coreService.progress = {busy: subscription, message: '', backdrop: true};
    this.addToSubscriptions(subscription);
  }

  setChildren(parent, response, index) {
    let newParent;
    if (!parent.children) {
      parent.children = [];
      parent.children.push({
        label: response[index].headRoleName, data: response[index], expandedIcon: this.roleTreeExpandedIcon,
        collapsedIcon: this.roleTreeCollapsedIcon, leaf: false, expanded: true
      });
      newParent = parent.children[0];
    } else {
      parent.children.map(c => {
        if (c.data.id === response[index].id) {
          c.expanded = true;
          newParent = c;
        }
      });
    }

    if (index < response.length - 1) {
      this.setChildren(newParent, response, index + 1);
    } else {
      this.roleData.roles.roleTree = this.tmpRoleTree;
    }

  }

  search(event) {
    const subscription =  this.as.searchOrgUnits(event.query).subscribe(data => {
        this.suggestionsResults = data;
      });
    this.coreService.progress = {busy: subscription, message: '', backdrop: true};
    this.addToSubscriptions(subscription);
  }

  orgUnitSelected(selected) {
    this.selectedOrgUnit = selected;

    this.roleData.roles.selectedRole = {data:selected};
    this.getAccessPolicyMappings(selected.id);
    this.getEntryTemplates(selected.id);
    this.getAccessPolicies(selected.id);
  }

  clearSubscriptions() {
    this.subscriptions.map(s => {
      s.unsubscribe();
    });
  }

  addToSubscriptions(subscription) {
    this.subscriptions.push(subscription);
  }

  ngOnDestroy() {
    this.clearSubscriptions();
  }
}
