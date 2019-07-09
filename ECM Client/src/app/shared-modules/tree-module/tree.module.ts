import {NgModule} from '@angular/core';
import {CommonModule} from '@angular/common';
import {
  ConfirmDialogModule, ContextMenuModule, DialogModule,
  TreeModule, TooltipModule
} from 'primeng/primeng';

import {HttpModule} from '@angular/http';
import {TreeComponent} from '../../components/generic-components/tree/tree.component';
import {BusyModule} from 'angular2-busy';

@NgModule({
  declarations: [
    TreeComponent
  ],
  imports: [
    CommonModule,
    HttpModule,
    ConfirmDialogModule,
    DialogModule, TooltipModule,
    TreeModule,
    BusyModule,
    ContextMenuModule
  ],
  providers: [],
  exports: [TreeComponent]
})
export class SharedTreeModule {
}
