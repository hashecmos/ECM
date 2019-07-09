import {NgModule} from '@angular/core';
import {CommonModule} from '@angular/common';
import {HttpModule} from '@angular/http';
import {AddDocumentComponent} from '../../components/generic-components/add-document/add-document.component';
import {
  ButtonModule, CalendarModule, CheckboxModule, DialogModule, DropdownModule, FileUploadModule,
  InputTextModule, TreeModule,
} from 'primeng/primeng';
import {FormsModule, ReactiveFormsModule} from '@angular/forms';
import {SharedTreeModule} from '../tree-module/tree.module';
import {AdminService} from "../../services/admin.service";


@NgModule({
  declarations: [
    AddDocumentComponent
  ],
  imports: [
    CommonModule,
    HttpModule,
    DropdownModule,
    FormsModule,
    ReactiveFormsModule,
    FileUploadModule, DialogModule,
    ButtonModule, SharedTreeModule,
    InputTextModule,
    CheckboxModule,
    CalendarModule,
    TreeModule
  ],
  providers: [AdminService],
  exports: [AddDocumentComponent]
})
export class SharedAddDocumentModule {

}
