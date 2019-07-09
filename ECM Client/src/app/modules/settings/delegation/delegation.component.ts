import {Component, OnDestroy, OnInit} from '@angular/core';
import {ConfirmationService, OverlayPanel, SelectItem, TreeNode} from 'primeng/primeng';
import {Subscription} from 'rxjs/Subscription';
import {UserService} from '../../../services/user.service';
import {User} from '../../../models/user/user.model';
import {GrowlService} from '../../../services/growl.service';
import {BreadcrumbService} from '../../../services/breadcrumb.service';
import {DelegateModel} from '../../../models/user/delegate.model';
import * as global from '../../../global.variables';
import {CoreService} from '../../../services/core.service';
import {Recipients} from '../../../models/user/recipients.model';

@Component({
  selector: 'app-delegation',
  templateUrl: './delegation.component.html',
  styleUrls: ['./delegation.component.css']
})
export class DelegationComponent implements OnInit, OnDestroy {
  criteria: SelectItem[];
  selectedcriteria: string;
  searchStarted: boolean;
  userDelegation = new DelegateModel();
  colHeaders: any[];
  colHeaderUsers: any[];
  roleDelegation = new DelegateModel();
  delegatedRoles: any[] = [];
  delegatedUsers: any;
  searchText: any;
  user: any;
  userSelected: any[] = [];
  roles: any[];
  selectedRole: string;
  isUnlimited = false;
  toDate: any = undefined;
  fromDate: any = undefined;
  delegateId: any = undefined;
  editEnabled = false;
  delegationId: any;
  delegatedOn: any;
  delName: any;
  minTo: Date;
  today: Date;
  private subscription: Subscription[] = [];
  public SelectedUserList = [];
  emptyMessage: any;
  selectedRoleMembers: any[] = [];
  public searchQueary = {
    userName: undefined, mail: undefined, title: undefined, phone: undefined, orgCode: undefined,
    empNo: undefined, userType: undefined, filter: ''
  };

  constructor(private us: UserService, private confirmationService: ConfirmationService, private growlService: GrowlService,
              private breadcrumbService: BreadcrumbService, private coreService: CoreService) {
    this.userSelected = [];
  }
  removeFromRole(e){
    e.id=e.del;
    this.confirmationService.confirm({
      message: 'Are you sure that you want to remove' + ' ' + e.name + ' ' + 'from Delegation?',
      accept: () => {
        //Actual logic to perform a confirmation
        this.revokeDelegation(e);
      }
    });
  }

  ngOnInit() {
    this.today = new Date();
    this.user = this.us.getCurrentUser();
    this.emptyMessage = global.no_del;
    this.criteria = [];
    this.roles = [];
    this.selectedcriteria = 'NAME';
    for (const role of this.user.roles) {
      this.roles.push({label: role.name, value: role.id});
    }
    if (this.user.roles.length > 0) {
      this.selectedRole = this.user.roles[0].id;
      //this.onSelectionChange({value:this.selectedRole});
    }
    this.criteria.push({label: 'Email', value: 'EMAIL'});
    this.criteria.push({label: 'Name', value: 'NAME'});
    this.criteria.push({label: 'Designation', value: 'TITLE'});
    this.criteria.push({label: 'Phone', value: 'PHONE'});
    this.criteria.push({label: 'Org Code', value: 'ORGCODE'});
    this.criteria.push({label: 'Koc No', value: 'KOCNO'});
    this.colHeaderUsers = [
      {field: 'delName', header: 'Name'},
      {field: 'delegatedByName', header: 'Delegated By'},
      {field: 'fromDate', header: 'Active From'},
      {field: 'toDate', header: 'Expire On'},
    ];
    this.colHeaders = [
      {field: 'delName', header: 'Name'},
      {field: 'delegatedByName', header: 'Delegated By'},
      {field: 'fromDate', header: 'Active From'},
      {field: 'toDate', header: 'Expire On'},
    ];
    const subscription = this.us.getUserDelegation().subscribe(val => {
      val.map((d, i) => {
        if (d.fromDate !== undefined) {
          d.fromDate = this.coreService.formatDateForDelegate(d.fromDate);
        }
        if (d.toDate !== undefined) {
          d.toDate = this.coreService.formatDateForDelegate(d.toDate);
        }
      });
      this.delegatedUsers = val;
    });
    this.coreService.progress = {busy: subscription, message: '', backdrop: true};
    this.addToSubscriptions(subscription);
    if (this.user.roles.length > 0) {
      const allRoleDelegations = [];
      this.user.roles.map((role) => {
        const subscription1 = this.us.getRoleDelegation(role.id).subscribe(val => {
          val.map((d, i) => {
            if (d.fromDate !== undefined) {
              d.fromDate = this.coreService.formatDateForDelegate(d.fromDate);
            }
            if (d.toDate !== undefined) {
              d.toDate = this.coreService.formatDateForDelegate(d.toDate);
            }
            allRoleDelegations.push(d);
          });
          this.delegatedRoles = Object.assign([], allRoleDelegations);
        });
        this.coreService.progress = {busy: subscription1, message: '', backdrop: true};
        this.addToSubscriptions(subscription1);
        this.onSelectionChange({value: role.id});
      });
    }
  }

  clearResult() {
    this.searchStarted = false;
    //this.searchText = '';
  }

  checkChange(event) {
    if (event === true) {
      this.isUnlimited = true;
      this.toDate = undefined;
    }
    else {
      this.isUnlimited = false;

    }
  }

  confirm(event) {
    this.confirmationService.confirm({
      message: 'Are you sure that you want to remove' + ' ' + event.delName + ' ' + 'from Delegation?',
      accept: () => {
        //Actual logic to perform a confirmation
        this.revokeDelegation(event);
      }
    });
  }

  searchUsers() {
    /*this.searchStarted = true;
    const subscription = this.us.searchUsersList('USER', this.searchText, this.selectedcriteria,'').subscribe(data => {
      this.SelectedUserList = data;
    });
    this.coreService.progress = {busy: subscription, message: '', backdrop: true};
    this.addToSubscriptions(subscription);*/

    let formValid = true;
    this.searchQueary.userType = 'USER';
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
      const subscription = this.us.searchEcmUsers(this.searchQueary).subscribe(data => {
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


  selectUser(user) {
    if (!this.editEnabled) {
      this.userSelected = [{'fulName': user.fulName}];
      this.delegateId = user.EmpNo;
    }
  }

  unSelected(event) {
    this.delegateId = undefined;
  }

  addDelegationUser() {
    if (this.isUnlimited === false) {
      if (this.delegateId && this.fromDate && this.toDate) {
        this.userDelegation.id = 0;
        this.userDelegation.delegateId = this.delegateId;
        this.userDelegation.delegatedBy = this.user.EmpNo;
        this.userDelegation.fromDate = this.fromDate;
        this.userDelegation.toDate = this.toDate;
        this.userDelegation.userId = this.user.EmpNo;
        this.userDelegation.userType = 'USER';
        if (this.editEnabled) {
          this.userDelegation.status = 'ACTIVE';
          this.userDelegation.id = this.delegationId;
          this.userDelegation.delName = this.delName;
          this.userDelegation.delegatedOn = this.delegatedOn;
        }
        if (this.userDelegation.delegateId !== this.user.EmpNo) {
          const subscription = this.us.saveDelegation(this.userDelegation).subscribe(data => this.addUserSuccessfull(data), error => this.addUserFailed());
          this.coreService.progress = {busy: subscription, message: '', backdrop: true};
          this.addToSubscriptions(subscription);
        }
        else {
          this.growlService.showGrowl({
            severity: 'error',
            summary: 'Not Allowed', detail: 'Cannot Delegate To Self'
          });
        }
      }
      else {
        let message = 'Select User and Active From';
        if (!this.delegateId) {
          message = 'Select User';
        } else if (!this.fromDate) {
          message = 'Select Active From';
        }
        else if (!this.toDate) {
          message = 'Select Expire On';
        }
        this.growlService.showGrowl({
          severity: 'error',
          summary: 'Fill Required', detail: message
        });
      }
    } else {
      if (this.delegateId && this.fromDate) {
        this.userDelegation.id = 0;
        this.userDelegation.delegateId = this.delegateId;
        this.userDelegation.delegatedBy = this.user.EmpNo;
        this.userDelegation.fromDate = this.fromDate;
        this.userDelegation.toDate = this.toDate;
        this.userDelegation.userId = this.user.EmpNo;
        this.userDelegation.userType = 'USER';
        if (this.editEnabled) {
          this.userDelegation.status = 'ACTIVE';
          this.userDelegation.id = this.delegationId;
          this.userDelegation.delName = this.delName;
          this.userDelegation.delegatedOn = this.delegatedOn;
        }
        if (this.userDelegation.delegateId !== this.user.EmpNo) {
          const subscription = this.us.saveDelegation(this.userDelegation).subscribe(data => this.addUserSuccessfull(data), error => this.addUserFailed());
          this.coreService.progress = {busy: subscription, message: '', backdrop: true};
          this.addToSubscriptions(subscription);
        }
        else {
          this.growlService.showGrowl({
            severity: 'error',
            summary: 'Not Allowed', detail: 'Cannot Delegate To Self'
          });
        }
      }
      else {
        let message = 'Select User and Active From';
        if (!this.delegateId) {
          message = 'Select User';
        } else if (!this.fromDate) {
          message = 'Select Active From';
        }
        this.growlService.showGrowl({
          severity: 'error',
          summary: 'Fill Required', detail: message
        });
      }
    }
  }

  addUserSuccessfull(data) {
    if (data._body === 'EXISTS') {
      this.growlService.showGrowl({
        severity: 'warn',
        summary: 'Already Exist', detail: 'User Already Exist'
      });
    }
    else {
      if (this.editEnabled) {
        this.growlService.showGrowl({
          severity: 'success',
          summary: 'Success', detail: 'Saved Successfully'
        });
      }
      else {
        this.growlService.showGrowl({
          severity: 'info',
          summary: 'Success', detail: 'Added Successfully'
        });
      }
    }
    this.editEnabled = false;
    const subscription = this.us.getUserDelegation().subscribe(val => {
      val.map((d, i) => {
        if (d.fromDate !== undefined) {
          d.fromDate = this.coreService.formatDateForDelegate(d.fromDate);
        }
        if (d.toDate !== undefined) {
          d.toDate = this.coreService.formatDateForDelegate(d.toDate);
        }
      });
      this.delegatedUsers = val;
    });
    this.coreService.progress = {busy: subscription, message: '', backdrop: true};
    this.addToSubscriptions(subscription);
    this.fromDate = undefined;
    this.toDate = '';
    this.userSelected = [];
    this.isUnlimited = false;
    this.delegateId = undefined;
    this.userDelegation.fromDate = '';
    this.searchText = undefined;
    this.SelectedUserList = [];
  }

  addUserFailed() {
    this.growlService.showGrowl({
      severity: 'error',
      summary: 'Failure', detail: 'Add Delegation Failed'
    });
  }

  addDelegationRole() {
    if (this.isUnlimited === false) {
      if (this.delegateId && this.fromDate && this.toDate) {
        this.roleDelegation.id = 0;
        this.roleDelegation.delegateId = this.delegateId;
        this.roleDelegation.delegatedBy = this.user.EmpNo;
        this.roleDelegation.fromDate = this.fromDate;
        this.roleDelegation.toDate = this.toDate;
        this.roleDelegation.userId = this.selectedRole;
        this.roleDelegation.userType = 'ROLE';
        if (this.editEnabled) {
          this.roleDelegation.status = 'ACTIVE';
          this.roleDelegation.id = this.delegationId;
          this.roleDelegation.delName = this.delName;
          this.roleDelegation.delegatedOn = this.delegatedOn;
        }
        if (this.roleDelegation.delegateId !== this.user.EmpNo) {
          const subscription = this.us.saveDelegation(this.roleDelegation).subscribe(data => this.addRoleSuccessfull(data), error => this.addUserRoleFailed());
          this.coreService.progress = {busy: subscription, message: '', backdrop: true};
          this.addToSubscriptions(subscription);
        }
        else {
          this.growlService.showGrowl({
            severity: 'error',
            summary: 'Not Allowed', detail: 'Cannot Delegate To Self'
          });
        }
      }
      else {
        let message = 'Select User and Active From';
        if (!this.delegateId) {
          message = 'Select User';
        } else if (!this.fromDate) {
          message = 'Select Active From';
        }
        else if (!this.toDate) {
          message = 'Select Expire On';
        }
        this.growlService.showGrowl({
          severity: 'error',
          summary: 'Fill Required', detail: message
        });
      }
    }
    else {
      if (this.delegateId && this.fromDate) {
        this.roleDelegation.id = 0;
        this.roleDelegation.delegateId = this.delegateId;
        this.roleDelegation.delegatedBy = this.user.EmpNo;
        this.roleDelegation.fromDate = this.fromDate;
        this.roleDelegation.toDate = this.toDate;
        this.roleDelegation.userId = this.selectedRole;
        this.roleDelegation.userType = 'ROLE';
        if (this.editEnabled) {
          this.roleDelegation.status = 'ACTIVE';
          this.roleDelegation.id = this.delegationId;
          this.roleDelegation.delName = this.delName;
          this.roleDelegation.delegatedOn = this.delegatedOn;
        }
        if (this.roleDelegation.delegateId !== this.user.EmpNo) {
          const subscription = this.us.saveDelegation(this.roleDelegation).subscribe(data => this.addRoleSuccessfull(data), error => this.addUserRoleFailed());
          this.coreService.progress = {busy: subscription, message: '', backdrop: true};
          this.addToSubscriptions(subscription);
        }
        else {
          this.growlService.showGrowl({
            severity: 'error',
            summary: 'Not Allowed', detail: 'Cannot Delegate To Self'
          });
        }
      }
      else {
        let message = 'Select User and Active From';
        if (!this.delegateId) {
          message = 'Select User';
        } else if (!this.fromDate) {
          message = 'Select Active From';
        }
        this.growlService.showGrowl({
          severity: 'error',
          summary: 'Fill Required', detail: message
        });
      }
    }
  }

  addRoleSuccessfull(data) {
    if (data._body === 'EXISTS') {
      this.growlService.showGrowl({
        severity: 'warn',
        summary: 'Already Exist', detail: 'User Already Exist'
      });
    }
    else {
      if (this.editEnabled) {
        this.growlService.showGrowl({
          severity: 'success',
          summary: 'Success', detail: 'Saved Successfully'
        });
      }
      else {
        this.growlService.showGrowl({
          severity: 'info',
          summary: 'Success', detail: 'Added Successfully'
        });
        this.onSelectionChange({value: this.selectedRole});
      }
    }
    this.editEnabled = false;
    // const subscription = this.us.getRoleDelegation().subscribe(val => {
    //   val.map((d, i) => {
    //     if (d.fromDate !== undefined) {
    //       d.fromDate = this.coreService.formatDateForDelegate(d.fromDate);
    //     }
    //     if (d.toDate !== undefined) {
    //       d.toDate = this.coreService.formatDateForDelegate(d.toDate);
    //     }
    //   });
    //   this.delegatedRoles = val;
    // });
    const allRoleDelegations = [];
    this.user.roles.map((role) => {
      const subscription1 = this.us.getRoleDelegation(role.id).subscribe(val => {
        val.map((d, i) => {
          if (d.fromDate !== undefined) {
            d.fromDate = this.coreService.formatDateForDelegate(d.fromDate);
          }
          if (d.toDate !== undefined) {
            d.toDate = this.coreService.formatDateForDelegate(d.toDate);
          }
          allRoleDelegations.push(d);
        });
        this.delegatedRoles = Object.assign([], allRoleDelegations);
      });
      this.coreService.progress = {busy: subscription1, message: '', backdrop: true};
      this.addToSubscriptions(subscription1);
      //this.onSelectionChange({value:role.id});
    });
    this.fromDate = undefined;
    this.toDate = '';
    this.userSelected = [];
    this.isUnlimited = false;
    this.delegateId = undefined;
    this.roleDelegation.fromDate = '';
  }

  cancel() {
    this.editEnabled = false;
    this.fromDate = undefined;
    this.toDate = '';
    this.userSelected = [];
    this.isUnlimited = false;
    this.delegateId = undefined;
    this.userDelegation.fromDate = '';
    this.searchText = undefined;
    this.SelectedUserList = [];
  }

  addUserRoleFailed() {
    this.growlService.showGrowl({
      severity: 'error',
      summary: 'Failure', detail: 'Add Delegate Failed'
    });
  }

  revokeDelegation(del) {
    console.log(del);
    if (del.id === 0) {
      const subscription = this.us.removeUserFromRole(del.empNo, del.roleId).subscribe(res => {
        if (res === 'OK') {
          this.growlService.showGrowl({
            severity: 'info',
            summary: 'Success', detail: 'Removed Delegation'
          });
          this.removeDelegationSuccess();


        } else {
          this.growlService.showGrowl({
            severity: 'error',
            summary: 'Failure', detail: 'Failed To Remove Delegation'
          });
        }
      });
    }
    else {
      const subscription = this.us.revokeDelegation(del.id).subscribe(data => {
        if (data === 'OK') {
          this.growlService.showGrowl({
            severity: 'info',
            summary: 'Success', detail: 'Removed Delegation'
          });
          this.removeDelegationSuccess();
          //this.onSelectionChange({value:this.selectedRole});

        } else {
          this.growlService.showGrowl({
            severity: 'error',
            summary: 'Failure', detail: 'Failed To Remove Delegation'
          });
        }
      });
      this.coreService.progress = {busy: subscription, message: '', backdrop: true};
      this.addToSubscriptions(subscription);
    }


  }
  removeDelegationSuccess(){

    this.fromDate = undefined;
    this.toDate = '';
    this.userSelected = [];
    this.isUnlimited = false;
    this.delegateId = undefined;
    this.roleDelegation.fromDate = '';
    this.editEnabled = false;
    if (this.user.roles.length > 0) {
      const allRoleDelegations = [];
      this.user.roles.map((role) => {
        const subscription1 = this.us.getRoleDelegation(role.id).subscribe(val => {
          val.map((d, i) => {
            if (d.fromDate !== undefined) {
              d.fromDate = this.coreService.formatDateForDelegate(d.fromDate);
            }
            if (d.toDate !== undefined) {
              d.toDate = this.coreService.formatDateForDelegate(d.toDate);
            }
            allRoleDelegations.push(d);
          });
          this.delegatedRoles = Object.assign([], allRoleDelegations);
        });
        this.coreService.progress = {busy: subscription1, message: '', backdrop: true};
        this.addToSubscriptions(subscription1);
        this.onSelectionChange({value: role.id});
      });

    } else {
      const subscription1 = this.us.getUserDelegation().subscribe(val => {
        val.map((d, i) => {
          if (d.fromDate !== undefined) {
            d.fromDate = this.coreService.formatDateForDelegate(d.fromDate);
          }
          if (d.toDate !== undefined) {
            d.toDate = this.coreService.formatDateForDelegate(d.toDate);
          }
        });
        this.delegatedUsers = val;
      });
      this.coreService.progress = {busy: subscription1, message: '', backdrop: true};
      this.addToSubscriptions(subscription1);
    }
  }

  editDelegationRole(data) {
    this.editEnabled = true;
    this.fromDate = data.fromDate;
    this.toDate = data.toDate;
    this.userSelected = [{'fulName': data.delName}];
    this.delegateId = data.delegateId;
    this.delegationId = data.id;
    this.delName = data.delName;
    this.delegatedOn = data.delegatedOn;
    this.changeFrom(data.fromDate);
    this.selectedRole = data.userId;
    if (!this.toDate) {
      this.isUnlimited = true;
    }
  }

  editDelegationUser(data) {
    this.editEnabled = true;
    this.fromDate = data.fromDate;
    this.toDate = data.toDate;
    this.userSelected = [{'fulName': data.delName}];
    this.delegateId = data.delegateId;
    this.delegationId = data.id;
    this.delName = data.delName;
    this.delegatedOn = data.delegatedOn;
    this.changeFrom(data.fromDate);
  }

  changeFrom(event) {
    // let temp= new Date(event);
    // temp.setDate(temp.getDate() + 1);
    // this.minTo = temp;
    this.minTo = new Date(event);
  }

  changeTo(event) {
    this.toDate = this.coreService.formatDateForDelegate(event);
  }

  clearSubscriptions() {
    this.subscription.map(s => {
      s.unsubscribe();
    });
  }

  getRoleMembers(role) {
    if (!role.members) {
      let RoleNameString = '';
      const subscription = this.us.getRoleMembers(role.value).subscribe((res: any) => {
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

  showRoleMembers(data) {
    if (!data.members) {
      const subscription = this.us.getRoleMembers(data.userId).subscribe((res: any) => {
        res.map(d => {
          d.delName = d.name;
          d.delegateId = d.empNo;
          d.id = d.delId;
          if (d.fromDate === 'Unlimited') {
            d.fromDate = undefined;
          } else if (d.fromDate === "") {
            d.fromDate = '-';
          }
          else {
            d.fromDate = this.coreService.formatDateForDelegateDDMMYY(d.fromDate);

          }

          if (d.toDate === 'Unlimited') {
            d.toDate = undefined;
            d.todate = "-";
          }

          else {
            d.toDate = this.coreService.formatDateForDelegateDDMMYY(d.toDate);
            d.todate = d.toDate;

          }
        });
        data.members = res;

      }, err => {

      });
      this.coreService.progress = {busy: subscription, message: '', backdrop: true};
      this.addToSubscriptions(subscription);
    }
  }

  onSelectionChange(event) {
    this.selectedRoleMembers[event.value] = [];
    const roleMembers = [];
    const subscription = this.us.getRoleMembers(event.value).subscribe((res: any) => {
      for (const RName of res) {
        if (RName.name !== undefined && RName.empNo === this.user.EmpNo) {
          roleMembers.push({name: RName.name, del:RName.delId, disabled:true,roleId:RName.roleId,empNo:RName.empNo});
        } else {
          roleMembers.push({name: RName.name, del:RName.delId, disabled:false,roleId:RName.roleId,empNo:RName.empNo});
        }
      }
      this.selectedRoleMembers[event.value] = Object.assign([], roleMembers);
    }, err => {
    });
    this.coreService.progress = {busy: subscription, message: '', backdrop: true};
    this.addToSubscriptions(subscription);
  }

  addToSubscriptions(subscription) {
    this.subscription.push(subscription);
  }

  ngOnDestroy() {
    this.clearSubscriptions();
    this.delegatedRoles = [];
    this.delegatedUsers = [];
    this.SelectedUserList = [];
    this.userDelegation = undefined;
    this.roleDelegation = undefined;
    this.roles = [];
    this.criteria = [];
    this.user = undefined;
    this.selectedRoleMembers = [];

  }
}
