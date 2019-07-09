import {Component, OnInit, Input, Output, EventEmitter} from '@angular/core';
import {AdminService} from "../../../services/admin.service";
import {Subscription} from "rxjs/Subscription";

@Component({
  selector: 'app-edit-ap-permission',
  templateUrl: './edit-ap-permission.component.html',
  styleUrls: ['./edit-ap-permission.component.css']
})
export class EditApPermissionComponent implements OnInit {

  @Input() public selectedPolicy;
  @Input() public newPermissions;
  @Output() pc = new EventEmitter();
  @Output() removeP = new EventEmitter();
  @Output() addP = new EventEmitter();
  @Output() getGS = new EventEmitter();
  @Output() removeNP = new EventEmitter();
  @Output() addNP = new EventEmitter();
  @Output() accessTC = new EventEmitter();

  granteeTypes = [{label: 'USER', value: 'USER'}, {label: 'GROUP', value: 'GROUP'}];
  public accessLevels = [{label: 'Full Access', value: 'Full Control'},
    {label: 'Author', value: 'Author'},
    {label: 'Viewer', value: 'Viewer'},
    {label: 'Custom', value: 'Custom'}
    ];
  public accessType = [{label: 'Allow', value: 'ALLOW'}, {label: 'Deny', value: 'DENY'}];

  constructor() { }

  ngOnInit() {
  }
  rowStyleMapFn(row, index) {
    if (row.action === 'REMOVE') {
      return 'removed-row';
    }
  }
  accessTypeChanged(permission){
    this.accessTC.emit(permission);
  }
  permissionChanged(permission) {
    this.pc.emit(permission);
  }
  removePermission(permission){
    this.removeP.emit(permission);
  }
  addPermission(permission){
    this.addP.emit(permission);
  }
  getGranteesSuggestion(event, newPermission) {
    this.getGS.emit({event: event, np:newPermission});
  }
  onGranteeTypeChange(permission) {
    permission.granteeName = undefined;
  }
  removeNewPermission(permission) {
    this.removeNP.emit(permission);
  }
  addNewPermission() {
    this.addNP.emit()
  }
}
