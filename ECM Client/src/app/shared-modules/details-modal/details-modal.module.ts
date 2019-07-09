import {NgModule} from '@angular/core';
import {CommonModule} from '@angular/common';
import {
  CalendarModule,
  ConfirmDialogModule, DataTableModule, DialogModule,
  DropdownModule, FileUploadModule, InputTextModule, TabViewModule, TooltipModule
} from 'primeng/primeng';
import {FormsModule, ReactiveFormsModule} from '@angular/forms';
import {BusyModule} from 'angular2-busy';
import {DocDetailsModalComponent} from '../../components/generic-components/doc-details-modal/doc-details-modal.component';

@NgModule({
  declarations: [
    DocDetailsModalComponent
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
    BusyModule, TabViewModule, DataTableModule, TooltipModule
  ],
  providers: [],
  exports: [DocDetailsModalComponent]
})
export class SharedDetailsModalModule {
}
