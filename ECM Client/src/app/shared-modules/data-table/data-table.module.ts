import {NgModule} from '@angular/core';
import {CommonModule} from '@angular/common';
import {
  ButtonModule, CheckboxModule,
  ConfirmDialogModule,
  DataTableModule, DialogModule, TooltipModule,
  DropdownModule,
  InputTextModule,
  TabViewModule,
} from 'primeng/primeng';
import {DataTableComponent} from '../../components/generic-components/datatable/datatable.component';
import {AsideModule} from 'ng2-aside';
import {HttpModule} from '@angular/http';
import {FormsModule} from '@angular/forms';
import {SharedDetailsModalModule} from '../details-modal/details-modal.module';
import {BusyModule} from "angular2-busy";
import {SharedHTMLPipeModule} from "../safe-html-pipe/safe-html-pipe";

@NgModule({
  declarations: [
    DataTableComponent
  ],
  imports: [
    CommonModule,
    FormsModule,
    HttpModule,
    ButtonModule,
    CheckboxModule,
    DataTableModule, TooltipModule,
    DropdownModule,
    InputTextModule,
    AsideModule,
    TabViewModule,
    DialogModule,BusyModule,
    ConfirmDialogModule, SharedDetailsModalModule, SharedHTMLPipeModule
  ],
  providers: [],
  exports: [DataTableComponent]
})
export class SharedDataTableModule {
}
