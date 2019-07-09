import {Component, Input, Output, EventEmitter, OnInit} from '@angular/core';

@Component({
  selector: 'app-user-lists',
  templateUrl: './user-lists.component.html'
})
export class UserListsComponent {
  @Input() public listData: any;

  @Output() onSelect = new EventEmitter();

  mOnSelect(item, type) {
    item.type = type;
    this.onSelect.emit(item)
  }


}


