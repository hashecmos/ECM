import {NgModule} from '@angular/core';
import {CommonModule} from '@angular/common';
import {HttpModule} from '@angular/http';
import {
  ButtonModule, CalendarModule, CheckboxModule, DialogModule, DropdownModule, FileUploadModule,
  InputTextModule, PanelModule, TreeModule
} from 'primeng/primeng';
import {FormsModule, ReactiveFormsModule} from '@angular/forms';
import {SearchDocumentComponent} from '../../components/generic-components/search-document/search.component';
import {BusyModule} from "angular2-busy";

@NgModule({
  declarations: [
    SearchDocumentComponent
  ],
  imports: [
    PanelModule,
    DialogModule,
    TreeModule,
    CommonModule,
    HttpModule,
    DropdownModule,
    FormsModule,
    ReactiveFormsModule,
    FileUploadModule,
    ButtonModule,
    InputTextModule,
    CheckboxModule,
    CalendarModule,BusyModule
  ],
  providers: [],
  exports: [SearchDocumentComponent]
})
export class SharedSearchDocumentModule {

}
