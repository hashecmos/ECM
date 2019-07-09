import {NgModule} from '@angular/core';
import {CommonModule} from '@angular/common';
import {
  CalendarModule,
  ConfirmDialogModule, DialogModule,
  DropdownModule, FileUploadModule, InputTextModule, TreeModule

} from 'primeng/primeng';
import {HttpModule} from '@angular/http';
import {FormsModule, ReactiveFormsModule} from '@angular/forms';
import {RightpanelComponent} from '../../components/generic-components/rightpanel/rightpanel.component';
import {SharedTreeModule} from '../tree-module/tree.module';
import {AccessPolicyService} from '../../services/access-policy.service';
import {BusyModule} from 'angular2-busy';
import {SharedEditApPermissionModule} from "../edit-ap-permission/edit-ap-permission.module";
import {AdminService} from "../../services/admin.service";

@NgModule({
  declarations: [
    RightpanelComponent
  ],
  imports: [
    CommonModule,
    FormsModule,
    ReactiveFormsModule,
    HttpModule,
    ConfirmDialogModule,
    DialogModule,
    FileUploadModule, InputTextModule,TreeModule,
    CalendarModule,
    DropdownModule,
    SharedTreeModule,
    InputTextModule,
    BusyModule,SharedEditApPermissionModule
  ],
  providers: [AccessPolicyService,AdminService],
  exports: [RightpanelComponent]
})
export class SharedRightPanelModule {
}
