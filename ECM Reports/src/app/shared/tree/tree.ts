import {Component, Input, Output, EventEmitter, ViewChild, OnInit, OnDestroy} from '@angular/core';
import {ReportService} from '../../services/reports.service';
import * as $ from 'jquery';
import {ITreeOptions} from 'angular-tree-component';
import {Subscription} from 'rxjs/Subscription';

@Component({
    selector: 'app-tree',
    templateUrl: 'tree.html',
    styleUrls: ['./tree.css']
})

export class TreeComponent implements OnInit, OnDestroy {
    @Input() testTab;
    @Output() orgCodeUpdated = new EventEmitter();
    nodes: any[];
    public user: any;

    public tempTree: any[] = [];
    @ViewChild('tree') tree;

    options: ITreeOptions = {
        idField: 'uuid',
        nodeHeight: 23,
        useVirtualScroll: true,
        getChildren: this.onNodeExpandGetSubOrgRoles.bind(this)
    }
    subscriptions: Subscription[]= [];
    constructor(public us: ReportService) {
        this.user = JSON.parse(localStorage.getItem('user'));
    }

    ngOnInit() {
        console.log(this.user)
        const subscription = this.us.getUserSupervisorTree(this.user.EmpNo).subscribe(res => this.assignSupervisorTree(res));
        this.addToSubscriptions(subscription);
    }

    assignSupervisorTree(data) {
        data.map((n, i) => {
            if (n.parent === 0) {
                this.tempTree.push({
                    isExpanded: !(this.user.orgCode === n.orgCode),
                    name: n.headRoleName,
                    subTitle: n.headRoleName,
                    id: n.id,
                    orgCode: n.orgCode,
                    enabled: this.user.orgCode === n.orgCode,
                    hasChildren: true
                });
            }
        });
        if (data.length > 1) {
            this.setChildren(this.tempTree[0], data, 1);
        } else {
            this.nodes = this.tempTree;
        }
    }

    setChildren(parent, children, index) {
        let newParent;
        if (!parent.children) {
            parent.children = [];
            parent.children.push({
                isExpanded: true,
                name: children[index].headRoleName,
                subTitle: children[index].headRoleName,
                id: children[index].id,
                orgCode: children[index].orgCode,
                enabled: this.user.orgCode === children[index].orgCode || parent.enabled,
                hasChildren: true
            });
            newParent = parent.children[0];
        } else {
            parent.children.map(c => {
                if (c.data.id === children[index].id) {
                    c.isExpanded = true;
                    newParent = c;
                }
            });
        }

        if (index < children.length - 1) {
            this.setChildren(newParent, children, index + 1);
        } else {
            if (index === children.length - 1) {
                newParent.isExpanded = false;
            }
            this.nodes = this.tempTree;
        }
    }

    onNodeExpandGetSubOrgRoles(parent) {
        let newNodes
        const subscription = this.us.getSubOrgUnits(parent.data.orgCode).subscribe((res: any) => {
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
            setTimeout(() => resolve(newNodes), 500);
        });
    }

    nodeSelected(node) {
        if (!node.enabled) {
            return;
        } else {
            this.orgCodeUpdated.emit(node.orgCode);
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
    ngOnDestroy() {
        this.clearSubscriptions();
        this.subscriptions = [];
        this.nodes = [];
        this.tempTree = [];
    }

}

