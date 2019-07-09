import {Component, Input, Output, EventEmitter, OnInit} from '@angular/core';

@Component({
  selector: 'app-user-list',
  templateUrl: './user-list.component.html'
})
export class UserListComponent {
  @Input() public items: any;
  @Input() public field: string;
  @Input() public clickable: boolean;
  @Input() public actionType: string;
  @Input() public showToBtn = true;
  @Input() public showCcBtn = true;
  @Input() public showAddBtn = true;
  @Input() public showSelectBtn = false;
  @Input() public showTooltip = true;
  @Input() public iconClass = "fa ui-icon-people";
  @Input() public isRowSelectable = false;
  @Output() addToToList = new EventEmitter();
  @Output() addSelectToList = new EventEmitter();
  @Output() addToCCList = new EventEmitter();
  @Output() addToList = new EventEmitter();
  @Output() getRoleMembers = new EventEmitter();
  @Output() getListMembers = new EventEmitter();
  @Output() existsInList = new EventEmitter();

  mAddToToList(item) {
    this.addToToList.emit(item);
  }

  mAddToCCList(item) {
    this.addToCCList.emit(item);
  }

  mAddToList(item) {
    this.addToList.emit(item);
  }

  mGetItemMembers(item) {
    this.getRoleMembers.emit(item);
  }

  mGetListMembers(item) {
    this.getListMembers.emit(item);
  }

  mExistsInList(list) {
    this.existsInList.emit(list);
  }

  mSelectToList(item) {
    this.addSelectToList.emit(item);
  }
}


