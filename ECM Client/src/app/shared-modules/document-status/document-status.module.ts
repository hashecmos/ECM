import {NgModule} from '@angular/core';
import {CommonModule} from '@angular/common';
import {DocumentStatusComponent} from "../../components/generic-components/document-status/document-status.component";
import {ButtonModule, InputTextareaModule, InputTextModule, PanelModule, TabViewModule} from "primeng/primeng";
import {FormsModule} from "@angular/forms";



@NgModule({
  declarations: [
    DocumentStatusComponent
  ],
  imports: [
    CommonModule,
    ButtonModule,
    InputTextModule,
    FormsModule,
    InputTextareaModule
  ],
  providers: [],
  exports: [DocumentStatusComponent]
})
export class SharedDocumentStatusModule {

}
