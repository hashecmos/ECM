import {NgModule} from '@angular/core';
import {SettingsComponent} from './settings.component';
import {SettingsRoute} from './settings.routes';
import {
  ButtonModule, CalendarModule, CheckboxModule, DataTableModule, DropdownModule, InputTextModule,
  AccordionModule, ConfirmDialogModule, TabViewModule,
  AutoCompleteModule, SelectButtonModule, DialogModule, TooltipModule,SpinnerModule
} from 'primeng/primeng';
import {FormsModule} from '@angular/forms';
import {DelegationComponent} from './delegation/delegation.component';
import {ListComponent} from './list/list.component';
import {GeneralComponent} from './general/general.component';
import {WorkflowService} from '../../services/workflow.service';
import {CommonModule} from '@angular/common';
import {SharedUserListModule} from '../../shared-modules/user-list/user-list.module';
import {SharedRoleTreeModule} from "../../shared-modules/role-tree/role-tree.module";

@NgModule({
  declarations: [
    SettingsComponent,
    DelegationComponent,
    ListComponent,
    GeneralComponent
  ],
  imports: [
    SettingsRoute, CommonModule,
    DataTableModule, ConfirmDialogModule, TabViewModule,TooltipModule,
    DropdownModule,
    FormsModule,
    ButtonModule,
    InputTextModule,
    CheckboxModule,
    CalendarModule,
    AccordionModule, AutoCompleteModule,SelectButtonModule,DialogModule,SpinnerModule,
    SharedUserListModule,
    SharedRoleTreeModule
  ],
  providers: [WorkflowService],
})
export class SettingsModule {
}
