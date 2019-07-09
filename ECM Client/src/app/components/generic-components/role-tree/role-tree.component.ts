import {Component, Input, Output, EventEmitter} from '@angular/core';

@Component({
  selector: 'app-role-tree',
  templateUrl: './role-tree.component.html'
})
export class RoleTreeComponent {
  @Input() public data: any;
  @Input() public showAddToToBtn = true;
  @Input() public showAddToCCBtn = true;
  @Input() public showAddChildBtn = false;
  @Input() public showRemoveItemBtn = false;
  @Input() public editRole;
  @Input() public actionType: string;
  @Output() nodeSelect = new EventEmitter();
  @Output() nodeUnselect = new EventEmitter();
  @Output() expandNode = new EventEmitter();
  @Output() getRoleMembers = new EventEmitter();
  @Output() addToToList = new EventEmitter();
  @Output() addToCCList = new EventEmitter();
  @Output() addToList = new EventEmitter();
  @Output() removeItem = new EventEmitter();
  @Output() existsInList = new EventEmitter();
  @Output() addChildren = new EventEmitter();
  @Output() selectNode = new EventEmitter();
  @Output() showEditRole = new EventEmitter();
  @Output() showDeleteRole = new EventEmitter();
  mExpandNode(item) {
    this.expandNode.emit(item);
  }

  mSelectNode(item) {
    this.selectNode.emit(item);
  }

  mGetRoleMembers(item) {
    if (item.empNo) {
      return;
    }
    this.getRoleMembers.emit(item);
  }

  mAddToToList(item) {
    this.addToToList.emit(item);
  }

  mAddToCCList(item) {
    this.addToCCList.emit(item);
  }

  mAddToList(item) {
    this.addToList.emit(item);
  }

  mExistsInList(item) {
    this.existsInList.emit(item);
  }

  mRemoveItem(item) {
    this.removeItem.emit(item);
  }

  mAddChildren(item) {
    this.addChildren.emit(item);
  }
  editRoleitem(item) {
    this.showEditRole.emit(item);
  }
  deleteRole(item) {
    this.showDeleteRole.emit(item);
  }
}


