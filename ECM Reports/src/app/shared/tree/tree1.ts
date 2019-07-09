import {Component, Input, Output, EventEmitter, ViewChild, OnInit, OnDestroy} from '@angular/core';
import {ReportService} from '../../services/reports.service';
import * as $ from 'jquery';
import {ITreeOptions, TreeNode} from 'angular-tree-component';
import {Subscription} from 'rxjs/Subscription';

@Component({
    selector: 'app-tree1',
    templateUrl: 'tree1.html',
    styleUrls: ['./tree.css']
})

export class TreeComponent implements OnInit, OnDestroy {
    @Input() testTab;
    @Output() orgCodeUpdated = new EventEmitter();
    public user: any;
    private subscription: Subscription[] = [];

    options: ITreeOptions = {
        idField: 'uuid',
        nodeHeight: 23,
        useVirtualScroll: true,
        // isExpandedField: 'expanded'
        // getChildren: (node: TreeNode) => {
        //     return this.onNodeExpandGetSubOrgRoles(node);
        // }
         getChildren: this.onNodeExpandGetSubOrgRoles.bind(this)
    };
    @ViewChild('tree') tree;
    roleTree: any[] = [];
    private tmpRoleTree = [];

    constructor(public rs: ReportService) {
        this.user = JSON.parse(localStorage.getItem('user'));
    }

    ngOnInit() {
        console.log(this.user);
        this.getOrgRole(true);
    }
    nodeSelected(node) {
        if (!node.enabled) {
            return;
        } else {
            this.orgCodeUpdated.emit(node.orgCode);
        }
    }
    getOrgRole(init) {
    this.roleTree = [];
    this.rs.getUserSupervisorTree(this.user.EmpNo).subscribe(res => {
        res.map((head, index) => {
            if (head.parent === 0) {
                this.tmpRoleTree.push({
                    name: head.headRoleName,
                    subTitle: head.headRoleName,
                    id: head.id,
                    orgCode: head.orgCode,
                    data: head,
                    enabled: this.user.orgCode === head.orgCode,
                    children: [],
                    isExpanded: true
                });
            }
        });
        this.getSubOrgRoles(this.tmpRoleTree[0], init);
    });
  }


  getSubOrgRoles(parent, init) {
    this.subscription.push(this.rs.getSubOrgUnits(parent.orgCode).subscribe(res => {
      parent.children = [];
      res.map(d => {
        parent.children.push({
            name: d.desc,
            subTitle: d.desc,
            id: d.id,
            orgCode: d.orgCode,
            data: d,
            enabled: this.user.orgCode === d.orgCode || parent.enabled,
            hasChildren: this.user.orgCode === d.orgCode || parent.enabled
        });
      });
      if (init) {
        this.getUserSupervisorTree(this.tmpRoleTree);
      }
    }, err => {

    }));
  }
   getUserSupervisorTree(tmpRoleTree) {
    this.rs.getUserSupervisorTree(this.user.EmpNo).subscribe(res => {
      if (res.length > 1) {
        this.setChildren(this.tmpRoleTree[0], res, 1);
      } else {
        this.roleTree = tmpRoleTree;
      }
    }, err => {

    });
  }

  setChildren(parent, response, index) {
    let newParent;
    if (!parent.children) {
      parent.children = [];
      parent.children.push({
        name: response[index].headRoleName,
        subTitle: response[index].headRoleName,
        id: response[index].id,
        orgCode: response[index].orgCode,
        data: response[index],
        enabled: this.user.orgCode === response[index].orgCode || parent.enabled,
        isExpanded: true,
        hasChildren: this.user.orgCode === response[index].orgCode || parent.enabled
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
      this.roleTree = this.tmpRoleTree;
    }

  }
  onNodeExpandGetSubOrgRoles(parent) {
    console.log(parent.data);
    let newNodes
    const subscription = this.rs.getSubOrgUnits(parent.data.orgCode).subscribe((res: any) => {
         newNodes = res.map((c) => Object.assign({}, {
            name: c.desc,
            subTitle: c.desc,
            id: c.id,
            orgCode: c.orgCode,
            data: c,
            enabled: true,
            hasChildren: true
        }));
    });
    this.addToSubscriptions(subscription);
    return new Promise((resolve, reject) => {
      setTimeout(() => resolve(newNodes), 1000);
    });
  }

  addToSubscriptions(subscription) {
    this.subscription.push(subscription);
  }
  ngOnDestroy() {
    this.roleTree = [];
    this.tmpRoleTree = [];
  }
}

