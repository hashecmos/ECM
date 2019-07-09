import {BreadcrumbService} from '../../../services/breadcrumb.service';
import {Component, OnDestroy, OnInit} from '@angular/core';
import {UserService} from '../../../services/user.service';
import {GrowlService} from '../../../services/growl.service';
import {ConfirmationService} from 'primeng/primeng';
import {CoreService} from '../../../services/core.service';
import {Subscription} from "rxjs/Rx";
import {AdminService} from "../../../services/admin.service";
import {Role} from "../../../models/user/role.model";
import {consoleTestResultHandler} from "tslint/lib/test";

@Component({
  selector: 'app-rolemanagement',
  templateUrl: './rolemanagement.component.html',
  styleUrls: ['./rolemanagement.component.css']
})
export class RolemanagementComponent implements OnInit, OnDestroy {
  private roleData: any = {roles: {model: {}}};
  private roleTreeData: any = {roles: {model: {}}};
  roleTreeExpandedIcon = 'ui-icon-people-outline';
  roleTreeCollapsedIcon = 'ui-icon-people';
  userIcon = 'ui-icon-person';
  private subscriptions: any[] = [];
  public showEditRole = false;
  public editMode = false;
  public editRole = new Role();
  public options: any[] = [];
  suggestionsResults: any[];
  granteesSuggestion: any[];
 // public busyModal: Subscription;
  public activeTab = 0;
  public tmpRoleTree: any[] = [];
  public searchQueary = { userName:undefined,mail:undefined,title:undefined,phone:undefined,orgCode:undefined,
                            empNo:undefined,userType:undefined,filter: ''};
  public editRoleWithOrg = false;
  constructor(private userService: UserService, private coreService: CoreService, private growlService: GrowlService, private as: AdminService,
              private breadcrumbService: BreadcrumbService, private confirmationService: ConfirmationService) {
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
    this.options = [{label: 'No', value: 0}, {label: 'Yes', value: 1}];
    this.roleData.roles.type = [{label: 'Directorate', value: 3}, {label: 'Group', value: 2},{label: 'Role', value: 1}];
    this.roleData.roles.selectedOrgCodeType = true;
  }

  ngOnInit() {
    this.breadcrumbService.setItems([
      {label: 'Admin'},
      {label: 'Role Management'}
    ]);
    this.getOrgRole();
    this.getRoles();
  }

  getRoles() {
    const subscription = this.userService.getRoles().subscribe(res => {
      const response = res;
      this.roleData.roles.roles = res;
      const tmpRoles = [];
      response.map(r => {
        tmpRoles.push({
          label: r.name,
          data: r,
          expandedIcon: this.roleTreeExpandedIcon,
          collapsedIcon: this.roleTreeCollapsedIcon,
          leaf: false,
          expanded: false
        });
      });
      this.roleData.roles.roleTree = tmpRoles;
      this.roleData.roles.oRoleTree = Object.assign([], tmpRoles);

/*      this.roleData.roles.parentRoleList = [];
      this.roleData.roles.roles.map((R,I)=>{
        if(R.type != 1){
          this.roleData.roles.parentRoleList.push({label:R.name,value:R.id});
        }
      });*/

    }, err => {
    });
    this.coreService.progress = {busy: subscription, message: '', backdrop: true};
    this.addToSubscriptions(subscription);
  }

  expandNode(event) {
    this.getRoleMembers(event.node);
  }

  getRoleMembers(node) {
    const subscription = this.userService.getRoleMembers(node.data.id).subscribe(res => {
      node.children = [];
      res.map(r => {
        node.children.push({
          label: r.name,
          data: r,
          expandedIcon: this.userIcon,
          collapsedIcon: this.userIcon,
          leaf: true,
          expanded: false,
          selectable: false
        });
      });
      node.expanded = true;

    }, err => {
    });
    this.addToSubscriptions(subscription);
  }

  getRoleMembersForTooltip(role) {
    if (!role.members) {
      let RoleNameString = '';
      let roleId;
      if(role.headRoleId){
        roleId=role.headRoleId
      } else if(role.id){
        roleId=role.id
      } else if(role.EmpNo){
        roleId=role.EmpNo
      }
      const subscription = this.userService.getRoleMembers(roleId).subscribe((res:any) => {
        for (const RName of res) {
          if (RName.name !== undefined) {
            RoleNameString = RoleNameString + '\n' +'<i class=material-icons style=font-size:.95em;>person</i>'  + ' '+ RName.name;
          }
        }
        role.members = RoleNameString.slice(1);

      }, err => {

      });
    this.addToSubscriptions(subscription);
    }

  }

  existsInList(user) {
    let exists = false;
    if(this.activeTab===0){
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
    } else {
       if (this.roleTreeData.roles.selectedRole) {
        this.roleTreeData.roles.selectedRole.children.map(c => {
          if (user.EmpNo === c.data.empNo) {
            user.disabled = true;
            exists = true;
          }
        });
      } else {
        exists = true;
      }
    }
    return exists;
  }

  getRoleMembersStr(role) {
    if (!role.members) {
      let RoleNameString = '';
      const subscription = this.userService.getRoleMembers(role.id).subscribe(res => {
        for (const RName of res) {
          if (RName.name !== undefined) {
            RoleNameString = RoleNameString + ',' + '<i class=material-icons style=font-size:.95em;>person</i>'  + ' '+RName.name;
          }
        }
        role.members = RoleNameString.slice(1);

      }, err => {

      });
      this.addToSubscriptions(subscription);
    }

  }

  addToList(user) {
    this.userService.getUserRoles(user.userName).subscribe(data=>this.assignRoles(data,user));

  }
  assignRoles(data,user){
    let temp='';
    data.roles.map(d=>{
      temp+=d.name+'<br/>';
    });
    if(temp===''){
      temp='No roles found'
    }
    this.confirmationService.confirm({
      message: temp,
      key:'confirmKeyAddRole',
      accept: () => {
        //Actual logic to perform a confirmation
        this.confirmAdd(user);
      }
    });

  }
  confirmAdd(user){
     if (!this.existsInList(user)) {
      const subscription = this.userService.addUserToRole(user.EmpNo,
        this.activeTab === 0 ? this.roleData.roles.selectedRole.data.id : this.roleTreeData.roles.selectedRole.data.id).subscribe(res => {
        this.growlService.showGrowl({
          severity: 'info',
          summary: 'Success', detail: 'User Added Successfully'
        });
        this.getRoleMembers(this.activeTab === 0 ? this.roleData.roles.selectedRole : this.roleTreeData.roles.selectedRole);
      }, err => {
        this.growlService.showGrowl({
          severity: 'error',
          summary: 'Error', detail: 'Error In Adding The User'
        });
      });
      this.addToSubscriptions(subscription);
    }
  }

  addMember(event) {
    if (event.leaf) {
      return
    }
    if(this.activeTab===0){
      this.roleData.roles.selectedRole = event;
    } else {
      this.roleTreeData.roles.selectedRole = event;
    }
    this.getRoleMembers(event);
  }

  removeMember(event) {
    let patentRole;
    if(this.activeTab===0){
      patentRole = event.parent.data.name
    } else {
      patentRole = event.parent.data.headRoleName
    }
    this.confirmationService.confirm({
      message: 'Are you sure that you want to remove ' + event.data.name + ' from ' + patentRole + '?',
      key: 'removeRoleUserConfirmation',
      accept: () => {
        const subscription = this.userService.removeUserFromRole(event.data.empNo, event.data.roleId).subscribe(res => {
          this.growlService.showGrowl({
            severity: 'info',
            summary: 'Success', detail: 'Member Removed Successfully'
          });
          this.getRoleMembers(event.parent);
        }, err => {
          this.growlService.showGrowl({
            severity: 'error',
            summary: 'Error', detail: 'Error In Removing The Member'
          });
        });
        this.addToSubscriptions(subscription);
      }
    });

  }

  searchUsersList() {
    // const subscription = this.userService.searchUsersList('USER', this.roleData.roles.model.searchText,
    //   this.roleData.roles.model.selectedCriterion,'')
    //   .subscribe(res => {
    //     this.roleData.roles.result = res;
    //     this.roleData.roles.model.searchText = '';
    //   }, err => {
    //
    //   });
    // this.addToSubscriptions(subscription);
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
      const subscription = this.userService.searchEcmUsers(this.searchQueary).subscribe(data => {
        if(data.length === 0){
            this.growlService.showGrowl({
              severity: 'error',
              summary: 'Failure', detail: 'No Results Found'
            });
        }
        this.roleData.roles.result = data;
      });
      this.coreService.progress = {busy: subscription, message: '', backdrop: true};
      this.addToSubscriptions(subscription)
    }
  }

  searchRole() {
    this.roleData.roles.roleTree = this.roleData.roles.oRoleTree.filter(e =>
      e.data.name.toUpperCase().indexOf(this.roleData.roles.model.query.toUpperCase()) !== -1
    );
  }

  clearSubscriptions() {
    this.subscriptions.map(s => {
      s.unsubscribe();
    });
  }

  addToSubscriptions(subscription) {
    this.subscriptions.push(subscription);
  }

  orgCodeTypeChanged(event){
    if(!this.editMode){
      this.editRole.orgCode = undefined;
    }
  }

  typeChanged(event){
    this.editRole = Object.assign({},new Role());
    this.editRole.type = event.value;
    this.roleData.roles.parentRoleList = [];
    const subscription = this.userService.getRolesByType(event.value+1).subscribe(res=> {
        res.map((R, I) => {
            this.roleData.roles.parentRoleList.push({label: R.name, value: R.id});
        });
      });
  }

  prepareAddRole(){
    this.showEditRole=true;
    this.roleData.roles.selectedOrgCodeType = true;
  }

  populateParentRoles(event,cb?){
    if(event.orgCode){
      this.editRoleWithOrg = true;
    } else {
      this.editRoleWithOrg = false;
    }
    this.roleData.roles.parentRoleList = [];
    const type = event.type;
    const typeId = +type;
    if(typeId<3){
      const subscription = this.userService.getRolesByType(typeId+1).subscribe(res=> {
        res.map((R, I) => {
            this.roleData.roles.parentRoleList.push({label: R.name, value: R.id});
            if(res.length === I+1){
              cb();
            }
        });
      });
      this.coreService.progress = {busy: subscription, message: '', backdrop: true};
      this.addToSubscriptions(subscription);
    } else {
      cb();
    }
  }
  editRoleItem(event) {
    this.populateParentRoles(event,()=> {
      this.editRole = Object.assign({}, event);
      this.editMode = true;
      this.showEditRole = true;
    });
  }

  editTreeRoleItem(event) {
    this.roleData.roles.roles.map((r,index)=>{
      if(r.id===event.id){
        this.populateParentRoles(r,()=>{
          this.editRole = Object.assign({},r);
          this.editMode = true;
          this.showEditRole = true;
        });
      }
    });
  }
  saveRole() {
/*    if (!this.editMode) {
      this.editRole.type = 1;
    }*/
    const subscription = this.userService.saveRole(this.editRole).subscribe(res => {
      this.growlService.showGrowl({
        severity: 'info',
        summary: 'Success', detail: 'Saved Successfully'
      });
      this.closeModel();
      if(this.activeTab === 0){
        this.getOrgRole();
      } else {
        this.getRoles();
      }
    }, Error => {
      this.growlService.showGrowl({
        severity: 'error',
        summary: 'Error', detail: 'Operation Failed'
      });
    });
    this.coreService.progress = {busy: subscription, message: '', backdrop: true};
    this.addToSubscriptions(subscription);
  }

  closeModel() {
    this.showEditRole = false;
    this.editRole = Object.assign({},new Role());
    this.editMode = false;
    this.editRoleWithOrg = false;
    this.roleData.roles.parentRoleList = [];
  }

  search(event) {
    const subscription = this.as.searchOrgUnits(event.query).subscribe(data => {
      this.suggestionsResults = [];
      for (const orgunit of data) {
        this.suggestionsResults.push(orgunit.orgCode);
      }
    });
    this.coreService.progress = {busy: subscription, message: '', backdrop: true};
    this.addToSubscriptions(subscription);
  }

  deleteRole(event) {
    this.confirmationService.confirm({
      message: 'Are you sure that you want to perform this action?',
      key: 'confirmKey',
      accept: () => {
         this.confirmDeleteRole(event);
      }
    });
  }

  confirmDeleteRole(event) {
    const subscription = this.userService.deleteRole(event.id).subscribe(res => {
      this.growlService.showGrowl({
        severity: 'info',
        summary: 'Success', detail: 'Deleted Successfully'
      });
      this.getRoles();
      this.getOrgRole();
    }, Error => {
      this.growlService.showGrowl({
        severity: 'error',
        summary: 'Error', detail: 'Failed To Delete'
      });
    });
    this.coreService.progress = {busy: subscription, message: '', backdrop: true};
    this.addToSubscriptions(subscription);
  }

  getGranteesSuggestion(event) {
    const subscription = this.as.searchLDAPGroups(event.query).subscribe(res => {
      this.granteesSuggestion = [];
        res.map((group, index)=>{
          this.granteesSuggestion.push(group.name);
        });
      }, err => {

    });
    this.addToSubscriptions(subscription);
    this.coreService.progress = {busy: subscription, message: '', backdrop: false};
  }

  tabChange(event){
    this.activeTab = event.index;
    if(event.index === 1){
      this.getRoles();
    } else {
      this.getOrgRole();
    }
    if(this.roleData.roles.selectedRole){
      this.roleData.roles.selectedRole=undefined;
    }
    if(this.roleTreeData.roles.selectedRole){
      this.roleTreeData.roles.selectedRole=undefined;
    }
    this.roleData.roles.result = [];
    this.searchQueary = { userName:undefined,mail:undefined,title:undefined,phone:undefined,orgCode:undefined,
                            empNo:undefined,userType:undefined,filter: ''};

  }
  getOrgRole() {
    const subscription = this.userService.getTopRolesList().subscribe(res => {
      const response = res;
      this.tmpRoleTree = [];
      res.map((head)=>{
        this.tmpRoleTree.push({
          label: head.headRoleName,
          data: head,
          expandedIcon: this.roleTreeExpandedIcon,
          collapsedIcon: this.roleTreeCollapsedIcon,
          leaf: false,
          expanded: false,
          selectable:head.orgCode ? true:false
        });
      });

      this.roleTreeData.roles.roleTree = this.tmpRoleTree;
    }, err => {
    });
    this.coreService.progress = {busy: subscription, message: '', backdrop: true};
    this.addToSubscriptions(subscription);
  }
  getSubOrgRoles(parent) {
    const subscription = this.userService.getSubRolesList(parent.data.id).subscribe((res:any) => {
      parent.children = [];
      res.map(d => {
        parent.children.push({
          label: d.headRoleName,
          data: d,
          expandedIcon: this.roleTreeExpandedIcon,
          collapsedIcon: this.roleTreeCollapsedIcon,
          leaf: false,
          selectable: d.orgCode ? true:false
        });
      });
    }, err => {

    });
     this.coreService.progress = {busy: subscription, message: '', backdrop: true};
    this.addToSubscriptions(subscription);
  }
  exportToExcel(){
     let array=['name'];
    this.coreService.exportToExcel( this.roleData.roles.roles,'ECM_Users.xlsx',array)
  }
  ngOnDestroy() {
    this.clearSubscriptions();
    localStorage.setItem('split-pane',null);
    this.editRoleWithOrg = false;
  }
}
