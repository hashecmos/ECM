import {
  Component,
  Renderer,
  OnDestroy,
  OnInit,
  OnChanges,
  SimpleChanges,
  DoCheck,
  Input,
  ViewChild,
  AfterViewChecked
} from '@angular/core';
import {BreadcrumbService} from "../../../services/breadcrumb.service";
// service
import {WorkflowService} from '../../../services/workflow.service';
import {UserService} from '../../../services/user.service';
import {ContentService} from '../../../services/content-service.service';
// models
import {User} from '../../../models/user/user.model';
import {FormBuilder, FormGroup, Validators} from '@angular/forms';
import {ConfirmationService, Message, SelectItem} from 'primeng/primeng';
import {DocumentService} from '../../../services/document.service';
import {Subscription} from 'rxjs/Subscription';
import {Attachment} from '../../../models/document/attachment.model';
import {ActivatedRoute, Router} from '@angular/router';
import {BrowserEvents} from '../../../services/browser-events.service';
import {WorkItemAction} from '../../../models/workflow/workitem-action.model';
import {CoreService} from '../../../services/core.service';
import {Recipients} from '../../../models/user/recipients.model';
import {Role} from '../../../models/user/role.model';
import {GrowlService} from '../../../services/growl.service';
import {Location, LocationStrategy, PathLocationStrategy} from '@angular/common';
import {DocumentCartComponent} from '../../../components/generic-components/document-cart/document-cart.component';
import {DomSanitizer, SafeResourceUrl} from "@angular/platform-browser";

@Component({
  templateUrl: './launch.component.html',
  providers: [Location],
  styleUrls: ['launch.component.scss']
})
export class LaunchComponent implements OnInit, OnDestroy, DoCheck {
  public currentUser: User = new User();
  roleTreeSelection: any;
  roleTreeExpandedIcon = 'ui-icon-people-outline';
  roleTreeCollapsedIcon = 'ui-icon-people';
  @Input() public launch: any = {
    routeParams: {},
    documents: {existing: {}, new: {}, cartItems: []}, recipients: {
      roles: {result: undefined}, list: {}, search: {},
      toList: [], ccList: []
    }, workflow: {model: {}}
  };
  isRelaunch = false;
  msgs: Message[];
  replyRecipients = [];
  subjectDisabled = false;
  public actionType: any;
  public actionTypes: any;
  public wiaAction: any;
  wiaReply = new WorkItemAction();
  wiaForward = new WorkItemAction();
  wiaReplyAll = new WorkItemAction();
  items: any;
  reminderRequired = false;
  public distList = {'id': 1, 'empNo': 1002, 'name': 'Distribution List', lists: []};
  public globalList = {'id': 1, 'empNo': 1002, 'name': 'Global List', lists: []};
  public defaultList = {'id': -1, 'empNo': 1002, 'name': 'Default List'};
  flag = true;
  public isFromDraft = false;
  emitActionType: any = 'Default';
  private searchTemplateNew: any;
  private documentClassesNew: any = [];
  private selectedDocumentClassNew: string;
  private tmpRoleTree = [];
  private searchResult: any;
  private subscriptions: Subscription[] = [];
  private breadCrumbPath: any[] = [];
  private activeIndex = 0;
  public bulkRole: any;
  public isDel: any;
  private colHeaders = [
    {field: 'creator', header: 'Created By'},
    {field: 'addOn', header: 'Added On'},
    {field: 'modOn', header: 'Modified On'},
    {field: 'modifier', header: 'Modified By'}];
  private itemsPerPage = 8;
  private recepientsLoaded: boolean;
  private filteredRoles: any[];
  private actionId: any;
  private draftWorkflow: any;
  showIframe=false;
  private attach_url: SafeResourceUrl;
  public viewer = false;
  constructor(private breadcrumbService: BreadcrumbService, private ws: WorkflowService,private sanitizer: DomSanitizer,
              private us: UserService, private bs: BrowserEvents,
              private contentService: ContentService, private documentService: DocumentService,
              private router: Router, private actroute: ActivatedRoute,
              private coreService: CoreService, private location: Location,
              private growlService: GrowlService, private confirmationService: ConfirmationService) {
    this.breadcrumbService.setItems([
      {label: 'Launch'}
    ]);
    this.init();
  }

  ngDoCheck() {
    this.prepareStepItems();

  }


  init() {
    this.currentUser = this.us.getCurrentUser();
    this.launch.documents.existing = {
      activeAccordionIndices: [0, 1],
      documentClasses: [], searchTemplate: undefined, model: {
        contentSearch: {name: "Content", symName: "CONTENT", dtype: "STRING", mvalues: []},
        actionType: 'Default'
      }, actionTypes: [{label: 'Default', value: 'Default'},{label: 'Bulk Launch', value: 'bulkLaunch'},
        {label: 'Signature', value: 'Signature'}, {label: 'Initial', value: 'Initial'}],
      matchTypes: [{label: 'Exact match', value: 'exact_match'}, {label: 'All of the words', value: 'all_of_the_words'},
        {label: 'Any of the words', value: 'any_of_the_words'}],
    };
    this.launch.documents.cartItems = this.documentService.cartItems;
    this.launch.recipients.roles = {
      selectCriterions: [{label: 'Title', value: 'NAME'},
        {label: 'Org Code', value: 'ORGCODE'}], result: undefined, model: {selectedCriterion: 'NAME'}
    };
    this.launch.recipients.roles.roleTree = [];
    this.launch.recipients.search = {
      searchCriterions: [{label: 'Name', value: 'NAME'}, {label: 'Email', value: 'EMAIL'},
        {label: 'Designation', value: 'TITLE'}, {label: 'Phone', value: 'PHONE'}, {label: 'Org Code', value: 'ORGCODE'},
        {label: 'Koc No', value: 'KOCNO'}], model: {searchCriterion: 'NAME'}
    };
    this.launch.recipients.list = {userList: [], selectedUserList: {}, selectedSublist: {}};
    this.launch.workflow.forOptions = [];
    this.launch.workflow.priorityOptions = [{label: 'Low', value: 1}, {label: 'Normal', value: 2}, {
      label: 'High',
      value: 3
    }];
    this.launch.workflow.model.priority = 2;
    this.launch.launchBtnItems = [];
    this.launch.replyBtnItems = [];
    this.launch.forwardBtnItems = [];
    this.launch.replyAllBtnItems = [];
    this.assignLaunchUserOptions();
    //this.assignActionUserOptions();
    this.launch.currentDate = new Date();
  }
  assignLaunchUserOptions(){
    this.currentUser.roles.map((r, i) => {
      if (i > 0) {
        this.launch.launchBtnItems.push({
          'label': "Launch (On behalf Of " + r.name +")", command: event => {
            this.launchAsRole(r, 'normal');
          }
        });
      }
    });
    this.currentUser.delegated.map((d, i) => {
      this.launch.launchBtnItems.push({
        'label': "Launch (On behalf Of " + d.delName +")", command: event => {
          this.launchAsDelegatedUser(d, 'normal');
        }
      });
    });
    if(this.currentUser.roles.length > 0){
      this.launch.launchBtnItems.push({
        'label': "Launch (As "+ this.currentUser.fulName +")", command: event => {
          this.launchAsCurrentUser('normal', 0, false, '');
        }
      });
    }
  }

  assignBulkLaunchOptions() {
    this.currentUser.roles.map((r, i) => {
      if (i > 0) {
        this.launch.launchBtnItems.push({
          'label': "Bulk launch (On Behalf Of " + r.name +")", command: event => {
            //this.launchAsCurrentUser('bulk', r, false, '');
            this.launchAsRole(r, 'bulk');
          }
        });
      }
    });
    this.currentUser.delegated.map((d, i) => {
      this.launch.launchBtnItems.push({
        'label': "Bulk launch (On Behalf Of " + d.delName +")", command: event => {
          //this.launchAsCurrentUser('bulk', d, false, 'del');
          this.launchAsDelegatedUser(d, 'bulk');
        }
      });
    });
    if(this.currentUser.roles.length > 0) {
      this.launch.launchBtnItems.push({
        'label': "Bulk launch (As " +this.currentUser.fulName+ ")", command: event => {
          this.launchAsCurrentUser('bulk', 0, false, '');
        }
      });
    }
  }

  forwardAsCurrentUser(empNo,userName){
    if(this.currentUser.EmpNo === empNo){
      this.wiaForward.workflow.delEmpNo = 0;
      this.wiaForward.EMPNo = empNo;
      this.wiaForward.workflow.empNo = empNo;
      this.wiaForward.workflow.role = 0;
      this.wiaForward.roleId = 0;
      this.ws.sentSelectedUserTab = 0 + '@' + userName;
      this.setforwardWorkItem();
    } else{
      this.currentUser.delegated.map((del, index)=>{
        if(del.userId===empNo){
          this.forwardAsDelegatedUser(del);
        }
      });
    }
  }
  forwardAsRole(roleId) {
    this.wiaForward.workflow.delEmpNo = 0;
    this.wiaForward.workflow.role = roleId;
    this.wiaForward.roleId = roleId;

    this.currentUser.roles.map((role,index)=>{
      if(role.id === roleId) {
        this.ws.sentSelectedUserTab = (index+1) + '@' + role.name;
      }
    });
    this.setforwardWorkItem();
  }
  forwardAsDelegatedUser(d) {
    this.wiaForward.workflow.delEmpNo = this.currentUser.EmpNo;
    this.wiaForward.EMPNo = d.userId;
    this.wiaForward.workflow.empNo = d.userId;
    this.wiaForward.workflow.role = 0;
    this.wiaForward.roleId = 0;
    this.ws.sentSelectedUserTab = (this.currentUser.delegated.indexOf(d)+1) + '@' + d.delName;
    this.setforwardWorkItem();
  }
  replyAsCurrentUser(empNo,userName){
    if(this.currentUser.EmpNo === empNo){
      this.wiaReply.workflow.delEmpNo = 0;
      this.wiaReply.EMPNo = empNo;
      this.wiaReply.workflow.empNo = empNo;
      this.wiaReply.workflow.role = 0;
      this.wiaReply.roleId = 0;
      this.ws.sentSelectedUserTab = 0 + '@' + userName;
      this.reply();
    } else {
      this.currentUser.delegated.map((del, index)=>{
        if(del.userId===empNo){
          this.replyAsDelegatedUser(del);
        }
      });
    }
  }
  replyAsRole(roleId) {
    this.wiaReply.workflow.delEmpNo = 0;
    this.wiaReply.workflow.role = roleId;
    this.wiaReply.roleId = roleId;

    this.currentUser.roles.map((role,index)=>{
      if(role.id === roleId) {
        this.ws.sentSelectedUserTab = (index+1) + '@' + role.name;
      }
    });
    this.reply();
  }
  replyAsDelegatedUser(d) {
    this.wiaReply.workflow.delEmpNo = this.currentUser.EmpNo;
    this.wiaReply.EMPNo = d.userId;
    this.wiaReply.workflow.empNo = d.userId;
    this.wiaReply.workflow.role = 0;
    this.wiaReply.roleId = 0;
    this.ws.sentSelectedUserTab = (this.currentUser.delegated.indexOf(d)+1) + '@' + d.delName;
    this.reply();
  }
  replyAllAsCurrentUser(empNo,userName){
    if(this.currentUser.EmpNo === empNo){
      this.wiaReplyAll.workflow.delEmpNo = 0;
      this.wiaReplyAll.EMPNo = empNo;
      this.wiaReplyAll.workflow.empNo = empNo;
      this.wiaReplyAll.workflow.role = 0;
      this.wiaReplyAll.roleId = 0;
      this.ws.sentSelectedUserTab = 0 + '@' + userName;
      this.replyAll();
    } else {
      this.currentUser.delegated.map((del, index)=>{
        if(del.userId===empNo){
          this.replyAllAsDelegatedUser(del);
        }
      });
    }
  }
  replyAllAsRole(roleId) {
    this.wiaReplyAll.workflow.delEmpNo = 0;
    this.wiaReplyAll.workflow.role = roleId;
    this.wiaReplyAll.roleId = roleId;

    this.currentUser.roles.map((role,index)=>{
      if(role.id === roleId) {
        this.ws.sentSelectedUserTab = (index+1) + '@' + role.name;
      }
    });
    this.replyAll();
  }
  replyAllAsDelegatedUser(d) {
    this.wiaReplyAll.workflow.delEmpNo = this.currentUser.EmpNo;
    this.wiaReplyAll.EMPNo = d.userId;
    this.wiaReplyAll.workflow.empNo = d.userId;
    this.wiaReplyAll.workflow.role = 0;
    this.wiaReplyAll.roleId = 0;
    this.ws.sentSelectedUserTab = (this.currentUser.delegated.indexOf(d)+1) + '@' + d.delName;
    this.replyAll();
  }

  clickIt() {
    this.flag = !this.flag;
  }

  ngOnInit() {
    this.actroute.paramMap.subscribe(data => {
      const routeParams: any = data;
      this.launch.routeParams = routeParams.params;
      this.prepareStepItems();
      this.getDocumentCart();
      //this.getEntryTemplate();
      this.actroute.params.subscribe(params => {
        routeParams.actionType = params['actionType'];
        this.assignActionType(routeParams);
      })

    })
  }


  onActionTypeChanged(event) {
    if(event.value==='Signature' || event.value==='Initial'){
      this.launch.recipients.ccList = [];
      this.launch.recipients.toList = [];
    }
    this.emitActionType = event.value;
    if(event.value==='bulkLaunch'){
      this.subjectDisabled = true;
    } else if(!(this.isRelaunch || this.actionTypes === 'forward' || this.actionTypes === 'reply' || this.actionTypes === 'replyAll')){
      this.subjectDisabled = false;
    }
  }

  onAccordionTabOpen($event) {
    this.launch.documents.existing.activeAccordionIndices = [];
    this.launch.documents.existing.activeAccordionIndices.push($event.index);
    if ($event.index === 0) {
      this.launch.documents.existing.activeAccordionIndices.push(1);
    }
  }

  onAccordionTabClose($event) {
    const activeIndices = Object.assign([], this.launch.documents.existing.activeAccordionIndices);
    this.launch.documents.existing.activeAccordionIndices.map((activeIndex, i) => {
      if (activeIndex === $event.index) {
        activeIndices.splice(i, 1);
      }
      if ($event.index === 0 && activeIndex === 1) {
        activeIndices.splice(i - 1, 1);
      }
    });
    this.launch.documents.existing.activeAccordionIndices = activeIndices;


  }

  onDocumentSearchCommplete() {
    //  this.launch.documents.existing.activeAccordionIndices.splice();
    this.launch.documents.existing.activeAccordionIndices = [2];
  }

  onRecipientRemoved() {
    setTimeout(() => {
      this.prepareStepItems();
    }, 1000);

  }

  onDocumentRemoved() {
    this.prepareStepItems();
    this.growlService.showGrowl({
      severity: 'info',
      summary: 'Success', detail: 'Document Removed From Cart'
    });
  }

  prepareStepItems() {
    this.items = [{
      label: 'Documents',
      command: (event: any) => {

      }
    },
      {
        label: 'Recipients',
        command: (event: any) => {
          if ((!this.launch.documents.cartItems || this.launch.documents.cartItems.length === 0) &&
            this.actionTypes === 'launch' && !this.isRelaunch) {
            this.growlService.showGrowl({
              severity: 'error',
              summary: 'No Documents', detail: 'No item in document cart'
            });
            this.activeIndex = 0;
          } else if(this.launch.documents.existing.model.actionType === 'bulkLaunch' && !this.isRelaunch &&
            this.actionTypes !== 'draftLaunch' && this.launch.documents.cartItems.length<=1){
            this.growlService.showGrowl({
              severity: 'error',
              summary: 'Warning', detail: 'Need minimum 2 documents in the cart to perform Bulk Launch'
            });
            this.activeIndex = 0;
          } else if(this.launch.documents.existing.model.actionType === 'bulkLaunch' && !this.isRelaunch &&
            this.actionTypes === 'draftLaunch' && (this.launch.documents.cartItems.length + this.launch.workflow.model.attachments.length)<=1){
            this.growlService.showGrowl({
              severity: 'error',
              summary: 'Warning', detail: 'Need minimum 2 documents to perform Bulk Launch'
            });
            this.activeIndex = 0;
          } else {
            this.loadRecepients();
          }

        }
      },
      {
        label: 'Workflow',
        command: (event: any) => {
          if ((this.launch.recipients.toList && this.launch.recipients.ccList &&
              this.launch.recipients.toList.length === 0 && this.launch.recipients.ccList.length === 0) ) {
            this.growlService.showGrowl({
              severity: 'error',
              summary: 'Select Recepients', detail: 'No Recipients selected'
            });
            if((!this.launch.documents.cartItems || this.launch.documents.cartItems.length === 0) && this.actionTypes === 'launch'
             && !this.isRelaunch || this.launch.documents.existing.model.actionType === 'bulkLaunch' && !this.isRelaunch &&
             this.actionTypes !== 'draftLaunch' && this.launch.documents.cartItems.length<=1 ||
             this.launch.documents.existing.model.actionType === 'bulkLaunch' && !this.isRelaunch &&
             this.actionTypes === 'draftLaunch' && (this.launch.documents.cartItems.length + this.launch.workflow.model.attachments.length)<=1){
              this.activeIndex = 0;
            } else {
              this.activeIndex = 1;
              this.loadRecepients();
            }

          } else {
            this.loadWorkflow();
            this.assignDefaultsInWorkflow();
          }

        }
      }
    ];
    if(this.launch.documents.existing.model.actionType === 'bulkLaunch'){
      this.launch.launchBtnItems = [];
      this.assignBulkLaunchOptions();
    } else {
      this.launch.launchBtnItems = [];
      this.assignLaunchUserOptions();
    }
  }

  assignDefaultsInWorkflow() {
    // if(!localStorage.getItem('workflowSubject')) {
      if (this.actionTypes === 'launch' && !this.isRelaunch) {
        if (this.launch.documents.cartItems.length > 0) {
          this.launch.workflow.model.subject = this.launch.documents.cartItems[0].fileName.replace(/\.[^/.]+$/, "");
        }
      }
    //}
    // else{
    //   this.launch.workflow.model.subject=localStorage.getItem('workflowSubject');
    // }

  }
  assignSubjectFromCart(data){
    console.log(data);
    this.launch.workflow.model.subject=data.replace(/\.[^/.]+$/, "");
  }

  assignActionTypes(fn?) {
    this.launch.workflow.forOptions = [];
    if (this.launch.documents.existing.model.actionType.match(/Signature/) !== null) {
      this.launch.workflow.forOptions.push({label: 'Signature', value: 'Signature'});
      this.launch.workflow.model.actions = [this.launch.workflow.forOptions[0].value];
    }
    else if (this.launch.documents.existing.model.actionType.match(/Initial/) !== null) {
      this.launch.workflow.forOptions.push({label: 'Initial', value: 'Initial'});
      this.launch.workflow.model.actions = [this.launch.workflow.forOptions[0].value];
    }
    else {
      const subscription = this.ws.getActions(this.currentUser.EmpNo)
        .subscribe(res => {
          res.map(a => {
            if (a.name !== 'Signature' && a.name !== 'Initial') {
              this.launch.workflow.forOptions.push({label: a.name, value: a.name});
            }

          });
          if( this.actionTypes !== 'draftLaunch' && !this.router.url.includes('reLaunch')){
            this.us.getUserSettings().subscribe(val => {
            const res2: any = val;
            for (const setting of res2) {
              if (setting.key === 'Default Action') {
                if(setting.val){
                  if(setting.val !== ''){
                    this.launch.workflow.model.actions = [setting.val];
                  } else {
                    this.launch.workflow.model.actions = [];
                  }
                } else {
                    this.launch.workflow.model.actions = [];
                }
              }
            }
            });
          } else if(this.actionTypes === 'draftLaunch' && this.draftWorkflow){
            this.launch.workflow.model.actions = this.draftWorkflow.actions.split(',');
          } else if(this.router.url.includes('reLaunch') && this.wiaAction) {
            this.launch.workflow.model.actions = this.wiaAction.actions.split(',');
          }
          if (fn) {
            fn();
          }
        }, err => {
        });
      this.coreService.progress = {busy: subscription, message: '', backdrop: true};
      this.addToSubscriptions(subscription);

    }
  }


  goToNextStep(nextIndex) {
    this.activeIndex = nextIndex;
    if (nextIndex === 1) {
      this.loadRecepients();
    }
    if (nextIndex === 2) {
      this.loadWorkflow();
      this.assignDefaultsInWorkflow();
    }
  }

  loadRecepients() {
    if (this.launch.recipients.roles.roleTree.length === 0) {
      this.getOrgRole(true);
      this.getUserLists();
    }
    this.launch.recipients.roles.result = [];
    this.launch.recipients.search.result = [];


  }

  loadWorkflow() {
    this.initWorkflow();
  }

  initWorkflow() {
    this.assignActionTypes()
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
      })
    }

    if (index < response.length - 1) {
      this.setChildren(newParent, response, index + 1);
    } else {
      this.launch.recipients.roles.roleTree = this.tmpRoleTree;
    }

  }

  getUserSupervisorTree(tmpRoleTree) {
    const subscription = this.us.getUserSupervisorTree(this.currentUser.EmpNo).subscribe(res => {
      if (res.length > 1) {
        this.setChildren(this.tmpRoleTree[0], res, 1);
      }
      else {
        this.launch.recipients.roles.roleTree = tmpRoleTree;
      }
    }, err => {

    });
    this.coreService.progress = {busy: subscription, message: '', backdrop: true};
    this.addToSubscriptions(subscription);


  }

  getOrgRole(init) {
    this.launch.recipients.roles.roleTree = [];
    const subscription = this.us.getTopRolesList().subscribe(res => {
      res.map((head)=>{
        this.tmpRoleTree.push({
          label: head.headRoleName,
          data: head,
          expandedIcon: this.roleTreeExpandedIcon,
          collapsedIcon: this.roleTreeCollapsedIcon,
          leaf: false,
          expanded: false
        });
      });
      //this.getSubOrgRoles(this.tmpRoleTree[0], init);
        this.launch.recipients.roles.roleTree = this.tmpRoleTree;
    }, err => {

    });
    this.coreService.progress = {busy: subscription, message: '', backdrop: true};
    this.addToSubscriptions(subscription);
  }

//favourite 0, default -1, dist=1, rest>1 will be in dist
  getUserLists() {
    const subscription = this.us.getUserLists(true).subscribe(res => {
      const remainings = [];
      res.map((l, i) => {
        if (l.id > 1 && l.isGlobal === 'N') {
          this.distList.lists.push(l);
        } else if (l.id > 1 && l.isGlobal === 'Y') {
          this.globalList.lists.push(l);
        } else {
          remainings.push(l);
        }
      });
      this.launch.recipients.list.userList = remainings;
      this.launch.recipients.list.userList.push(this.defaultList);
      this.launch.recipients.list.userList.push(this.distList);
      this.launch.recipients.list.userList.push(this.globalList);
    }, err => {

    });
    this.coreService.progress = {busy: subscription, message: '', backdrop: true};
    this.addToSubscriptions(subscription);
  }

  getSubOrgRoles(parent, init) {
    const subscription = this.us.getSubRolesList(parent.data.id).subscribe(res => {
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
      if (init) {

        this.getUserSupervisorTree(this.tmpRoleTree);
      }
    }, err => {

    });
    this.coreService.progress = {busy: subscription, message: '', backdrop: true};
    this.addToSubscriptions(subscription);
  }

  searchUsersList() {
    const subscription = this.us.searchUsersList('ROLE', this.launch.recipients.roles.model.searchText, this.launch.recipients.roles.model.selectedCriterion, '')
      .subscribe(res => {
        this.launch.recipients.roles.result = res;
      }, err => {

      });
    this.coreService.progress = {busy: subscription, message: '', backdrop: true};
    this.addToSubscriptions(subscription);
  }

  getListUsers(event, type) {
    let list;
    if (event.value && event.value[0]) {
      list = event.value[0];
    }
    if (!list) {
      list = event;
    }

    if (list.lists) {
      this.launch.recipients.list.subLists = list.lists;
      return;
    } else if (type === 'list') {
      this.launch.recipients.list.subLists = [];
    }
    if (list.users) {
      this.launch.recipients.list.selectedUserList = list;
      return;
    }


    const subscription = this.us.getListUsers(list.id).subscribe(res => {
      list.users = res;
      this.launch.recipients.list.selectedUserList = list;
    }, err => {

    });
    this.coreService.progress = {busy: subscription, message: '', backdrop: true};
    this.addToSubscriptions(subscription);
  }


  getRoleMembers(role) {
    if (!role.members) {
      let RoleNameString = '';
      const subscription = this.us.getRoleMembers(role.id).subscribe(res => {
        for (const RName of res) {
          if (RName.name !== undefined) {
            RoleNameString = RoleNameString + '\n' + '<i class=material-icons style=font-size:.95em;>person</i>' +RName.name;
          }
        }
        role.members = RoleNameString.slice(1);
        console.log( role.members)

      }, err => {

      });
      this.coreService.progress = {busy: subscription, message: '', backdrop: true};
      this.addToSubscriptions(subscription);
    }

  }

  getListUsersForTooltip(list) {
    if (!list.users) {
      let RoleNameString = '';
      const subscription = this.us.getListUsers(list.id).subscribe(res => {
        for (const RName of res) {
          if (RName.name || RName.fulName) {
            RoleNameString = RoleNameString + ',' +  '<i class=material-icons style=font-size:.95em;>person</i>' + RName.name ||  '<i class=material-icons style=font-size:.95em;>person</i>' +RName.fulName;
          }
        }
        list.members = RoleNameString.slice(1);
        console.log( list.members)
      }, err => {

      });
      this.coreService.progress = {busy: subscription, message: '', backdrop: true};
      this.addToSubscriptions(subscription);
    }

  }


  getDocumentCart() {
    const subscription = this.documentService.getCart(this.currentUser.EmpNo).subscribe((data) => {

      this.documentService.refreshCart(data);

      if (this.launch.routeParams.docId) {
        this.launch.documents.cartItems.map(d => {
          d.name = d.fileName;
          if (d.id === this.launch.routeParams.docId) {
            this.populateWorkflowForm(d);
          }
        })
      }
      this.prepareStepItems();
    }, (err) => {
    });
    this.coreService.progress = {busy: subscription, message: '', backdrop: true};
    this.addToSubscriptions(subscription);
  }


  getEntryTemplate() {
    const subscription = this.ws.getEntryTemplates().subscribe(data => {
      data.map((d) => {
        this.documentClassesNew.push({value: d.id, label: d.symName});
      });
      if (data[0]) {
        this.selectedDocumentClassNew = data[0].id;
        this.getEntryTemplateId(data[0].id);
      }

    }, err => {

    });
    this.coreService.progress = {busy: subscription, message: '', backdrop: true};
    this.addToSubscriptions(subscription);
  }

  getEntryTemplateId(id) {
    const subscription = this.ws.getEntryTemplatesId(id).subscribe(data => {
      this.searchTemplateNew = data;
    }, err => {

    });
    this.coreService.progress = {busy: subscription, message: '', backdrop: true};
    this.addToSubscriptions(subscription);
  }


  switchDocumentClassNew() {
    this.getEntryTemplateId(this.selectedDocumentClassNew);
  }

  changeAction(action) {
  }


  removeFromCart(item) {
    const subscription = this.documentService.removeFromCart(this.currentUser.EmpNo, item.id).subscribe((data) => {
      this.launch.documents.cartItems.map((d, i) => {
        if (d.id === item.id) {
          this.launch.documents.cartItems.splice(i, 1);
        }

      });
      this.prepareStepItems();
    }, (err) => {

    });
    this.coreService.progress = {busy: subscription, message: '', backdrop: true};
    this.addToSubscriptions(subscription);
  }


  expandNode(event) {
    this.getSubOrgRoles(event.node, false);
  }


  addToCart(doc) {
    const subscription = this.documentService.getCart(this.currentUser.EmpNo).subscribe(docs => {
      let exists = false;
      if (this.documentService.checkForSameNameDoc(docs, doc.fileName, doc.id)) {
        exists = true;
        this.confirmationService.confirm({
          message: 'Document with same name already exists in Cart, do you want to continue?',
          key: 'addToCartConfirmation',
          accept: () => {
            this.addToCart2(doc);
          }
        });
      }
      if (!exists) {
        this.addToCart2(doc);
      }
    });
    this.coreService.progress = {busy: subscription, message: 'Verifying Document...'};
    this.addToSubscriptions(subscription);
  }

  addToCart2(doc) {
    const subscription = this.documentService.addToCart(this.currentUser.EmpNo, doc.id).subscribe(res => {
      if (res === 'OK') {
        this.growlService.showGrowl({
          severity: 'info',
          summary: 'Success', detail: 'Added To Cart Successfully'
        });
        //localStorage.setItem('workflowSubject',doc.name.replace(/\.[^/.]+$/, ""));

      }
      else {
        this.growlService.showGrowl({
          severity: 'error',
          summary: 'Failure', detail: 'Add To Cart Failed'
        });
      }

      this.getDocumentCart();
    });
    this.coreService.progress = {busy: subscription, message: 'Adding To Cart...'};
    this.addToSubscriptions(subscription);
  }

  downloadDoc(doc) {
    window.location.assign(this.documentService.downloadDocument(doc.id));
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
      this.launch.recipients.toList.push(role);
      this.prepareStepItems();
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
      this.launch.recipients.ccList.push(role);
      this.prepareStepItems();
    }

  }

  addListUsersToToList(list) {
    if (list.users) {
      list.users.map(l => {
        if (!this.existsInList(l)) {
          l.disabled = true;
          this.launch.recipients.toList.push(l);
        }
      });
      this.prepareStepItems();
    } else {
      const subscription = this.us.getListUsers(list.id).subscribe(users => {
        list.users.map(l => {
          if (!this.existsInList(l)) {
            l.disabled = true;
            this.launch.recipients.toList.push(l);
          }
        });
        this.prepareStepItems();
      }, err => {

      });
      this.coreService.progress = {busy: subscription, message: '', backdrop: true};
      this.addToSubscriptions(subscription);
    }


  }

  addListUsersToCCList(list) {
    if (list.users) {
      list.users.map(l => {
        if (!this.existsInList(l)) {
          l.disabled = true;
          this.launch.recipients.ccList.push(l);
        }
      });
      this.prepareStepItems();
    } else {
      const subscription = this.us.getListUsers(list.id).subscribe(users => {
        list.users.map(l => {
          if (!this.existsInList(l)) {
            l.disabled = true;
            this.launch.recipients.ccList.push(l);
          }
        });
        this.prepareStepItems();
      }, err => {

      });
      this.coreService.progress = {busy: subscription, message: '', backdrop: true};
      this.addToSubscriptions(subscription);
    }


  }

  existsInList(role) {
    let exists = false;
    if ((this.launch.documents.existing.model.actionType === 'Signature' || this.launch.documents.existing.model.actionType === 'Initial')
      && this.launch.recipients.toList.length === 1) {
      role.disabled = true;
      if (this.launch.recipients.toList[0].id === role.id) {
        return true;
      } else {
        return false;
      }

    }
    this.launch.recipients.toList.concat(this.launch.recipients.ccList).map(r => {
      if (r.id === role.id) {
        exists = true;
      }
    });
    role.disabled = exists;
    return exists;
  }

  existsInListUserList(role) {
    return false;
  }


  existsInAttachment(attachment, newDoc) {
    let exist = false;
    attachment.map((att, index) => {
      if (att.docId === newDoc.id) {
        exist = true;
      }
    });
    return exist
  }

  searchRoles(event, q) {
    if ((this.launch.documents.existing.model.actionType === 'Signature' || this.launch.documents.existing.model.actionType === 'Initial')
      && this.launch.recipients.toList.length === 1) {
      this.filteredRoles = [];
      return;
    }

    this.filteredRoles = this.launch.recipients.search.result.concat(this.launch.recipients.roles.result)
      .filter(r => r.name.indexOf(event.query) !== -1
        && !this.existsInList(r));
  }

  launchOnBehalfOf() {

  }


  searchUsers() {
    const subscription = this.us.searchUsersList('USER', this.launch.recipients.search.model.searchText, this.launch.recipients.search.model.searchCriterion, '')
      .subscribe(res => {
        this.launch.recipients.search.result = res;
      }, err => {

      });
    this.coreService.progress = {busy: subscription, message: 'Searching...'};
    this.addToSubscriptions(subscription);
  }

  onDocumentAdded(doc) {
    this.addToCart2(doc);
  }


  initWorkflowObj(draft) {
    const workflow: any = {
      EMPNo: undefined, roleId: undefined, workflow: {
        role: undefined, roleId: undefined,
        ECMNo: undefined, empNo: undefined, docDate: 1452364200000, docRecDate: 1452709800000
      }, wiAction: 'LAUNCH', draftId: 0,
      draft: draft, actionDetails: 'New', attachments: [], recipients: []
    };
    workflow.workflow = Object.assign({}, this.launch.workflow.model);
    workflow.actions = workflow.workflow.actions.toString();
    workflow.workflow.actions = undefined;
    /*if(this.launch.documents.cartItems.length>0){
      workflow.workflow.ECMNo = this.launch.documents.cartItems[0].props;
    }*/
    workflow.workflow.role = 0;
    workflow.workflow.projNo = 0;
    workflow.workflow.refNo = 0;
    workflow.workflow.docDate = 1452364200000;
    workflow.workflow.docRecDate = 1452709800000;
    workflow.workflow.contractNo = 0;
    workflow.roleId = 0;
    workflow.priority = 0;
    workflow.wiRemarks = undefined;
    if (!workflow.workflow.deadlineDate) {
      workflow.deadline = null;
    }
    else {
      workflow.deadline = this.coreService.formatDateForLaunch(workflow.workflow.deadlineDate);
    }
    if (workflow.workflow.instructions) {
      workflow.instructions = workflow.workflow.instructions;
      workflow.workflow.instructions = undefined;
    }
    workflow.workflow.deadlineDate = undefined;
    if (!workflow.workflow.reminderDate) {
      workflow.reminder = null;
    }
    else {
      workflow.reminder = this.coreService.formatDateForLaunch(workflow.workflow.reminderDate);
    }
    if (workflow.workflow.wiAction) {
      workflow.wiAction = workflow.workflow.wiAction;
      workflow.workflow.wiAction = undefined;
    }
    if (workflow.workflow.draftId) {
      workflow.draftId = workflow.workflow.draftId;
      workflow.workflow.draftId = undefined;
    }
    if (workflow.workflow.attachments) {
      workflow.attachments = workflow.workflow.attachments;
    }
    workflow.workflow.attachments = undefined;
    workflow.workflow.reminderDate = undefined;
    if (!workflow.workflow.docFrom) {
      workflow.workflow.docFrom = "";
    }
    if (!workflow.workflow.docTo) {
      workflow.workflow.docTo = "";
    }
    if (!workflow.workflow.keywords) {
      workflow.workflow.keywords = "";
    }
    if (!workflow.workflow.remarks) {
      workflow.workflow.remarks = "";
    }
    if (this.currentUser.roles.length > 0 && !draft) {
      workflow.workflow.role = this.currentUser.roles[0].id;
      workflow.roleId = this.currentUser.roles[0].id;
    }
    workflow.EMPNo = this.currentUser.EmpNo;
    workflow.workflow.empNo = this.currentUser.EmpNo;
    workflow.workflow.delEmpNo = 0;
    return workflow;
  }

  launchAsDelegatedUser(delegated, type) {
    const workflow: any = this.initWorkflowObj(false);
    workflow.workflow.delEmpNo = this.currentUser.EmpNo;
    workflow.workflow.role = 0;
    workflow.roleId = 0;
    workflow.EMPNo = delegated.userId;
    workflow.workflow.empNo = delegated.userId;
    this.ws.sentSelectedUserTab = (1+this.currentUser.roles.length+this.currentUser.delegated.indexOf(delegated)) + '@' + delegated.delName;
    this.launchWorkflow(workflow, type, false);

  }

  clearCart(draft) {
    let count = 0;
    if (this.launch.documents.cartItems.length > 0) {
      this.launch.documents.cartItems.map((d, i) => {
        if(d.fromDraft){
          count++;
          if (count === this.launch.documents.cartItems.length) {
            setTimeout(()=>{
              this.launch.documents.cartItems.splice(0, this.launch.documents.cartItems.length);
              if (draft) {
                this.navigateToDraft();
              } else {
                this.navigateToSent();
              }
            },500);
          }
        } else {
          const subscription = this.documentService.removeFromCart(this.currentUser.EmpNo, d.id).subscribe((data) => {
          count++;
          if (count === this.launch.documents.cartItems.length) {
            this.launch.documents.cartItems.splice(0, this.launch.documents.cartItems.length);
            if (draft) {
              this.navigateToDraft();
            } else {
              this.navigateToSent();
            }
          }
          }, (err) => {

          });

          this.coreService.progress = {busy: subscription, message: '', backdrop: true};
          this.addToSubscriptions(subscription);
        }
      });
    }
    else {
      if (draft) {
        this.navigateToDraft();
      } else {
        this.navigateToSent();
      }
    }
  }

  navigateToDraft() {
    this.router.navigate(['/workflow/draft']);
  }

  navigateToSent() {
    this.router.navigate(['/workflow/sent']);
  }

  launchAsRole(role, type) {
    if(this.launch.documents.cartItems.length>0 && this.actionTypes !== 'draftLaunch' && !this.router.url.includes('reLaunch') && type!=='bulk'){
      this.launch.workflow.model.ECMNo = this.launch.documents.cartItems[0].props[0].mvalues[0];
    }
    const workflow = this.initWorkflowObj(false);
    workflow.workflow.role = role.id;
    workflow.roleId = role.id;
    this.ws.sentSelectedUserTab = (1+this.currentUser.roles.indexOf(role)) + '@' + role.name;
    this.launchWorkflow(workflow, type, false);
  }

  launchAsCurrentUser(type, id, draft, flag) {
    if(this.launch.documents.cartItems.length>0 && this.actionTypes !== 'draftLaunch' && !this.router.url.includes('reLaunch') && type!=='bulk'){
      this.launch.workflow.model.ECMNo = this.launch.documents.cartItems[0].props[0].mvalues[0];
    }
    const workflow = this.initWorkflowObj(draft);
    this.bulkRole = id;
    this.isDel = flag;
    if(id===0 && !draft){
      workflow.workflow.role = 0;
      workflow.roleId = 0;
      this.ws.sentSelectedUserTab = 0 + '@' + this.currentUser.fulName;
    } else if(flag !== 'del' && id && !draft){
      this.ws.sentSelectedUserTab = (1+this.currentUser.roles.indexOf(id)) + '@' + id.name
    } else if(flag === 'del' && id && !draft){
      this.ws.sentSelectedUserTab = (1+this.currentUser.roles.length+this.currentUser.delegated.indexOf(id)) + '@' + id.delName
    }
    this.launchWorkflow(workflow, type, draft);
  }

  launchWorkflow(workflow, type, draft) {
    if(this.actionTypes === 'draftLaunch' && this.launch.documents.existing.model.actionType === 'bulkLaunch'){
      workflow.attachments.map((att)=>{
        const attachment = {
          id:att.docId,
          format:att.format,
          fileName:att.docTitle,
          fromDraft: true
        };
        this.launch.documents.cartItems.push(attachment);
      });
    }
    workflow.wiRemarks = this.launch.workflow.model.remarks;
    this.launch.recipients.toList.map(r => {
      let user = new Recipients();
      user.name = r.name;
      user.actionType = r.actionType;
      user.userType = r.userType;
      if (this.actionTypes === 'draftLaunch') {
        user.id = r.id;
      } else {
        if (r.userType === 'USER') {
          user.id = r.EmpNo;
        } else if (r.userType === 'ROLE') {
          user.id = r.id;
        }
      }
      workflow.recipients.push(user);
    });

    this.launch.recipients.ccList.map(r => {
      let user = new Recipients();
      user.name = r.name;
      user.actionType = r.actionType;
      user.userType = r.userType;
      if (this.actionTypes === 'draftLaunch') {
        user.id = r.id;
      } else {
        if (r.userType === 'USER') {
          user.id = r.EmpNo;
        } else if (r.userType === 'ROLE') {
          user.id = r.id;
        }
      }
      workflow.recipients.push(user);
    });
    let count = 0;
    this.launch.documents.cartItems.map((doc, i) => {
      const att = new Attachment();
      att.docId = doc.id;
      att.format = doc.format;
      att.docTitle = doc.fileName;
      if (type !== 'bulk') {
        if (!this.existsInAttachment(workflow.attachments, doc)) {
          workflow.attachments.push(att);
        }
      } else {
        if (this.isDel === 'del') {
          workflow.workflow.delEmpNo = this.currentUser.EmpNo;
          workflow.EMPNo = this.bulkRole.userId;
          workflow.workflow.empNo = this.bulkRole.userId;
          workflow.roleId = this.currentUser.id;
          workflow.workflow.role = this.currentUser.id;
        }
        else {
          workflow.roleId = this.bulkRole.id;
          workflow.workflow.role = this.bulkRole.id;
        }


        if (this.launch.documents.cartItems.length > 1) {
          workflow.workflow.subject = att.docTitle;
          workflow.workflow.ECMNo = doc.props[0].mvalues[0];
          const subscription = this.ws.launchWorkflow(Object.assign({}, workflow, {attachments: [att]}))
            .subscribe(data => {
                count++;
                if (count === this.launch.documents.cartItems.length) {
                  this.growlService.showGrowl({
                      severity: 'info',
                      summary: 'Success', detail: 'Launched Successfully'
                  });
                  this.clearCart(draft);
                }
              },
              error => {
                  this.growlService.showGrowl({
                    severity: 'error',
                    summary: 'Failure', detail: JSON.parse(error.error).responseMessage
                  });
            });
          this.addToSubscriptions(subscription);
          this.coreService.progress = {busy: subscription, message: 'Launching...'};
        }
        else {
          this.growlService.showGrowl({
            severity: 'error',
            summary: 'Bulk Launch Not Allowed', detail: 'Minimum 2 documents  required in cart for bulk launch'
          });
        }
      }
    });

    if (type !== 'bulk') {
      const subscription = (this.ws.launchWorkflow(workflow)
        .subscribe(data => {
          if (draft) {
            this.growlService.showGrowl({
              severity: 'info',
              summary: 'Success', detail: 'Saved Successfully'
            });
          } else {
            this.growlService.showGrowl({
              severity: 'info',
              summary: 'Success', detail: 'Launched Successfully'
            });
          }
          this.clearCart(draft);
        }, error => {
          if (draft) {
            this.growlService.showGrowl({
              severity: 'error',
              summary: 'Failure', detail: JSON.parse(error.error).responseMessage
            });
          } else {
            this.growlService.showGrowl({
              severity: 'error',
              summary: 'Failure', detail: JSON.parse(error.error).responseMessage
            });
          }
        }));
      this.coreService.progress = {busy: subscription, message: 'Launching...'};
      this.addToSubscriptions(subscription);
      setTimeout(() => {
        this.ws.updateDraftsCount();
      }, 3000);

    }

  }

  populateWorkflowForm(item) {
    if (!this.subjectDisabled) {
      const subscription = this.documentService.getDocument(item.id).subscribe(data => this.assignDefaultForm(data));
      this.coreService.progress = {busy: subscription, message: '', backdrop: true};
      this.addToSubscriptions(subscription);
    }
  }
  showDocPreview(item){
    this.showIframe=true;
    const subscription1 = this.documentService.getDocumentInfo(item.id).subscribe(data=>this.assignDocIdForView(data));
    this.coreService.progress = {busy: subscription1, message: '', backdrop: true};
    this.addToSubscriptions(subscription1);
  }
  viewAttachment(item){
    this.showIframe=true;
    const subscription = this.documentService.getDocumentInfo(item.docId).subscribe(data=>this.assignDocIdForView(data));
    this.coreService.progress = {busy: subscription, message: '', backdrop: true};
    this.addToSubscriptions(subscription);
  }
  assignDocIdForView(data){
     this.attach_url = this.transform(this.documentService.getViewUrl(data.id));
     this.viewer = true;
  }
   transform(url) {
    return this.sanitizer.bypassSecurityTrustResourceUrl(url);
  }

  assignDefaultForm(data) {
    data.props.map((p) => {
      if (p.name === 'DocumentTitle') {
        this.launch.workflow.model.subject = p.mvalues[0].replace(/\.[^/.]+$/, "");
      } else if (p.symName === 'DocumentTitle') {
        this.launch.workflow.model.subject = p.mvalues[0].replace(/\.[^/.]+$/, "");
      }
      if (p.name === 'Document To') {
        this.launch.workflow.model.docTo = p.mvalues[0];
      } else if (p.symName === 'DocumentTo') {
        this.launch.workflow.model.docTo = p.mvalues[0];
      }
      if (p.name === 'Document From') {
        this.launch.workflow.model.docFrom = p.mvalues[0];
      } else if (p.symName === 'DocumentFrom') {
        this.launch.workflow.model.docFrom = p.mvalues[0];
      }
      if (p.name === 'Document Tag') {
        this.launch.workflow.model.keywords = p.mvalues[0];
      } else if (p.symName === 'DocumentTag') {
        this.launch.workflow.model.keywords = p.mvalues[0];
      }
    })
  }

  assignActionType(data) {
    this.actionId = data.params.id;
    this.actionTypes = data.params.actionType;
    this.assignActionTypes(() => {
      if (data.params.actionType !== undefined) {
        if (data.params.actionType === 'draftLaunch') {
          const subscription = this.ws.getDrafts(this.currentUser.EmpNo, 'USER').subscribe(
            data2 => this.assignDraft(data2), Error => console.log(Error)
          );
          this.coreService.progress = {busy: subscription, message: '', backdrop: true};
          this.addToSubscriptions(subscription);
        } else if (data.params.actionType === 'browseLaunch') {
          this.loadRecepients();
          this.activeIndex = 1;
        }
        else {
          const subscription = this.ws.getWorkitem(data.params.id, this.currentUser.EmpNo)
            .subscribe(res => this.assignRecepients(res, false));
          this.coreService.progress = {busy: subscription, message: '', backdrop: true};
          this.addToSubscriptions(subscription);
          this.subjectDisabled = true;
          this.loadRecepients();
        }
      }
      else {
        this.actionTypes = 'launch'
      }
    })


  }

  addToSubscriptions(subscription) {
    this.subscriptions.push(subscription);
  }

  assignRecepients(data, fromDraft) {
    this.launch.documents.existing.actionTypes = Object.assign([], [{label: 'Default', value: 'Default'},
        {label: 'Signature', value: 'Signature'}, {label: 'Initial', value: 'Initial'}]);
    this.isFromDraft = fromDraft;
    this.launch.workflow.model.attachments = [];
    this.wiaAction = data;
    this.launch.workflow.model.attachments = Object.assign([], this.wiaAction.attachments);
    this.launch.workflow.model.subject = data.subject.replace(/\.[^/.]+$/, "");
    this.launch.workflow.model.ECMNo = data.ECMNo;
    if (this.actionTypes === 'forward') {
      this.activeIndex = 1;
      this.breadcrumbService.setItems([
        {label: 'Forward'}
      ]);
      this.launch.workflow.model.wiAction = "FORWARD";
      if (fromDraft) {
        //this.launch.workflow.model.subject = data.workflow.subject.substring(0, data.workflow.subject.indexOf('.'));
        this.launch.workflow.model.subject = data.workflow.subject.replace(/\.[^/.]+$/, "");
      }
      this.wiaForward = this.initWorkflowObj(false);

      this.wiaForward.actionDetails = '';
      this.wiaForward.id = this.wiaAction.workitemId;
      this.wiaForward.wiAction = "FORWARD";
      this.wiaForward.attachments = this.wiaAction.attachments;
    }
    else if (this.actionTypes === 'reply') {
      this.activeIndex = 1;
      this.breadcrumbService.setItems([
        {label: 'Reply'}
      ]);
      this.launch.workflow.model.wiAction = "REPLY";
      if (fromDraft) {
        //this.launch.workflow.model.subject = data.workflow.subject.substring(0, data.workflow.subject.indexOf('.'));
        this.launch.workflow.model.subject = data.workflow.subject.replace(/\.[^/.]+$/, "");
      }
      this.wiaReply = this.initWorkflowObj(false);
      this.wiaReply.wiAction = "REPLY";
      this.wiaReply.actionDetails = '';
      this.wiaReply.id = this.wiaAction.workitemId;
      this.wiaReply.attachments = this.wiaAction.attachments;
      if (!fromDraft) {
        const sender = {actionType: 'TO', id: 0, name:'' ,userType:''};
        if(this.wiaAction.senderName){
           sender.id = this.wiaAction.senderEMPNo;
           sender.name = this.wiaAction.senderName;
           sender.userType = 'USER';
        } else if(this.wiaAction.senderRoleName){
           sender.id = this.wiaAction.senderRoleId;
           sender.name = this.wiaAction.senderRoleName;
           sender.userType = 'ROLE';
        }
        this.launch.recipients.toList.push(sender);
      }
    }
    else if (this.actionTypes === 'replyAll') {
      this.activeIndex = 1;
      this.breadcrumbService.setItems([
        {label: 'Reply All'}
      ]);
      this.replyRecipients = this.wiaAction.recipients;
      this.launch.workflow.model.wiAction = "REPLY";
      if (fromDraft) {
        //this.launch.workflow.model.subject = data.workflow.subject.substring(0, data.workflow.subject.indexOf('.'));
        this.launch.workflow.model.subject = data.workflow.subject.replace(/\.[^/.]+$/, "");
      }
      this.wiaReplyAll = this.initWorkflowObj(false);
      this.wiaReplyAll.wiAction = "REPLY";
      this.wiaReplyAll.actionDetails = '';
      this.wiaReplyAll.id = this.wiaAction.workitemId;
      this.wiaReplyAll.attachments = this.wiaAction.attachments;
      if (!fromDraft) {
        this.replyRecipients.map((recipient,index)=>{
          if(recipient.name === this.wiaAction.recipientName || recipient.name === this.wiaAction.recipientRoleName){
            this.replyRecipients.splice(index, 1);
          }
        });
        const sender = {actionType: 'TO', id: 0, name:'' ,userType:''};
        if(this.wiaAction.senderName){
           sender.id = this.wiaAction.senderEMPNo;
           sender.name = this.wiaAction.senderName;
           sender.userType = 'USER';
        } else if(this.wiaAction.senderRoleName){
           sender.id = this.wiaAction.senderRoleId;
           sender.name = this.wiaAction.senderRoleName;
           sender.userType = 'ROLE';
        }
        this.replyRecipients.push(sender);
        this.replyRecipients.map((rec, i) => {
          if (rec.actionType === 'TO' || rec.actionType === 'Reply-TO') {
            rec.actionType = 'TO';
            this.launch.recipients.toList.push(rec);
          }
          else if (rec.actionType === 'CC' || rec.actionType === 'Reply-CC') {
            rec.actionType = 'CC';
            this.launch.recipients.ccList.push(rec);
          }
        });
      }
    }
    else if (this.actionTypes === 'reLaunch') {
      this.activeIndex = 1;
      //console.log(this.wiaAction.subject);
      //this.launch.workflow.model.subject = this.wiaAction.subject.substring(0, this.wiaAction.subject.indexOf('.'));
      this.launch.workflow.model.subject = this.wiaAction.subject.replace(/\.[^/.]+$/, "");
      this.launch.workflow.model.priority = this.wiaAction.priority;
      this.launch.workflow.model.remarks = this.wiaAction.remarks;
      this.launch.workflow.model.instructions = this.wiaAction.instructions;
      this.launch.workflow.model.docFrom = this.wiaAction.docFrom;
      this.launch.workflow.model.docTo = this.wiaAction.docTo;
      if(this.wiaAction.deadline && this.wiaAction.reminder){
        this.launch.workflow.model.deadlineDate = new Date(this.wiaAction.deadline);
        this.launch.workflow.model.reminderDate = new Date(this.wiaAction.reminder)
      }
      this.launch.workflow.model.keywords = this.wiaAction.Keywords;
      this.launch.workflow.model.attachments = [];
      this.launch.workflow.model.attachments = Object.assign([], this.wiaAction.attachments);
      const actions = this.wiaAction.actions.split(',');
      this.launch.workflow.model.actions = Object.assign([],actions);
      if( actions[0] === 'Signature' ||  actions[0] === 'Initial'){
        this.launch.documents.existing.model.actionType = actions[0];
      }
      this.subjectDisabled = true;
      this.actionTypes = 'launch';
      this.isRelaunch = true;
    }
  }

  assignDraft(data) {
    data.map((item, index) => {
      if (this.actionId == item.draftId) {
        this.draftWorkflow = item;
      }
    });
    this.loadRecepients();
    this.activeIndex = 0;
    this.launch.workflow.model.draftId = this.draftWorkflow.draftId;
    //this.launch.workflow.model.subject = this.draftWorkflow.workflow.subject.substring(0, this.draftWorkflow.workflow.subject.indexOf('.'));
    this.launch.workflow.model.subject = this.draftWorkflow.workflow.subject.replace(/\.[^/.]+$/, "");
    this.launch.workflow.model.priority = this.draftWorkflow.workflow.priority;
    this.launch.workflow.model.remarks = this.draftWorkflow.workflow.remarks;
    this.launch.workflow.model.instructions = this.draftWorkflow.instructions;
    this.launch.workflow.model.docFrom = this.draftWorkflow.workflow.docFrom;
    this.launch.workflow.model.docTo = this.draftWorkflow.workflow.docTo;
    if(this.draftWorkflow.deadline && this.draftWorkflow.reminder){
        this.launch.workflow.model.deadlineDate = new Date(this.draftWorkflow.deadline);
        this.launch.workflow.model.reminderDate = new Date(this.draftWorkflow.reminder);
    }
    this.launch.workflow.model.keywords = this.draftWorkflow.workflow.keywords;
    this.launch.workflow.model.ECMNo = this.draftWorkflow.workflow.ECMNo;
    this.draftWorkflow.recipients.map((rec, i) => {
      if (rec.actionType === 'TO') {
        this.launch.recipients.toList.push(rec);
      }
      else if (rec.actionType === 'CC') {
        this.launch.recipients.ccList.push(rec);
      }
    });
    if (this.draftWorkflow.wiAction === 'FORWARD') {
      this.actionTypes = 'forward';
      this.subjectDisabled = true;
      this.assignRecepients(this.draftWorkflow, true);
    } else if (this.draftWorkflow.wiAction === 'REPLY') {
      this.actionTypes = 'reply';
      this.subjectDisabled = true;
      this.assignRecepients(this.draftWorkflow, true);
    } else {
      this.launch.workflow.model.attachments = [];
      this.launch.workflow.model.attachments = Object.assign([], this.draftWorkflow.attachments);
      this.actionTypes = 'draftLaunch';
      const actions = this.draftWorkflow.actions.split(',');
      this.launch.workflow.model.actions = Object.assign([],actions);
      if( actions[0] === 'Signature' ||  actions[0] === 'Initial'){
        this.launch.documents.existing.model.actionType = actions[0];
      }
    }
  }
  setforwardWorkItem() {

    this.wiaForward.priority = this.launch.workflow.model.priority;
    this.wiaForward.actionTaken = this.launch.workflow.model.actionTaken;
    this.wiaForward.wiRemarks = this.launch.workflow.model.remarks;
    this.wiaForward.instructions = this.launch.workflow.model.instructions;
    this.wiaForward.docFrom = this.launch.workflow.model.docFrom;
    this.wiaForward.docTo = this.launch.workflow.model.docTo;
    this.wiaForward.actions = this.launch.workflow.model.actions.toString();
    if (this.launch.workflow.model.deadlineDate) {
      this.wiaForward.deadline = this.coreService.formatDateForLaunch(this.launch.workflow.model.deadlineDate);
    }
    else {
      this.wiaForward.deadline = null;
    }
    if (this.launch.workflow.model.reminderDate) {
      this.wiaForward.reminder = this.coreService.formatDateForLaunch(this.launch.workflow.model.reminderDate);
    }
    else {
      this.wiaForward.reminder = null;
    }

    this.launch.recipients.toList.map(r => {
      this.wiaForward.recipients.push({id: r.id, name: r.name, actionType: r.actionType, userType: r.userType});
    });

    this.launch.recipients.ccList.map(r => {
      this.wiaForward.recipients.push({id: r.id, name: r.name, actionType: r.actionType, userType: r.userType});
    });

    if(this.wiaAction.actions === 'Signature' || this.wiaAction.actions === 'Initial'){
      const subscription = this.ws.forwardWorkflow(this.wiaForward)
        .subscribe(data => this.forwardSuccess(), error => this.forwardfail());
      this.coreService.progress = {busy: subscription, message: 'Forwarding...'};
      this.addToSubscriptions(subscription);
    } else {
      let subscription1 = this.documentService.getCart(this.currentUser.EmpNo).subscribe((items) => {
      if (items.length > 0) {
        items.map((doc, i) => {
          const att = new Attachment();
          att.docId = doc.id;
          att.format = doc.format;
          att.docTitle = doc.fileName;
          if (!this.existsInAttachment(this.wiaForward.attachments, doc)) {
            this.wiaForward.attachments.push(att);
          }
        })
      }
      subscription1 = this.ws.forwardWorkflow(this.wiaForward)
        .subscribe(data => this.forwardSuccess(), error => this.forwardfail());
      this.coreService.progress = {busy: subscription1, message: 'Forwarding...'};
      this.addToSubscriptions(subscription1);
    });
    this.coreService.progress = {busy: subscription1, message: '', backdrop: true};
    this.addToSubscriptions(subscription1);
    }
  }

  reply() {
    this.wiaReply.priority = this.launch.workflow.model.priority;
    this.wiaReply.actionTaken = this.launch.workflow.model.actionTaken;
    this.wiaReply.wiRemarks = this.launch.workflow.model.remarks;
    this.wiaReply.instructions = this.launch.workflow.model.instructions;
    this.wiaReply.docFrom = this.launch.workflow.model.docFrom;
    this.wiaReply.docTo = this.launch.workflow.model.docTo;
    this.wiaReply.actions = this.launch.workflow.model.actions.toString();
    if (this.launch.workflow.model.deadlineDate) {
      this.wiaReply.deadline = this.coreService.formatDateForLaunch(this.launch.workflow.model.deadlineDate);
    }
    else {
      this.wiaReply.deadline = null;
    }
    if (this.launch.workflow.model.reminderDate) {
      this.wiaReply.reminder = this.coreService.formatDateForLaunch(this.launch.workflow.model.reminderDate);
    }
    else {
      this.wiaReply.reminder = null;
    }
    this.launch.recipients.toList.map((to, i) => {
      const toItems = {
        'actionType': to.actionType,
        'id': to.id,
        'name': to.name,
        'userType': to.userType
      };
      if (to.userType === 'USER' && !this.isFromDraft) {
        if (to.EmpNo !== undefined) {
          toItems.id = to.EmpNo;
        }
        else {
          toItems.id = to.id;
        }
      } else if (this.isFromDraft) {
        toItems.id = to.id
      }
      this.wiaReply.recipients.push(toItems)
    });
    this.launch.recipients.ccList.map((cc, i) => {
      const ccItems = {
        'actionType': cc.actionType,
        'id': cc.id,
        'name': cc.name,
        'userType': cc.userType
      };
      if (cc.userType === 'USER' && !this.isFromDraft) {
        if (cc.EmpNo !== undefined) {
          ccItems.id = cc.EmpNo
        }
        else {
          ccItems.id = cc.id;
        }
      } else if (this.isFromDraft) {
        ccItems.id = cc.id
      }
      this.wiaReply.recipients.push(ccItems)
    });
    if(this.wiaAction.actions === 'Signature' || this.wiaAction.actions === 'Initial'){
      const subscription = this.ws.replyWorkflow(this.wiaReply).subscribe(data => this.replysuccess(), error => this.replyfailed());
      this.coreService.progress = {busy: subscription, message: 'Replying...'};
      this.addToSubscriptions(subscription);
    } else {
      let subscription = this.documentService.getCart(this.currentUser.EmpNo).subscribe((items) => {
        if (items.length > 0) {
          items.map((doc, i) => {
            const att = new Attachment();
            att.docId = doc.id;
            att.format = doc.format;
            att.docTitle = doc.fileName;
            if (!this.existsInAttachment(this.wiaReply.attachments, doc)) {
              this.wiaReply.attachments.push(att);
            }
          })
        }

        subscription = this.ws.replyWorkflow(this.wiaReply)
          .subscribe(data => this.replysuccess(), error => this.replyfailed());
        this.coreService.progress = {busy: subscription, message: 'Replying...'};
        this.addToSubscriptions(subscription);
      });
      this.coreService.progress = {busy: subscription, message: 'Replying...'};
      this.addToSubscriptions(subscription);
    }
  }

  replyAll() {
    this.wiaReplyAll.priority = this.launch.workflow.model.priority;
    this.wiaReplyAll.actionTaken = this.launch.workflow.model.actionTaken;
    this.wiaReplyAll.wiRemarks = this.launch.workflow.model.remarks;
    this.wiaReplyAll.instructions = this.launch.workflow.model.instructions;
    this.wiaReplyAll.docFrom = this.launch.workflow.model.docFrom;
    this.wiaReplyAll.docTo = this.launch.workflow.model.docTo;
    this.wiaReplyAll.actions = this.launch.workflow.model.actions.toString();
    if (this.launch.workflow.model.deadlineDate) {
      this.wiaReplyAll.deadline = this.coreService.formatDateForLaunch(this.launch.workflow.model.deadlineDate);
    }
    else {
      this.wiaReplyAll.deadline = null;
    }
    if (this.launch.workflow.model.reminderDate) {
      this.wiaReplyAll.reminder = this.coreService.formatDateForLaunch(this.launch.workflow.model.reminderDate);
    }
    else {
      this.wiaReplyAll.reminder = null;
    }
    this.launch.recipients.toList.map((to, i) => {
      const toItems = {
        'actionType': to.actionType,
        'id': to.id,
        'name': to.name,
        'userType': to.userType
      };
      if (to.userType === 'USER' && !this.isFromDraft) {
        if (to.EmpNo !== undefined) {
          toItems.id = to.EmpNo
        }
        else {
          toItems.id = to.id
        }
      } else if (this.isFromDraft) {
        toItems.id = to.id
      }
      this.wiaReplyAll.recipients.push(toItems)
    });
    this.launch.recipients.ccList.map((cc, i) => {
      const ccItems = {
        'actionType': cc.actionType,
        'id': cc.id,
        'name': cc.name,
        'userType': cc.userType
      };
      if (cc.userType === 'USER' && !this.isFromDraft) {
        if (cc.EmpNo !== undefined) {
          ccItems.id = cc.EmpNo;
        }
        else {
          ccItems.id = cc.id;
        }
      } else if (this.isFromDraft) {
        ccItems.id = cc.id
      }
      this.wiaReplyAll.recipients.push(ccItems)
    });
    let subscription = this.documentService.getCart(this.currentUser.EmpNo).subscribe((items) => {
        if (items.length > 0) {
          items.map((doc, i) => {
            const att = new Attachment();
            att.docId = doc.id;
            att.format = doc.format;
            att.docTitle = doc.fileName;
            if (!this.existsInAttachment(this.wiaReplyAll.attachments, doc)) {
              this.wiaReplyAll.attachments.push(att);
            }
          })
        }
        subscription = this.ws.replyWorkflow(this.wiaReplyAll)
          .subscribe(data => this.replysuccess(), error => this.replyfailed());
        this.coreService.progress = {busy: subscription, message: 'Replying...'};
        this.addToSubscriptions(subscription);
      });
    this.coreService.progress = {busy: subscription, message: 'Replying...'};
    this.addToSubscriptions(subscription);
  }

  replysuccess() {
    this.growlService.showGrowl({
      severity: 'info',
      summary: 'Success', detail: 'Reply Success'
    });
    if(this.wiaAction.actions !== 'Signature' && this.wiaAction.actions !== 'Initial'){
      this.clearCart(false);
    } else {
      this.navigateToSent();
    }
  }

  replyfailed() {
    this.growlService.showGrowl({
      severity: 'error',
      summary: 'Failure', detail: 'Reply Failed'
    });
    this.wiaReplyAll.recipients = [];
    //this.navigateToSent();
  }

  forwardSuccess() {
    this.growlService.showGrowl({
      severity: 'info',
      summary: 'Success', detail: 'Forward Success'
    });
    if(this.wiaAction.actions !== 'Signature' && this.wiaAction.actions !== 'Initial'){
      this.clearCart(false);
    } else {
      this.navigateToSent();
    }
  }

  forwardfail() {
    this.growlService.showGrowl({
      severity: 'error',
      summary: 'Failure', detail: 'Forward Failed'
    });
    this.wiaForward.recipients = [];
  }

  clearSubscriptions() {
    this.subscriptions.map(s => {
      s.unsubscribe();
    });
  }

  goBack() {
    this.location.back();
  }

  selectDeadLine() {
    //this.reminderRequired = true;
    if(this.launch.workflow.model.reminderDate){
      if(this.launch.workflow.model.deadlineDate.getTime() < this.launch.workflow.model.reminderDate.getTime()){
        this.launch.workflow.model.reminderDate = undefined;
      }
    }
  }
  closeViewPopUp() {
    this.showIframe = false;
    this.viewer = false;
  }

  getActionOnBehalfOf(){
    if(this.wiaAction.recipientRoleName){
      return 'On Behalf Of '+this.wiaAction.recipientRoleName;
    } else {
      return 'On Behalf Of '+this.wiaAction.recipientName;
    }
  }

  ngOnDestroy() {
    this.clearSubscriptions();
    this.subjectDisabled = false;
    this.isRelaunch = false;
    this.reminderRequired = false;
    //localStorage.removeItem('workflowSubject');
    this.showIframe=false;
    this.viewer = false;
  }
}
