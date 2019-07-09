import {Component, OnInit} from '@angular/core';
import {UserService} from "../../../services/user.service";
import {CoreService} from "../../../services/core.service";
import {Subscription} from "rxjs/Subscription";
import * as $ from 'jquery';
import {GrowlService} from "../../../services/growl.service";
import {User} from "../../../models/user/user.model";
import {UserList} from "../../../models/user/user-list.model";
import {ConfirmationService} from "primeng/primeng";
import {BreadcrumbService} from "../../../services/breadcrumb.service";

@Component({
  selector: 'app-ecm-global-list',
  templateUrl: './ecm-global-list.component.html',
  styleUrls: ['./ecm-global-list.component.css']
})
export class EcmGlobalListComponent implements OnInit {
  public userList: any;
  public user: User;
  private subscriptions: Subscription[] = [];
  private selectedParentList: any;
  public showingUsers = false;
  public criteria: any[];
  public searchStarted: boolean;
  private listUsers: any[] = [];
  public listName: any;
  private newDistList = true;
  public userLists: any;
  public selectedType = 'USER';
  public SelectedUserList = [];
  public searchText: any;
  public selectedIndex: any;
  public startAddUser = false;
  public selectedList = '';
  public showRoleTree = false;
  public showRoleList = false;
  private tmpRoleTree: any[];
  private roleData: any = {roles: {model: {}}};
  roleTreeExpandedIcon = 'ui-icon-people-outline';
  roleTreeCollapsedIcon = 'ui-icon-people';
  isGlobal=true;
  isSaveDisabled=true;
  public searchTypes = [
    {label: 'User', value: 'USER', icon: 'fa fa-fw fa-cc-paypal'},
    {label: 'Role', value: 'ROLE', icon: 'fa fa-fw fa-cc-visa'}
  ];

  private updateList = new UserList();
  public searchQueary = {
    userName: undefined, mail: undefined, title: undefined, phone: undefined, orgCode: undefined,
    empNo: undefined, userType: undefined, filter: ''
  };
  public distList = {'id': 1, 'empNo': 1002, 'name': 'ECM-Global List', lists: []};

  constructor(private userService: UserService, private coreService: CoreService, private growlService: GrowlService,
              private confirmationService: ConfirmationService, private breadcrumbService: BreadcrumbService) {
    this.user = this.userService.getCurrentUser();
  }

  ngOnInit() {
    this.getUserLists();
    this.breadcrumbService.setItems([
      {label: 'Admin'},
      {label: 'ECM-Global List'}
    ]);
  }
  clearItems(){
    this.listName=undefined;
    this.listUsers=[];
    this.newDistList = true;
  }

  getUserLists() {
    this.distList.lists = [];
    this.userList = [];
    const subscription = this.userService.getUserLists(true).subscribe(data => {
      const remainings = [];
      data.map((l, i) => {
        if (l.id > 1 && l.isGlobal === 'Y') {
          this.distList.lists.push(l);
        } else {
          remainings.push(l);
        }
      });
      this.userList = remainings;
      this.userList.push(this.distList);
    }, Error => console.log(Error));
    this.coreService.progress = {busy: subscription, message: '', backdrop: true};
    this.addToSubscriptions(subscription)
  }

  addToSubscriptions(subscription) {
    this.subscriptions.push(subscription);
  }

  clearSubscriptions() {
    this.subscriptions.map(s => {
      s.unsubscribe();
    });
  }
   checkChange(event) {
    this.isGlobal =event;
  }

  showSubList(event, listid, i) {
    $('.ui-accordion-content').show();
    this.searchStarted = false;
    this.listName = '';
    this.listUsers = [];
    if (this.selectedParentList === listid || !this.newDistList) {
      this.showingUsers = !this.showingUsers;
      const subscription = this.userService.getUserLists(true).subscribe(ldata => {
        this.assignUserList(ldata);
      });
      this.coreService.progress = {busy: subscription, message: '', backdrop: true};
      this.addToSubscriptions(subscription)
    } else {
      this.showingUsers = true;
    }
    this.selectedParentList = listid;
    this.newDistList = true;
  }

  assignUserList(data) {
    this.userLists = data;
    this.userLists.push(this.distList);
  }

  searchUsers() {
    let formValid = true;

    this.searchQueary.userType = this.selectedType;
    if ((this.searchQueary.userName !== undefined && this.searchQueary.userName !== '' && this.searchQueary.userName !== null) ||
      (this.searchQueary.title !== undefined && this.searchQueary.title !== '' && this.searchQueary.title !== null) ||
      (this.searchQueary.mail !== undefined && this.searchQueary.mail !== '' && this.searchQueary.mail !== null) ||
      (this.searchQueary.empNo !== undefined && this.searchQueary.empNo !== '' && this.searchQueary.empNo !== null) ||
      (this.searchQueary.orgCode !== undefined && this.searchQueary.orgCode !== '' && this.searchQueary.orgCode !== null) ||
      (this.searchQueary.phone !== undefined && this.searchQueary.phone !== '' && this.searchQueary.phone !== null)) {
    } else {
      formValid = false;
      this.growlService.showGrowl({
        severity: 'error',
        summary: 'Warning', detail: 'Fill Any One Field To Search'
      });
    }
    if (formValid) {
      this.searchStarted = true;
      const subscription = this.userService.searchEcmUsers(this.searchQueary).subscribe(data => {
        if (data.length === 0) {
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

  clearResult() {
    this.searchStarted = false;
    this.searchText = '';
  }

  userExist(user) {
    for (const User of this.listUsers) {
      if (user.fulName === User.fulName || user.name === User.fulName || user.headRoleName === User.fulName) {
        return true;
      }
    }
    return false;
  }

  showRoleTreeModel() {
    this.getOrgRole();
    this.showRoleTree = true;
  }

  showRoleListModel() {
    this.getRoles();
    this.showRoleList = true;
  }

  selectUser(user) {
    const exist = this.userExist(user);

    if (!exist) {
      if (this.selectedType === 'USER') {
        this.listUsers.push({'EmpNo': user.EmpNo, 'fulName': user.fulName, 'appRole': 'USER'});
      } else if (this.selectedType === 'ROLE') {
        if (user.name) {
          this.listUsers.push({'EmpNo': user.id, 'fulName': user.name, 'appRole': 'ROLE'});
        } else if (user.headRoleName) {
          this.listUsers.push({'EmpNo': user.id, 'fulName': user.headRoleName, 'appRole': 'ROLE'});
        }
      }
    }
    user.disabled = true;
    this.isSaveDisabled=false;
  }

  getOrgRole() {
    const subscription = this.userService.getTopRolesList().subscribe(res => {
      const response = res;
      this.tmpRoleTree = [];
      res.map((head) => {
        this.tmpRoleTree.push({
          label: head.headRoleName,
          data: head,
          expandedIcon: this.roleTreeExpandedIcon,
          collapsedIcon: this.roleTreeCollapsedIcon,
          leaf: false,
          expanded: false
        });
      });

      this.roleData.roles.roleTree = this.tmpRoleTree;
    }, err => {
    });
    this.coreService.progress = {busy: subscription, message: '', backdrop: true};
    this.addToSubscriptions(subscription);
  }
    confirmRemoveLink(event,listId, empno) {
    event.stopPropagation();
    this.confirmationService.confirm({
      message: 'Are you sure that you want to perform this action?',
      key: 'confirmKey',
      accept: () => {
         this.removeDlList(listId, empno);
      }
    });
  }

  removeDlList(listId, empno) {
    const subscription = this.userService.removeDistList(listId, empno).subscribe(res => {
      this.growlService.showGrowl({
        severity: 'info',
        summary: 'Success', detail: 'Deleted Successfully'
      });
      this.listName = '';
      this.searchText = '';
      this.searchStarted = false;
      this.listUsers = [];
      this.showingUsers = false;
      this.getUserLists();
    }, Error => {
      this.growlService.showGrowl({
        severity: 'error',
        summary: 'Failure', detail: 'Failed To Delete'
      });
    });
    this.coreService.progress = {busy: subscription, message: '', backdrop: true};
    this.addToSubscriptions(subscription)
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
    }, err => {
    });
    this.coreService.progress = {busy: subscription, message: '', backdrop: true};
    this.addToSubscriptions(subscription);
  }

  showUsers(event, listId: any, name, index,lst) {
    event.stopPropagation();
    this.listUsers = [];
    this.startAddUser = false;
    this.showingUsers = true;
    this.selectedParentList = listId;
    this.SelectedUserList = [];
    this.selectedList = listId;
    this.newDistList = false;
    this.selectedIndex = index;
    this.listName = name;
    this.isSaveDisabled=false;
    // if(lst.isGlobal==='Y'){
    //   this.isGlobal= true;
    // }
    // else{
    //   this.isGlobal= false;
    // }



    const subscription = this.userService.getListUsers(listId).subscribe(data => {
      //this.SelectedUserList = data;
      for (const user of data) {
        this.listUsers.push({'EmpNo': user.EmpNo, 'fulName': user.fulName, 'appRole': user.appRole});
      }
    });
    this.coreService.progress = {busy: subscription, message: '', backdrop: true};
    this.addToSubscriptions(subscription);
    if (this.SelectedUserList == null) {
      this.SelectedUserList = [];
    }

  }

  save() {
    if (this.selectedParentList === 0) {
      this.updateList.name = 'Favourites';
    } else {
      this.updateList.name = this.listName;
    }
    this.updateList.empNo = this.user.EmpNo;
    if (this.newDistList) {
      this.updateList.id = 0;
    } else {
      this.updateList.id = this.selectedParentList;
    }
    this.listUsers.map((user) => {
      delete user.members;
    });
    this.updateList.users = this.listUsers;
     if(this.isGlobal===true){
      this.updateList.isGlobal='Y';
    }
    else{
       this.updateList.isGlobal='N';
    }
    const subscription = this.userService.updateUserLists(this.updateList)
      .subscribe(res => this.updateSuccess(res), Error => this.updateSuccess(Error));

    this.coreService.progress = {busy: subscription, message: '', backdrop: true};
    this.addToSubscriptions(subscription)
  }

  onSearchTypeChanged(event) {

    this.searchQueary = {
      userName: undefined, mail: undefined, title: undefined, phone: undefined, orgCode: undefined,
      empNo: undefined, userType: undefined, filter: ''
    };
  }

  updateSuccess(data) {
    this.growlService.showGrowl({
      severity: 'info',
      summary: 'Success', detail: 'List Updated'
    });
   // this.listName = '';
   // this.searchText = '';
    this.searchStarted = false;
   // this.listUsers = [];
    this.showingUsers = false;
    this.showRoleTree = false;
    this.showRoleList = false;
    this.isGlobal=true;
    this.getUserLists();
    this.isSaveDisabled=true;
  }

  getRoleMembers(role) {
    if (!role.members) {
      let RoleNameString = '';
      let roleId;
      if (role.headRoleId) {
        roleId = role.headRoleId
      } else if (role.id) {
        roleId = role.id
      } else if (role.EmpNo) {
        roleId = role.EmpNo
      }
      const subscription = this.userService.getRoleMembers(roleId).subscribe((res: any) => {
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

  onRemove(event) {
    if (event.appRole === 'USER') {
      this.SelectedUserList.map((user) => {
        if (user.fulName === event.fulName) {
          user.disabled = false;
        }
      });
    } else if (event.appRole === 'ROLE') {
      this.SelectedUserList.map((user) => {
        if (user.name === event.fulName) {
          user.disabled = false;
        }
      });
    }


  }
}
