import {Component, OnInit, Input, Output, EventEmitter, OnDestroy} from '@angular/core';
import {BreadcrumbService} from "../../../services/breadcrumb.service";
import {WorkflowService} from '../../../services/workflow.service';
import {UserService} from '../../../services/user.service';
import {Subscription} from 'rxjs/Rx';
import {User} from '../../../models/user/user.model';
import {WorkitemSet} from '../../../models/workflow/workitem-set.model';
import {MultiSelectModule} from 'primeng/primeng';
import {SelectItem} from 'primeng/primeng';
import {style} from '@angular/animations';
import {FormBuilder, FormGroup, Validators, FormControl} from '@angular/forms';
import {Router} from '@angular/router';
import {DocumentService} from "../../../services/document.service";
import {CoreService} from "../../../services/core.service";

@Component({
  templateUrl: './draft.component.html',
})
export class DraftComponent implements OnInit, OnDestroy {
  private subscription: Subscription[] = [];
  public selectedItem: any[] = [];
  public colHeaders: any[] = [];
  public itemsPerPage: any;
  public draftWorkitems: WorkitemSet[] = [];
  public columns: any[];
  public selectedColumns: string[] = [];
  public user = new User();
  public actions: string[] = [];
  public selectedAction: any;
  public disableAction  = true;
  public selectedCount = 0;
  private subscriptions: any[]=[];

  constructor(private breadcrumbService: BreadcrumbService, private ws: WorkflowService, private us: UserService,
              private router: Router, private ds: DocumentService, private coreService:CoreService) {
    this.user = this.us.getCurrentUser();
    this.breadcrumbService.setItems([
      {label: 'Workflow'},
      {label: 'Drafts'},
      {label: this.user.fulName}
    ]);
  }

  ngOnInit() {
    this.us.getUserSettings().subscribe(val => {
      const res:any = val;
      this.assignPagination(res);
    });
    this.getDrafts();
    this.actions = ['View'];
    this.colHeaders = [{field: 'wiAction', header: 'Actions', hidden: true},
      {field: 'draftDate', header: 'Draft Date', hidden: true,sortField:'draftDate2'}
    ];
    this.columns = [];
    this.columns = [{label: 'Actions', value: 'wiAction'},
      {label: 'Draft Date', value: 'draftDate',sortField:'draftDate2'}
    ];
    this.selectedColumns = ['wiAction', 'draftDate'];
    for (const colunm of this.selectedColumns) {
      for (const tableHead of this.colHeaders) {
        if (tableHead.field === colunm) {
          tableHead.hidden = false;
        }
      }
    }

  }

   assignPagination(val) {
    if (val !== undefined) {
      val.map((d, i) => {
        if (d.key === 'Page Size') {
          if(d.val){
            this.itemsPerPage = parseInt(d.val,10);
          }else{
            this.itemsPerPage = 10;
          }

        }
      });

    }
  }

  assignDraftItems(data) {
    data.map((item, index) => {
      item.subject = item.workflow.subject;
       item.draftDate2=this.coreService.convertToTimeInbox(item.draftDate);
    });
    this.draftWorkitems = data;
  }

  getData(data: any) {
    this.selectedItem = data;
    if (this.selectedItem) {
      if (this.selectedItem.length > 0) {
        this.disableAction = false;
        this.selectedCount = this.selectedItem.length;
      } else {
        this.disableAction = true;
        this.selectedCount = 0;
      }
    }
  }

  getSelectedAction(data: any) {
    if (data === 'View') {
      this.router.navigate(['/workflow/launch', 'draftLaunch', {id: this.selectedItem[0].draftId}]);
    }
  }

  columnSelectionChanged(event: Event) {
    console.log(this.selectedColumns);
    for (const tableHead of this.colHeaders) {
      tableHead.hidden = true;
    }
    for (const colunm of this.selectedColumns) {
      for (const tableHead of this.colHeaders) {
        if (tableHead.field === colunm) {
          tableHead.hidden = false;
        }
      }
    }
  }

  actionSelectionChanged(event) {
  }

  getDrafts() {
    const subscription = this.ws.getDrafts(this.user.EmpNo, 'USER').subscribe(
      data => this.assignDraftItems(data), Error => console.log(Error)
    );
    this.coreService.progress={busy:subscription,message:'',backdrop:true};
    this.addToSubscriptions(subscription);

  }

  refreshTable() {
    this.ws.updateDraftsCount();
    this.getDrafts();
  }

  viewDraft(event) {
    this.router.navigate(['/workflow/launch', 'draftLaunch', {id: event.draftId}])
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
    this.subscription = [];
    this.selectedItem = [];
    this.colHeaders = [];
    this.itemsPerPage = undefined;
    this.draftWorkitems = [];
    this.columns = [];
    this.selectedColumns = [];
    this.user = undefined;
    this.actions = [];
    this.selectedAction = undefined;
    this.disableAction = true;
    this.selectedCount = 0;
  }
}
