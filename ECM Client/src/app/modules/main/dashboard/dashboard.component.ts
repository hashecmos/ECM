import {Component, OnInit, OnDestroy} from '@angular/core';
// services
import {WorkflowService} from '../../../services/workflow.service';
import {NewsService} from '../../../services/news.service';
import {UserService} from '../../../services/user.service';
import {BreadcrumbService} from "../../../services/breadcrumb.service";
// models
import {User} from '../../../models/user/user.model';
// libraries
import {SelectItem} from 'primeng/primeng';
import {MenuItem} from 'primeng/primeng';
import {Subscription} from 'rxjs/Rx';
import {Router} from '@angular/router';
import * as global from "../../../global.variables";
import {CoreService} from "../../../services/core.service";
@Component({
  templateUrl: './dashboard.component.html'
})
export class DashboardComponent implements OnInit, OnDestroy {

  events: any[];
  username: any;
  private subscription: Subscription[] = [];
  types: SelectItem[];
  dueTypes: SelectItem[];
  dueWorkitemTypes: SelectItem[];
  public newsItems: any;
  public user = new User();
  public currentUser: any = {};
  public selectedTabIndex = 0;
  public busy: Subscription;
  private subscriptions: any[] = [];
  public dashboardStatistics: any[] = [];
  public showDelegationInactiveDialog = false;
  public chartType = 'pie';
  public chartLabels = ['Read', 'Actioned', 'Unread'];
  public ChartColors = [
    {
      backgroundColor: ['#FFC107','#03A9F4','#4CAF50'],
      pointBackgroundColor: ['#FFE082','#81D4FA','#A5D6A7'],
    }
  ];
  public dueChartLabels = ['Pending', 'New'];
  public dueChartColors = [
    {
      backgroundColor: ['#009999','#90A4AE'],
      pointBackgroundColor: ['#80CBC4','#90A4ff'],
    }
  ];
  public chartOptions: any = {
    /*pieceLabel: {
      render: function (args) {
        const value = args.value;
        return value;
      },
      fontSize: 14,
      fontStyle: 'bold',
      fontColor: '#fff',
      fontFamily: '"Lucida Console", Monaco, monospace'
    },*/
      plugins: {
        datalabels: {
          align: 'center',
          anchor: 'center',
          backgroundColor: null,
          borderColor: null,
          borderRadius: 4,
          borderWidth: 1,
          color: '#FFFFFF',
          font: {
            size: 14,
            weight: 800
          },
          offset: 4,
          padding: 0,
          formatter: function(value) {
            return value
          }
        }
      },
     legend: {
      onHover: function(e) {
         e.target.style.cursor = 'pointer';
      }
     },
     hover: {
        onHover: function(e) {
           const point = this.getElementAtEvent(e);
           if (point.length) {
             e.target.style.cursor = 'pointer';
           }
           else {
             e.target.style.cursor = 'default';
           }
        }
     }
  };
  constructor(private breadcrumbService: BreadcrumbService, public workflowService: WorkflowService,
              private ns: NewsService, private us: UserService, private router: Router,
              private coreService: CoreService) {
    breadcrumbService.setItems([
      {label: 'Dashboard'},
      // {label: 'Dashboard', routerLink: ['/']}
    ]);
  }

  ngOnInit() {
    this.username = global.username;
    const subscription = this.us.logIn(this.username, 'def').subscribe(data => {
      localStorage.setItem('user', JSON.stringify(data));
      this.currentUser = this.us.getCurrentUser();
      this.dashboardStatistics[this.currentUser.EmpNo] = {
        today: {
          all: {read: 0, unread: 0, actioned: 0, total: 0, chartData: [0,0,0], active: true},
          to: {read: 0, unread: 0, actioned: 0, total: 0, chartData: [0,0,0], active: false},
          cc: {read: 0, unread: 0, actioned: 0, total: 0, chartData: [0,0,0], active: false},
          selectedType: 'all'
        },
        total: {
          all: {read: 0, unread: 0, actioned: 0, total: 0, chartData: [0,0,0], active: true},
          to: {read: 0, unread: 0, actioned: 0, total: 0, chartData: [0,0,0], active: false},
          cc: {read: 0, unread: 0, actioned: 0, total: 0, chartData: [0,0,0], active: false},
          selectedType: 'all'
        },
        deadline: {
          inbox: {
            dueToday: {read: 0, unread: 0, actioned: 0, total: 0, chartData: [0,0,0], active: true},
            overDue: {read: 0, unread: 0, actioned: 0, total: 0, chartData: [0,0,0], active: false},
            active: true
          }, sent: {
            dueToday: {read: 0, unread: 0, actioned: 0, total: 0, chartData: [0,0,0], active: true},
            overDue: {read: 0, unread: 0, actioned: 0, total: 0, chartData: [0,0,0], active: false},
            active: false
          },
          selectedType: 'dueToday', selectedWorkitemType: 'inbox'
        }
      };
      if (this.currentUser.roles.length > 0) {
        this.currentUser.roles.map((role, index) => {
          this.dashboardStatistics[role.id] = {
            today: {
              all: {read: 0, unread: 0, actioned: 0, total: 0, chartData: [0,0,0], active: true},
              to: {read: 0, unread: 0, actioned: 0, total: 0, chartData: [0,0,0], active: false},
              cc: {read: 0, unread: 0, actioned: 0, total: 0, chartData: [0,0,0], active: false},
              selectedType: 'all'
            },
            total: {
              all: {read: 0, unread: 0, actioned: 0, total: 0, chartData: [0,0,0], active: true},
              to: {read: 0, unread: 0, actioned: 0, total: 0, chartData: [0,0,0], active: false},
              cc: {read: 0, unread: 0, actioned: 0, total: 0, chartData: [0,0,0], active: false},
              selectedType: 'all'
            },
            deadline: {
              inbox: {
                dueToday: {read: 0, unread: 0, actioned: 0, total: 0, chartData: [0,0,0], active: true},
                overDue: {read: 0, unread: 0, actioned: 0, total: 0, chartData: [0,0,0], active: false},
                active: true
              }, sent: {
                dueToday: {read: 0, unread: 0, actioned: 0, total: 0, chartData: [0,0,0], active: true},
                overDue: {read: 0, unread: 0, actioned: 0, total: 0, chartData: [0,0,0], active: false},
                active: false
              },
              selectedType: 'dueToday', selectedWorkitemType: 'inbox'
            }
          };
        })
      }
      if (this.currentUser.delegated.length > 0) {
        this.currentUser.delegated.map((del, index) => {
          this.dashboardStatistics[del.userId] = {
            today: {
              all: {read: 0, unread: 0, actioned: 0, total: 0, chartData: [0,0,0], active: true},
              to: {read: 0, unread: 0, actioned: 0, total: 0, chartData: [0,0,0], active: false},
              cc: {read: 0, unread: 0, actioned: 0, total: 0, chartData: [0,0,0], active: false},
              selectedType: 'all'
            },
            total: {
              all: {read: 0, unread: 0, actioned: 0, total: 0, chartData: [0,0,0], active: true},
              to: {read: 0, unread: 0, actioned: 0, total: 0, chartData: [0,0,0], active: false},
              cc: {read: 0, unread: 0, actioned: 0, total: 0, chartData: [0,0,0], active: false},
              selectedType: 'all'
            },
            deadline: {
              inbox: {
                dueToday: {read: 0, unread: 0, actioned: 0, total: 0, chartData: [0,0,0], active: true},
                overDue: {read: 0, unread: 0, actioned: 0, total: 0, chartData: [0,0,0], active: false},
                active: true
              }, sent: {
                dueToday: {read: 0, unread: 0, actioned: 0, total: 0, chartData: [0,0,0], active: true},
                overDue: {read: 0, unread: 0, actioned: 0, total: 0, chartData: [0,0,0], active: false},
                active: false
              },
              selectedType: 'dueToday', selectedWorkitemType: 'inbox'
            }
          };
        })
      }
      if (this.currentUser.roles.length > 0) {
        //this.tabChange(this.currentUser.roles[0].name, 1);
        this.selectedTabIndex = 1;
      } else {
        this.getUserStat(this.currentUser.EmpNo);
      }
      this.ns.getNews(this.currentUser.EmpNo).subscribe(res => {
        this.newsItems = res;
      });
    }, Error => {
      localStorage.removeItem('user');
      this.router.navigate(['/auth/auth-failure'])
    });
    this.coreService.progress = {busy: subscription, message: '', backdrop: true};
    this.addToSubscriptions(subscription);
    this.coreService.progress = {busy: subscription, message: '', backdrop: false};
    this.types = [{label: 'All', value: 'all'}, {label: 'To', value: 'to'}, {label: 'CC', value: 'cc'}];
    this.dueTypes = [{label: 'Due Today', value: 'dueToday'}, {label: 'OverDue', value: 'overDue'}];
    this.dueWorkitemTypes = [{label: 'Inbox', value: 'inbox'}, {label: 'Sent', value: 'sent'}];
  }

  goToInbox() {
    this.router.navigate(['/workflow/inbox']);
  }
  goToSent() {
    this.router.navigate(['/workflow/sent']);
  }

  changeActiveView(type: any, id ,view, selectedWorkitemType) {
    if(type==='dueToday' || type==='overDue'){
      this.changeDeadlineToInactiveView(id, view);
    } else {
      this.changeToInactiveView(id, view);
    }
    switch (type) {
      case 'all':
        this.dashboardStatistics[id][view].all.active = true;
        break;
      case 'to':
        this.dashboardStatistics[id][view].to.active = true;
        break;
      case 'cc':
        this.dashboardStatistics[id][view].cc.active = true;
        break;
      case 'dueToday':
        this.dashboardStatistics[id][view].inbox.dueToday.active = true;
        this.dashboardStatistics[id][view].sent.dueToday.active = true;
        break;
      case 'overDue':
        this.dashboardStatistics[id][view].inbox.overDue.active = true;
        this.dashboardStatistics[id][view].sent.overDue.active = true;
        break;
    }
  }

  changeToInactiveView(id, view) {
    this.dashboardStatistics[id][view].all.active = false;
    this.dashboardStatistics[id][view].to.active = false;
    this.dashboardStatistics[id][view].cc.active = false;
  }
  changeDeadlineToInactiveView(id, view){
    this.dashboardStatistics[id][view].inbox.dueToday.active = false;
    this.dashboardStatistics[id][view].inbox.overDue.active = false;
    this.dashboardStatistics[id][view].sent.dueToday.active = false;
    this.dashboardStatistics[id][view].sent.overDue.active = false;
  }

  changeActiveWorkitemView(type: any, id){
    this.dashboardStatistics[id].deadline.inbox.active = false;
    this.dashboardStatistics[id].deadline.sent.active = false;
    switch (type) {
      case 'inbox':
        this.dashboardStatistics[id].deadline.inbox.active = true;
        break;
      case 'sent':
        this.dashboardStatistics[id].deadline.sent.active = true;
        break;
    }
  }

  onSelect(event: any, userType: any, userName: any, id: any, day: any, WIType: any, dueWorkitemTypes) {
    let label;
    if (event.active.length > 0) {
      const chart = event.active[0]._chart;
      const activePoints = chart.getElementAtEvent(event.event);
      if ( activePoints.length > 0) {
        const clickedElementIndex = activePoints[0]._index;
        label = chart.data.labels[clickedElementIndex];
      }
    }
    if (WIType !== undefined && userType !== undefined
      && userName !== undefined && id !== undefined
      && day !== undefined && label !== undefined) {
        this.breadcrumbService.fromDashboard = true;
        const dashboardFilter = {
          'filterUserType': ' ', 'filterUserName': ' ',
          'filterUserId': ' ', 'filterWIType': '',
          'filterStatus': '', 'filterReceivedDay': '', 'filterActiveTabIndex': 0
        };
        dashboardFilter.filterUserType = userType;
        dashboardFilter.filterUserName = userName;
        dashboardFilter.filterUserId = id;
        dashboardFilter.filterWIType = WIType;
        dashboardFilter.filterStatus = label;
        dashboardFilter.filterReceivedDay = day;
        dashboardFilter.filterActiveTabIndex = this.selectedTabIndex;
        this.breadcrumbService.dashboardFilterQuery = dashboardFilter;
      if (label === 'Actioned') {
        this.router.navigate(['/workflow/actioned']);
      } else if(dueWorkitemTypes === 'sent'){
        this.goToSent();
      } else {
        this.goToInbox();
      }
    }
  }

  getUserStat(id) {
    this.getWorkitemStatistics(id, 'USER', 'today', 'all');
    this.getWorkitemStatistics(id, 'USER', 'today', 'to');
    this.getWorkitemStatistics(id, 'USER', 'today', 'cc');
    this.getWorkitemStatistics(id, 'USER', 'total', 'all');
    this.getWorkitemStatistics(id, 'USER', 'total', 'to');
    this.getWorkitemStatistics(id, 'USER', 'total', 'cc');
    this.getWorkitemDeadlineStatistics(id, 'USER','inbox','overDue');
    this.getWorkitemDeadlineStatistics(id, 'USER','inbox','dueToday');
    this.getWorkitemDeadlineStatistics(id, 'USER','sent','overDue');
    this.getWorkitemDeadlineStatistics(id, 'USER','sent','dueToday');
  }

  getDelegateStat(id) {
    this.getWorkitemStatistics(id, 'USER', 'today', 'all');
    this.getWorkitemStatistics(id, 'USER', 'today', 'to');
    this.getWorkitemStatistics(id, 'USER', 'today', 'cc');
    this.getWorkitemStatistics(id, 'USER', 'total', 'all');
    this.getWorkitemStatistics(id, 'USER', 'total', 'to');
    this.getWorkitemStatistics(id, 'USER', 'total', 'cc');
    this.getWorkitemDeadlineStatistics(id, 'USER','inbox','overDue');
    this.getWorkitemDeadlineStatistics(id, 'USER','inbox','dueToday');
    this.getWorkitemDeadlineStatistics(id, 'USER','sent','overDue');
    this.getWorkitemDeadlineStatistics(id, 'USER','sent','dueToday');
  }

  getRoleStat(id) {
    this.getWorkitemStatistics(id, 'ROLE', 'today', 'all');
    this.getWorkitemStatistics(id, 'ROLE', 'today', 'to');
    this.getWorkitemStatistics(id, 'ROLE', 'today', 'cc');
    this.getWorkitemStatistics(id, 'ROLE', 'total', 'all');
    this.getWorkitemStatistics(id, 'ROLE', 'total', 'to');
    this.getWorkitemStatistics(id, 'ROLE', 'total', 'cc');
    this.getWorkitemDeadlineStatistics(id, 'ROLE','inbox','overDue');
    this.getWorkitemDeadlineStatistics(id, 'ROLE','inbox','dueToday');
    this.getWorkitemDeadlineStatistics(id, 'ROLE','sent','overDue');
    this.getWorkitemDeadlineStatistics(id, 'ROLE','sent','dueToday');
  }

  getWorkitemStatistics(id, userType, view, itemType) {
    this.subscription.push(this.workflowService.getWorkitemStats(id, userType, view.toUpperCase(), itemType.toUpperCase(), 'inbox')
      .subscribe(data => {
        this.dashboardStatistics[id][view][itemType].unread = data.unread;
        this.dashboardStatistics[id][view][itemType].actioned = data.reply;
        this.dashboardStatistics[id][view][itemType].read = data.read;
        this.dashboardStatistics[id][view][itemType].total = data.total;
        this.dashboardStatistics[id][view][itemType].chartData = [data.read, data.reply, data.unread];
      }));
  }

  getWorkitemDeadlineStatistics(id, userType, workitemType, itemType) {
    this.subscription.push(this.workflowService.getWorkitemStats(id, userType, 'DEADLINE', itemType, workitemType)
      .subscribe(data => {
        this.dashboardStatistics[id].deadline[workitemType][itemType].read = data.read;
        this.dashboardStatistics[id].deadline[workitemType][itemType].unread = data.unread;
        this.dashboardStatistics[id].deadline[workitemType][itemType].total = data.total;
        this.dashboardStatistics[id].deadline[workitemType][itemType].chartData = [data.read, data.unread];
      }));
  }

  tabChange(textLabel, index) {
    this.selectedTabIndex = index;
    this.breadcrumbService.dashboardTabSelected = this.selectedTabIndex + '@' + textLabel;
    let type;
    let id;
    for (const role of this.currentUser.roles) {
      if (textLabel === role.name) {
        type = 'role';
        id = role.id;
      }
    }
    if (type === undefined) {
      for (const del of this.currentUser.delegated) {
        if (textLabel === del.delName) {
          type = 'del';
          id = del.userId;

          this.us.validateDelegation(del.id).subscribe(res=>{
            if(res==='INACTIVE'){
              this.showDelegationInactiveDialog = true;
            }
          });
          this.workflowService.delegateId = del.id;
        }
      }
    }
    if (type === undefined) {
      if (textLabel === this.currentUser.fulName) {
        type = 'user';
        id = this.currentUser.EmpNo;
      }
    }
    if (type === 'role') {
      this.getRoleStat(id);
    } else if (type === 'del') {
      this.getDelegateStat(id);
    } else if (type === 'user') {
      this.getUserStat(id);
    }
  }

  addToSubscriptions(subscription) {
    this.subscriptions.push(subscription);
  }

  clearSubscriptions() {
    this.subscriptions.map(s => {
      s.unsubscribe();
    });
  }

  reloadApp(){
    this.showDelegationInactiveDialog = false;
    window.location.reload(true);
  }

  ngOnDestroy() {
    this.clearSubscriptions();
    for (const subs of this.subscription) {
      subs.unsubscribe();
    }
    this.subscription = null;
    if (this.busy) {
      this.busy.unsubscribe();
    }
    this.showDelegationInactiveDialog = false;
  }
}
