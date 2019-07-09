import {NgModule} from '@angular/core';
import {CommonModule} from '@angular/common';
import {
  AutoCompleteModule,
  CalendarModule,
  ConfirmDialogModule, DataTableModule, DialogModule,
  DropdownModule, FileUploadModule, InputTextModule, TabViewModule, TooltipModule
} from 'primeng/primeng';
import {FormsModule, ReactiveFormsModule} from '@angular/forms';
import {BusyModule} from 'angular2-busy';
import {EditApPermissionComponent} from "../../components/generic-components/edit-ap-permission/edit-ap-permission.component";

@NgModule({
  declarations: [
    EditApPermissionComponent
  ],
  imports: [
    CommonModule,
    FormsModule,
    ReactiveFormsModule,
    ConfirmDialogModule,
    DialogModule,
    FileUploadModule, InputTextModule,
    CalendarModule,
    DropdownModule,
    InputTextModule,
    BusyModule, TabViewModule, DataTableModule, TooltipModule, AutoCompleteModule,
  ],
  providers: [],
  exports: [EditApPermissionComponent]
})
export class SharedEditApPermissionModule {
}
