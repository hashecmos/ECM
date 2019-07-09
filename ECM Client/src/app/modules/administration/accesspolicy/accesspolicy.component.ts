import {Component, OnDestroy, OnInit} from '@angular/core';
import {AccessPolicyService} from '../../../services/access-policy.service';
import {AdminService} from '../../../services/admin.service';
import {BreadcrumbService} from '../../../services/breadcrumb.service';
import {GrowlService} from '../../../services/growl.service';
import {CoreService} from '../../../services/core.service';
import {User} from "../../../models/user/user.model";
import {UserService} from "../../../services/user.service";
import {ConfirmationService} from "primeng/primeng";

@Component({
  selector: 'app-accesspolicy',
  templateUrl: './accesspolicy.component.html',
  styleUrls: ['./accesspolicy.component.css']
})
export class AccesspolicyComponent implements OnInit, OnDestroy {
  accessPolicies: any[];
  showPermissionDialogue = false;
  selectedPolicy: any = {};
  colHeaders: any[];
  granteeTypes = [{label: 'USER', value: 'USER'}, {label: 'GROUP', value: 'GROUP'}];
  newPermissions: any[];
  searchUserOrGroup: any;
  viewpolicy = false;
  accessLevelsMap = {
    'Full Control': 983511,
    'Owner': 393687,
    'Author': 131543,
    'Viewer': 131201,
    'Custom': 131201
  };
  accessLevels = [{label: 'Full Access', value: 'Full Control'},
    {label: 'Author', value: 'Author'},
    {label: 'Viewer', value: 'Viewer'},
    {label: 'Custom', value: 'Custom'}];
  permissionRowStyleMap: { [key: string]: string };
  private subscriptions: any[] = [];
  private orgCodes: any[];
  private pageSize: any = 15;
  private allpolicy: any[];
  private tempPermissions: any[];
  private service: (jsonstring: any) => any;
  private disableAddNewPermission = false;
  public user = new User();
  selectedType: string = 'U';
  results: string[];
  isUserSelected = false;

  constructor(private accessPolicyService: AccessPolicyService, private coreService: CoreService, private adminService: AdminService,
              private growlService: GrowlService, private breadcrumbService: BreadcrumbService, private us: UserService,
              private confirmationService: ConfirmationService) {
    this.user = this.us.getCurrentUser();
  }

  refresh() {
    this.getAllPermissions();
  }

  radioButtonClick(e) {
    this.selectedType = e;
  }

  clearSelection() {
    this.searchUserOrGroup = undefined;
    this.results = [];

  }

  searchSelected(e) {
    console.log(e);
    if (e.name) {
      this.isUserSelected = true;
    }

  }

  search(e) {
    if (this.searchUserOrGroup.length > 2) {
      if (this.selectedType === 'U') {
        const subscription = this.adminService.searchLDAPUsers(this.searchUserOrGroup).subscribe(res => {
          console.log(res);
          this.results = res;

        }, err => {
        });
        this.addToSubscriptions(subscription);
        this.coreService.progress = {busy: subscription, message: '', backdrop: false};
      } else {
        const subscription = this.adminService.searchLDAPGroups(this.searchUserOrGroup).subscribe(res => {
          this.results = res;
        }, err => {
        });
        this.addToSubscriptions(subscription);
        this.coreService.progress = {busy: subscription, message: '', backdrop: false};


      }
    }
  }

  ngOnInit() {
    this.us.getUserSettings().subscribe(val => {
      const res: any = val;
      this.assignPagination(res);
    });
    this.getAllPermissions();
    this.breadcrumbService.setItems([
      {label: 'Admin'},
      {label: 'Access Policies'}
    ]);

  }

  assignPagination(val) {
    if (val !== undefined) {
      val.map((d, i) => {
        if (d.key === 'Page Size') {
          if (d.val) {
            this.pageSize = parseInt(d.val, 10);
          } else {
            this.pageSize = 15;
          }
        }
      });
    }
  }

  rowStyleMapFn(row, index) {
    if (row.action === 'REMOVE') {
      return 'removed-row';
    }
  }

  getAllPermissions() {
    const subscription = this.accessPolicyService.getAllAccessPolicies().subscribe(res => {
      this.accessPolicies = res;
      this.colHeaders = [
        {field: 'id', header: 'Id'},
        {field: 'name', header: 'Name'},
        {field: 'orgCode', header: 'Org Code'},
        {field: 'orgName', header: 'Organization'},
        {field: 'createdBy', header: 'Created By'},
        {field: 'createdDate', header: 'Created Date'},
        {field: 'modifiedBy', header: 'Modified By'},
        {field: 'modifiedDate', header: 'Modified Date'},
      ];
    }, err => {

    });
    this.coreService.progress = {busy: subscription, message: '', backdrop: true};
    this.addToSubscriptions(subscription);
  }

  edit(policy) {
    if (policy.isNew) {
      let exists = false;
      this.accessPolicies.map(ap => {
        if (!ap.isNew && ap.name === policy.name) {
          exists = true;
        }
      });
      if (exists) {
        this.growlService.showGrowl({
          severity: 'info',
          summary: 'Success', detail: 'Policy Name Already Exists'
        });
        return;
      }
    }

    this.selectedPolicy = policy;
    this.newPermissions = [];
    this.selectedPolicy.permissions = [];
    if (policy.objectId) {
      const subscription = this.accessPolicyService.getAccessPolicyPermissions(policy.objectId, policy.type).subscribe(res => {
        this.tempPermissions = res;
        res.map((r, i) => {
          r.id = i;
          if (r.inheritDepth === -2 || r.inheritDepth === -3) {
            this.selectedPolicy.permissions.push(Object.assign({}, r));
          }
        });
        this.selectedPolicy.permissions = [...this.selectedPolicy.permissions];
        this.showPermissionDialogue = true;


      }, err => {
      });
      this.addToSubscriptions(subscription);
      this.coreService.progress = {busy: subscription, message: '', backdrop: true};
    } else {
      this.addNewPermission();
      this.showPermissionDialogue = true;

    }

  }

  getOrgCodes(event) {
    const subscription = this.adminService.searchOrgUnits(event.query).subscribe(res => {
      this.orgCodes = res;
    }, err => {

    });
    this.addToSubscriptions(subscription);
  }

  onOrgCodeSelect(selectedOrg, policy) {
    policy.orgName = selectedOrg.desc;
  }

  accessTypeChanged(permission) {
    permission.action = 'ADD';
  }

  permissionChanged(permission) {
    permission.action = 'ADD';
    permission.accessMask = this.accessLevelsMap[permission.accessLevel];
  }

  savePermissions() {
    const newPermissions = [];
    const selectedPolicy = Object.assign({}, this.selectedPolicy);
    selectedPolicy.permissions.map((p, i) => {
      if (p.action === 'ADD') {
        const oldP = Object.assign({}, p);

        oldP.accessLevel = this.tempPermissions[p.id].accessLevel;
        oldP.id = undefined;
        oldP.accessMask = this.accessLevelsMap[oldP.accessLevel];
        oldP.action = 'REMOVE';
        oldP.accessType = 'ALLOW';
        selectedPolicy.permissions.splice(i, 1, oldP);
        newPermissions.push(p);
      }
      p.id = undefined;
    });
    selectedPolicy.permissions = selectedPolicy.permissions.concat(newPermissions);
    if (this.newPermissions) {
      this.newPermissions.map(newPermission => {
        if (newPermission.granteeName) {
          const newPermissionObj: any = {};
          newPermissionObj.accessType = newPermission.accessType;
          newPermissionObj.action = 'ADD';
          newPermissionObj.depthName = '';
          newPermissionObj.inheritDepth = -3;
          newPermissionObj.permissionSource = 'DIRECT';
          newPermissionObj.granteeName = newPermission.granteeName.login;
          newPermissionObj.accessLevel = newPermission.accessLevel;
          newPermissionObj.accessMask = this.accessLevelsMap[newPermission.accessLevel];
          selectedPolicy.permissions.push(newPermissionObj);
        }
      });
    }
    let service: string;
    let successMsg = 'Permission Added Successfully';
    let errorMsg = 'Error In Adding Permission';
    if (selectedPolicy.isNew) {
      selectedPolicy.orgUnitId = selectedPolicy.orgCode.id;
      selectedPolicy.orgCode = undefined;
      selectedPolicy.isNew = undefined;
      selectedPolicy.createdBy = this.user.fulName;
      service = 'addAccessPolicy';
    } else {
      selectedPolicy.modifiedBy = this.user.fulName;
      service = 'setPermissions';
      successMsg = 'Permission Updated Successfully';
      errorMsg = 'Error In Updating Permission';
    }

    const subscription = this.accessPolicyService[service](selectedPolicy).subscribe(res => {
      this.growlService.showGrowl({
        severity: 'info',
        summary: 'Success', detail: successMsg
      });
      this.showPermissionDialogue = false;
      this.disableAddNewPermission = false;
      this.getAllPermissions();


    }, err => {
      this.growlService.showGrowl({
        severity: 'error',
        summary: 'Error', detail: errorMsg
      });
    });

    this.coreService.progress = {busy: subscription, message: '', backdrop: true};
    this.addToSubscriptions(subscription);

  }

  onGranteeTypeChange(permission) {
    permission.granteeName = undefined;
  }

  rowStyle(data, index) {

    if (data.isNew) {
      return 'highlight'
    }
  }

  addNewPermission() {
    if (!this.newPermissions) {
      this.newPermissions = [];
    }
    this.newPermissions = [...this.newPermissions, {
      granteeType: 'USER',
      accessLevel: 'Full Control',
      accessType: 'ALLOW'
    }];
  }

  getGranteesSuggestion(event) {
    if (event.np.granteeType === 'USER') {
      if (event.event.query.length >= 3) {
        const subscription = this.adminService.searchLDAPUsers(event.event.query).subscribe(res => {
          event.np.granteesSuggestion = res;

        }, err => {
        });
        this.addToSubscriptions(subscription);
        this.coreService.progress = {busy: subscription, message: '', backdrop: false};
      }
    } else {
      if (event.event.query.length >= 3) {
        const subscription = this.adminService.searchLDAPGroups(event.event.query).subscribe(res => {
          event.np.granteesSuggestion = res;
        }, err => {
        });
        this.addToSubscriptions(subscription);
        this.coreService.progress = {busy: subscription, message: '', backdrop: false};
      }
    }
  }

  removePolicy(policy) {
    this.accessPolicies.map((p, i) => {
      if (p === policy) {
        this.accessPolicies.splice(i, 1);
        this.accessPolicies = [...this.accessPolicies];
        this.disableAddNewPermission = false;
      }
    });
  }

  addPermission(permission) {
    this.selectedPolicy.permissions.map((p, i) => {
      if (p === permission) {
        p.action = 'READ';
        this.selectedPolicy.permissions = [...this.selectedPolicy.permissions];
      }
    });
  }

  removePermission(permission) {
    this.selectedPolicy.permissions.map((p, i) => {
      if (p === permission) {
        p.action = 'REMOVE';
        this.selectedPolicy.permissions = [...this.selectedPolicy.permissions];
      }
    });
  }

  removeNewPermission(permission) {
    this.newPermissions.map((p, i) => {
      if (p === permission) {
        this.newPermissions.splice(i, 1);
        this.newPermissions = [...this.newPermissions];
      }
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

  addNewPolicy() {
    if (this.accessPolicies.length >= this.pageSize) {
      this.accessPolicies.map((ap, i) => {
        if (ap.isNew) {
          this.accessPolicies.splice(i - 1, 0, ap);
          this.accessPolicies.splice(i + 1, 1);
        }
      });
      this.accessPolicies.splice(this.pageSize - 1, 0, {isNew: true, id: null, objectId: null});
    } else {
      this.accessPolicies.push({isNew: true, id: null, objectId: null})
    }
    this.disableAddNewPermission = true;

    this.accessPolicies = [...this.accessPolicies];
  }

  exportToExcel() {
    let array = [];
    this.colHeaders.map(d => {
      array.push(d.field);
    });
    this.coreService.exportToExcel(this.accessPolicies, 'Access_Policies.xlsx', array)
  }

  viewAccesspolicy(policy) {
    this.viewpolicy = true;
    this.allpolicy = policy;
  }

  confirmDelete(policy) {
    this.confirmationService.confirm({
      header: 'Delete ' + policy.name,
      message: 'Are you sure that you want to delete?',
      accept: () => {
        this.accessPolicyService.removeAccessPolicy(policy.id).subscribe(res => {
          if (res === 'OK') {
            this.growlService.showGrowl({
              severity: 'info',
              summary: 'Success', detail: 'Access Policy Deleted Successfully'
            });
            this.refresh();
          } else if (res === 'Mapping Exists') {
            this.growlService.showGrowl({
              severity: 'error',
              summary: 'Error', detail: 'Mapping Exists Cannot Be Deleted'
            });
          }
        });
      }
    });
  }

  filterAP() {

  }

  ngOnDestroy() {
    this.clearSubscriptions();
    this.viewpolicy = false;
    this.isUserSelected = false;
  }


}
