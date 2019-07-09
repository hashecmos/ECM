import {Component, OnInit, Input, Output, EventEmitter, OnDestroy} from '@angular/core';
import {UserService} from '../../../services/user.service';
import {Subscription} from 'rxjs/Subscription';
import {CoreService} from "../../../services/core.service";
import {GrowlService} from "../../../services/growl.service";

@Component({
  selector: 'app-recipients',
  templateUrl: './recipients.component.html'
})
export class RecipientsComponent implements OnDestroy {
  @Input() public recipientsData: any;
  @Input() public documentsData: any;
  @Input() public actionType: any;
  @Output() prepareStepItems = new EventEmitter();
  private roleTreeSelection: any;
  private filteredRoles: any[];
  private subscriptions: Subscription[] = [];
  roleTreeExpandedIcon = 'ui-icon-people-outline';
  roleTreeCollapsedIcon = 'ui-icon-people';
  public dynamicCriteria: any[] = [];
  public criteria: any[];
  public userSearchQueary = { userName:undefined,mail:undefined,title:undefined,phone:undefined,orgCode:undefined,
                            empNo:undefined,userType:undefined,filter: undefined};
  public roleSearchQueary = { userName:undefined,mail:undefined,title:undefined,phone:undefined,orgCode:undefined,
                            empNo:undefined,userType:undefined,filter: undefined};
  public selectedType = 'USER';
  constructor(private userService: UserService,private coreService:CoreService, private growlService: GrowlService) {
  this.criteria = [{label: 'Name', value: 'userName'}, {label: 'Email', value: 'mail'}, {label: 'Designation', value: 'title'},
      {label: 'Phone', value: 'phone'}, {label: 'Org Code', value: 'orgCode'}, {label: 'KOC No', value: 'empNo'}];
  this.addNewCriterion();
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
          leaf: false
        });
      });
    }, err => {

    });
    this.coreService.progress={busy:subscription,message:'',backdrop:true};
    this.addToSubscriptions(subscription);
  }

  nodeSelect(event) {

  }

  nodeUnselect(event) {

  }

  expandNode(event) {
    this.getSubOrgRoles(event.node);
  }

  addToToList(role) {
    if (!this.existsInList(role)) {
      role.userType = 'ROLE';
      role.actionType = 'TO';
      if (role.fulName) {
        role.name = role.fulName;
        role.userType = 'USER';
      }
      if (role.headRoleName) {
        role.name = role.headRoleName;
        role.userType = 'ROLE';
      }
      role.disabled = true;
      this.recipientsData.toList.push(role);
      this.prepareStepItems.emit();
    }

  }

  addToCCList(role) {
    if (!this.existsInList(role)) {
      role.userType = 'ROLE';
      role.actionType = 'CC';
      if (role.fulName) {
        role.userType = 'USER';
        role.name = role.fulName;
      }
      if (role.headRoleName) {
        role.userType = 'ROLE';
        role.name = role.headRoleName;
      }
      role.disabled = true;
      this.recipientsData.ccList.push(role);
      this.prepareStepItems.emit();
    }

  }
  addToToListFromList(role) {
    if (!this.existsInList(role)) {
      if (role.appRole === 'ROLE') {
        role.userType = 'ROLE';
        role.actionType = 'TO';
        role.name = role.fulName;
      }
      if (role.appRole === 'USER') {
        role.userType = 'USER';
        role.actionType = 'TO';
        role.name = role.fulName;
      }
      role.disabled = true;
      this.recipientsData.toList.push(role);
      this.prepareStepItems.emit();
    }

  }

  addToCCListFromList(role) {
    if (!this.existsInList(role)) {
      if (role.appRole === 'ROLE') {
        role.userType = 'ROLE';
        role.actionType = 'CC';
        role.name = role.fulName;
      }
      if (role.appRole === 'USER') {
        role.userType = 'USER';
        role.actionType = 'CC';
        role.name = role.fulName;
      }
      role.disabled = true;
      this.recipientsData.ccList.push(role);
      this.prepareStepItems.emit();
    }

  }

  existsInListUserList(role) {
    return false;
  }

  searchUsersList() {
    let isSignInit;
    if(this.actionType==='Signature'){
      isSignInit='esign';
    }
    else if(this.actionType==='Initial'){
      isSignInit='initial'
    }
    else {
      isSignInit=this.actionType;
    }
    const subscription = this.userService.searchUsersList('ROLE', this.recipientsData.roles.model.searchText,
      this.recipientsData.roles.model.selectedCriterion,isSignInit)
      .subscribe((res:any) => {
        this.recipientsData.roles.result = res;
        this.recipientsData.roles.model.searchText = '';
      }, err => {

      });
   this.coreService.progress={busy:subscription,message:'',backdrop:true};
    this.addToSubscriptions(subscription);
  }

  getRoleMembers(role) {
    if (!role.members) {
      let RoleNameString = '';
      let roleId;
      if(role.headRoleId){
        roleId=role.headRoleId
      } else if(role.id){
        roleId=role.id
      }
      const subscription = this.userService.getRoleMembers(roleId).subscribe((res:any) => {
        for (const RName of res) {
          if (RName.name !== undefined) {
            RoleNameString = RoleNameString + '\n' +'<i class=material-icons style=font-size:.95em;>person</i>'  + ' '+RName.name;
          }
        }
        role.members = RoleNameString.slice(1);
      }, err => {

      });
    this.addToSubscriptions(subscription);
    }

  }

  getListUsers(event, type) {
    let list;
    if (event.value && event.value[0]) {
      list = event.value[0];
    }
    if (!list) {
      list = event;
    }
    if (list.lists || list.users) {
      if (type === 'sublist') {
        this.recipientsData.list.selectedSublist = list;
      } else {
        this.recipientsData.list.selectedUserList = list;
        this.recipientsData.list.selectedSublist = undefined;
      }
      return;
    }

    const subscription = this.userService.getListUsers(list.id).subscribe((res:any) => {
      list.users = res;
      if (type === 'sublist') {
        this.recipientsData.list.selectedSublist = list;
      } else {
        this.recipientsData.list.selectedUserList = list;
        this.recipientsData.list.selectedSublist = undefined;
      }
    }, err => {

    });
    this.coreService.progress={busy:subscription,message:'',backdrop:true};
    this.addToSubscriptions(subscription);
  }


  searchUsers() {
    let isSignInit;
    if(this.actionType==='Signature'){
      isSignInit='esign';
    }
    else if(this.actionType==='Initial'){
      isSignInit='initial'
    }
    else{
      isSignInit=this.actionType;
    }
    //search with criteria as dropdown
    // const subscription = this.userService.searchUsersList('USER', this.recipientsData.search.model.searchText,
    //   this.recipientsData.search.model.searchCriterion,isSignInit)
    //   .subscribe((res:any) => {
    //     this.recipientsData.search.result = res;
    //     this.recipientsData.search.model.searchText = '';
    //   }, err => {
    //
    //   });
    // this.coreService.progress={busy:subscription,message:'',backdrop:true};
    // this.addToSubscriptions(subscription);

    // dynamic search criteria
    let searchQueary = {'userName':undefined, 'title': undefined, 'mail': undefined,
                          'empNo': undefined, 'orgCode': undefined, 'phone': undefined,
                          'userType': this.selectedType, 'filter': isSignInit};
    // this.dynamicCriteria.map((criteria,index)=>{
    //   if(criteria.searchText !== undefined && criteria.searchText !== '' && criteria.searchText !== null){
    //     searchQueary[criteria.selectedOption] = criteria.searchText;
    //   } else {
    //      formValid = false;
    //      this.growlService.showGrowl({
    //         severity: 'error',
    //         summary: 'Warning', detail: 'Fill All Fields'
    //      });
    //   }
    // });
    if(this.selectedType === 'USER'){
      searchQueary = Object.assign({},this.userSearchQueary);
    } else {
      searchQueary = Object.assign({},this.roleSearchQueary);
    }
    searchQueary.userType = this.selectedType;
    searchQueary.filter = isSignInit;

    let formValid = true;
    if((searchQueary.userName !== undefined && searchQueary.userName !== '' && searchQueary.userName !== null) ||
        (searchQueary.title !== undefined && searchQueary.title !== '' && searchQueary.title !== null) ||
        (searchQueary.mail !== undefined && searchQueary.mail !== '' && searchQueary.mail !== null) ||
        (searchQueary.empNo !== undefined && searchQueary.empNo !== '' && searchQueary.empNo !== null) ||
        (searchQueary.orgCode !== undefined && searchQueary.orgCode !== '' && searchQueary.orgCode !== null) ||
        (searchQueary.phone !== undefined && searchQueary.phone !== '' && searchQueary.phone !== null)){
    } else {
      formValid = false;
      this.growlService.showGrowl({
            severity: 'error',
            summary: 'Warning', detail: 'Fill Any One Field To Search'
      });
    }
    if(formValid){
      const subscription = this.userService.searchEcmUsers(searchQueary).subscribe(data => {
        if(data.length === 0){
            this.growlService.showGrowl({
              severity: 'error',
              summary: 'Failure', detail: 'No Results Found'
            });
        }
        if(this.selectedType === 'ROLE'){
          this.recipientsData.roles.result = data;
        } else {
          this.recipientsData.search.result = data;
        }
      });
      this.coreService.progress = {busy: subscription, message: '', backdrop: true};
      this.addToSubscriptions(subscription)
    }
  }

  searchRoles(event, q) {
    if ((this.documentsData.existing.model.actionType === 'Signature' || this.documentsData.existing.model.actionType === 'Initial')
      && this.recipientsData.toList.length === 1) {
      this.filteredRoles = [];
      return;
    }
    this.filteredRoles = this.recipientsData.search.result.concat(this.recipientsData.roles.result)
      .filter(r => r.name.indexOf(event.query) !== -1
        && !this.existsInList(r));
  }

  existsInList(role) {
    let exists = false;
    if (role.fulName) {
      role.name = role.fulName;
    }
    if (role.headRoleName) {
      role.name = role.headRoleName;
    }

    if ((this.documentsData.existing.model.actionType === 'Signature' || this.documentsData.existing.model.actionType === 'Initial')
        && this.recipientsData.toList.length === 1) {
      role.disabled = true;

      if (role.name === this.recipientsData.toList[0].name && this.recipientsData.toList[0].id === role.id) {
        return true;
      } else {
        return false;
      }

    }
    if (role.EmpNo) {
      role.id = role.EmpNo;
    }
    this.recipientsData.toList.concat(this.recipientsData.ccList).map(r => {
      if (r.name === role.name && r.id === role.id) {
        exists = true;
      }
    });
    role.disabled = exists;
    return exists;
  }

  onRecipientRemoved() {
    setTimeout(() => {
      this.prepareStepItems.emit();
    }, 1000);

  }

  addListUsersToToList(list) {
    if (list.users) {
      list.users.map(l => {
        if (!this.existsInList(l)) {
          if(l.appRole === 'ROLE'){
             l.userType = 'ROLE';
          } else if(l.appRole === 'USER'){
             l.userType = 'USER';
          }
          l.actionType = 'TO';
          l.disabled = true;
          this.recipientsData.toList.push(l);
        }
      });
      this.prepareStepItems.emit();
    } else {
      const subscription = this.userService.getListUsers(list.id).subscribe((users:any) => {
        users.map(l => {
          if (!this.existsInList(l)) {
            if(l.appRole === 'ROLE'){
               l.userType = 'ROLE';
            } else if(l.appRole === 'USER'){
               l.userType = 'USER';
            }
            l.actionType = 'TO';
            l.disabled = true;
            this.recipientsData.toList.push(l);
          }
        });
        list.users = users;
        this.prepareStepItems.emit();
      }, err => {

      });
      this.coreService.progress={busy:subscription,message:'',backdrop:true};
    this.addToSubscriptions(subscription);
    }


  }

  addListUsersToCCList(list) {
    if (list.users) {
      list.users.map(l => {
        if (!this.existsInList(l)) {
          if(l.appRole === 'ROLE'){
             l.userType = 'ROLE';
          } else if(l.appRole === 'USER'){
             l.userType = 'USER';
          }
          l.actionType = 'CC';
          l.disabled = true;
          this.recipientsData.ccList.push(l);
        }
      });
      this.prepareStepItems.emit();
    } else {
      const subscription = this.userService.getListUsers(list.id).subscribe((users:any) => {
        users.map(l => {
          if (!this.existsInList(l)) {
            if(l.appRole === 'ROLE'){
               l.userType = 'ROLE';
            } else if(l.appRole === 'USER'){
               l.userType = 'USER';
            }
            l.actionType = 'CC';
            l.disabled = true;
            this.recipientsData.ccList.push(l);
          }
        });
        list.users = users;
        this.prepareStepItems.emit();
      }, err => {

      });
      this.coreService.progress={busy:subscription,message:'',backdrop:true};
    this.addToSubscriptions(subscription);
    }


  }

  getListUsersForTooltip(list) {
    if (!list.users) {
      let RoleNameString = '';
      const subscription = this.userService.getListUsers(list.id).subscribe((res:any) => {
        for (const RName of res) {
          if (RName.name || RName.fulName) {
            RoleNameString = RoleNameString + '\n' + '<i class=material-icons style=font-size:.95em;>person</i>' +' '+RName.name || '<i class=material-icons style=font-size:.95em;>person</i>' +' '+ RName.fulName;
          }
        }
        list.members = RoleNameString.slice(1);
      }, err => {

      });
      this.coreService.progress={busy:subscription,message:'',backdrop:true};
    this.addToSubscriptions(subscription);
    }

  }

  addNewCriterion() {
    const criterionArr = [];
    this.criteria.map((criterion) => {
      if ((!criterion.selected)) {
        criterionArr.push({
          label: criterion.label, value: criterion.value
        })
      }
    });
    if (!criterionArr[criterionArr.length - 1]) {
      return;
    }
    this.dynamicCriteria.push({options: criterionArr, selectedOption: criterionArr[0].value, searchText: undefined});
    this.criteria.map((cr, k) => {
      if (cr.value === criterionArr[0].value) {
        cr.selected = true;
      }
    });
    this.updateCriteriaOptions();
  }


  updateCriteriaOptions() {
    this.dynamicCriteria.map((dynamicCrite, i) => {
      dynamicCrite.options.map((option, j) => {
        this.criteria.map((cr, k) => {
          if (!cr.selected) {
            if (dynamicCrite.options.map(opt => opt.value).indexOf(cr.value) === -1) {
              dynamicCrite.options.push({
                label: cr.label, value: cr.value
              });
            }
          } else if (cr.value !== dynamicCrite.selectedOption) {
              if (dynamicCrite.options.map(opt => opt.value).indexOf(cr.value) !== -1) {
                dynamicCrite.options.splice(dynamicCrite.options.map(opt => opt.value).indexOf(cr.value), 1);
              }
          }
        });
      });
    });
  }

  criteriaChanged(index) {
    this.dynamicCriteria[index].searchText = undefined;
    this.setSelection();
    this.updateCriteriaOptions();
  }

  setSelection() {
    this.criteria.map((cr, k) => {
      if (this.dynamicCriteria.map(Criterion => Criterion.selectedOption).indexOf(cr.value) === -1) {
        cr.selected = false;
      } else {
        cr.selected = true;
      }
    });
  }

  removeCriteria(index) {
    this.dynamicCriteria.splice(index, 1);
    this.setSelection();
    this.updateCriteriaOptions();
  }
  tabChange(textLabel){
    if(textLabel=== 'Roles'){
      this.criteria = [{label: 'Name', value: 'userName'}, {label: 'Org Code', value: 'orgCode'}];
      this.selectedType = 'ROLE';
    } else if(textLabel=== 'Search'){
      this.criteria = [{label: 'Name', value: 'userName'}, {label: 'Email', value: 'mail'}, {label: 'Designation', value: 'title'},
      {label: 'Phone', value: 'phone'}, {label: 'Org Code', value: 'orgCode'}, {label: 'KOC No', value: 'empNo'}];
      this.selectedType = 'USER';
    }
    this.dynamicCriteria = [];
    this.addNewCriterion();
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
    localStorage.setItem('split-pane',null);
  }

}
