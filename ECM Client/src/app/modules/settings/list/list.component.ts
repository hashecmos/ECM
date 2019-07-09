import {Component, OnInit, OnChanges, OnDestroy} from '@angular/core';
import {UserService} from '../../../services/user.service';
import {Subscription} from 'rxjs/Subscription';
import {ConfirmationService, SelectItem} from 'primeng/primeng';
import * as global from '../../../global.variables';
import {User} from '../../../models/user/user.model';
import {UserList} from '../../../models/user/user-list.model';
import {BreadcrumbService} from '../../../services/breadcrumb.service';
import {GrowlService} from '../../../services/growl.service';
import * as $ from 'jquery';
import {CoreService} from "../../../services/core.service";

@Component({
  selector: 'app-list',
  templateUrl: './list.component.html',
  styleUrls: ['./list.component.css']
})
export class ListComponent implements OnInit, OnDestroy {
  public user: User;
  private subscriptions: Subscription[] = [];
  public userList: any;
  public distList = {'id': 1, 'empNo': 1002, 'name': 'Distribution List', lists: []};
  public defaultList = {'id': -1, 'empNo': 1002, 'name': 'Default List'};
  public listMembers: any[];
  private listUsers: any[] = [];
  private selectedParentList: any;
  public showingUsers = false;
  public criteria: any[];
  public searchStarted: boolean;
  public searchText: any;
  public listName: any;
  public SelectedUserList = [];
  public startAddUser = false;
  public userLists: any;
  private newDistList = true;
  public selectedIndex: any;
  public selectedList = '';
  private updateList = new UserList();
  public searchTypes = [
    {label: 'User', value: 'USER', icon: 'fa fa-fw fa-cc-paypal'},
    {label: 'Role', value: 'ROLE', icon: 'fa fa-fw fa-cc-visa'}
  ];
  public selectedType = 'USER';
  public showRoleTree = false;
  public showRoleList = false;
  roleTreeExpandedIcon = 'ui-icon-people-outline';
  roleTreeCollapsedIcon = 'ui-icon-people';
  private roleData: any = {roles: {model: {}}};
  public dynamicCriteria: any[] = [];
  private tmpRoleTree: any[];
  isSaveDisabled=true;

  public searchQueary = {
    userName: undefined, mail: undefined, title: undefined, phone: undefined, orgCode: undefined,
    empNo: undefined, userType: undefined, filter: ''
  };

  constructor(private userService: UserService, private breadcrumbService: BreadcrumbService, private growlService: GrowlService,
              private confirmationService: ConfirmationService, private coreService: CoreService) {
    this.roleData.roles = {model: {}};
  }

  ngOnInit() {
    this.user = this.userService.getCurrentUser();
    this.getUserLists();
    this.criteria = [{label: 'Name', value: 'userName'}, {label: 'Email', value: 'mail'}, {
      label: 'Designation',
      value: 'title'
    },
      {label: 'Phone', value: 'phone'}, {label: 'Org Code', value: 'orgCode'}, {label: 'KOC No', value: 'empNo'}];
  }

  getUserLists() {
    this.distList.lists = [];
    this.userList = [];
    const subscription = this.userService.getUserLists(false).subscribe(data => {
      const remainings = [];
      data.map((l, i) => {
        if (l.id > 1) {
          this.distList.lists.push(l);
        } else {
          remainings.push(l);
        }
      });
      this.userList = remainings;
      this.userList.push(this.defaultList);
      this.userList.push(this.distList);
    }, Error => console.log(Error));
    this.coreService.progress = {busy: subscription, message: '', backdrop: true};
    this.addToSubscriptions(subscription)
  }

  showListMembers(event, listid, i) {
    if (listid === 0) {
      $('.ui-accordion-content').hide();
    } else {
      $('.ui-accordion-content').show();
    }
    this.clearResult();
    if (listid !== -1) {
      if (this.selectedParentList === listid) {
        this.showingUsers = !this.showingUsers;
      } else {
        this.showingUsers = true;
      }
    } else if (listid === -1) {
      this.showingUsers = false;
    }
    this.selectedParentList = listid;
    this.listUsers = [];
    const subscription = this.userService.getListUsers(listid).subscribe(data => {
      this.listMembers = data;
      for (const user of this.listMembers) {
        this.listUsers.push({'EmpNo': user.EmpNo, 'fulName': user.fulName, 'appRole': user.appRole});
      }
    });
    this.coreService.progress = {busy: subscription, message: '', backdrop: true};
    this.addToSubscriptions(subscription)
  }

  showSubList(event, listid, i) {
    $('.ui-accordion-content').show();
    this.searchStarted = false;
    this.listName = '';
    this.listUsers = [];
    if (this.selectedParentList === listid || !this.newDistList) {
      this.showingUsers = !this.showingUsers;
      const subscription = this.userService.getUserLists(false).subscribe(ldata => {
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

  clearItems() {
    this.listName = undefined;
    this.listUsers = [];
    this.newDistList = true;
  }

  showUsers(event, listId: any, name, index) {
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

    const subscription = this.userService.getListUsers(listId).subscribe(data => {
      // this.SelectedUserList = data;
      for (const user of data) {
        this.listUsers.push({'EmpNo': user.EmpNo, 'fulName': user.fulName, 'appRole': user.appRole});
      }
    });
    this.coreService.progress = {busy: subscription, message: '', backdrop: true};
    this.addToSubscriptions(subscription);
    // this.updateAvailableUsers();
    this.isSaveDisabled=false;
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

  userExist(user) {
    for (const User of this.listUsers) {
      if (user.fulName === User.fulName || user.name === User.fulName || user.headRoleName === User.fulName) {
        return true;
      }
    }
    return false;
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
    console.log(JSON.stringify(this.updateList));
    const subscription = this.userService.updateUserLists(this.updateList)
      .subscribe(res => this.updateSuccess(res), Error => this.updateSuccess(Error));

    this.coreService.progress = {busy: subscription, message: '', backdrop: true};
    this.addToSubscriptions(subscription)
  }

  updateSuccess(data) {
    this.growlService.showGrowl({
      severity: 'info',
      summary: 'Success', detail: 'List Updated'
    });
    this.listName = '';
    this.searchText = '';
    this.searchStarted = false;
    this.listUsers = [];
    this.showingUsers = false;
    this.showRoleTree = false;
    this.showRoleList = false;
    this.getUserLists();
    this.isSaveDisabled=true;
  }

  failed(error) {
    this.growlService.showGrowl({
      severity: 'error',
      summary: 'Failure', detail: 'Update Failed'
    });
  }

  existsInList($event) {

  }

  addToSubscriptions(subscription) {
    this.subscriptions.push(subscription);
  }

  clearSubscriptions() {
    this.subscriptions.map(s => {
      s.unsubscribe();
    });
  }

  confirmRemoveLink(event, listId, empno) {
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

  onSearchTypeChanged(event) {
    this.searchQueary = {
      userName: undefined, mail: undefined, title: undefined, phone: undefined, orgCode: undefined,
      empNo: undefined, userType: undefined, filter: ''
    };
  }

  showRoleTreeModel() {
    this.getOrgRole();
    this.showRoleTree = true;
  }

  showRoleListModel() {
    this.getRoles();
    this.showRoleList = true;
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

  searchRole() {
    this.roleData.roles.roleTree = this.roleData.roles.oRoleTree.filter(e =>
      e.data.name.toUpperCase().indexOf(this.roleData.roles.model.query.toUpperCase()) !== -1
    );
  }

  closeRoleTree() {

  }

  closeRoleList() {

  }

  expandNode(event) {
    this.getSubOrgRoles(event.node);
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

  getSubOrgRoles(parent) {
    const subscription = this.userService.getSubRolesList(parent.data.id).subscribe((res: any) => {
      parent.children = [];
      res.map(d => {
        parent.children.push({
          label: d.headRoleName, data: d,
          expandedIcon: this.roleTreeExpandedIcon,
          collapsedIcon: this.roleTreeCollapsedIcon,
          leaf: false
        });
      });
    }, err => {

    });
    this.coreService.progress = {busy: subscription, message: '', backdrop: true};
    this.addToSubscriptions(subscription);
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
          // user.disabled = true;
        }
      });
    } else if (event.appRole === 'ROLE') {
      this.SelectedUserList.map((user) => {
        if (user.name === event.fulName) {
          // user.disabled = true;
        }
      });
    }
  }

  ngOnDestroy() {
    this.clearSubscriptions();
    this.subscriptions = [];
    this.user = undefined;
    this.userList = [];
    this.distList = undefined;
    this.defaultList = undefined;
    this.listMembers = [];
    this.listUsers = [];
    this.selectedParentList = undefined;
    this.showingUsers = false;
    this.criteria = [];
    this.searchStarted = false;
    this.searchText = undefined;
    this.listName = undefined;
    this.SelectedUserList = [];
    this.startAddUser = false;
    this.userLists = undefined;
    this.newDistList = undefined;
    this.selectedIndex = undefined;
    this.selectedList = undefined;
    this.updateList = undefined;
  }

}
