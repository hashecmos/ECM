import {NgModule} from '@angular/core';
import {DocumentCartComponent} from '../../components/generic-components/document-cart/document-cart.component';
import {CommonModule} from '@angular/common';
import {TooltipModule} from "primeng/primeng";
import {SharedHTMLPipeModule} from "../safe-html-pipe/safe-html-pipe";

@NgModule({
  declarations: [
    DocumentCartComponent
  ],
  imports: [
    CommonModule,
    TooltipModule,
    SharedHTMLPipeModule
  ],
  providers: [],
  exports: [DocumentCartComponent]
})
export class SharedDocumentCartModule {

}
