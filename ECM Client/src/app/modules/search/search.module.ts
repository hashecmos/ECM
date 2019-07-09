import {NgModule} from '@angular/core';
import {CommonModule} from '@angular/common';
import {SearchComponent} from './search.component';
import {SearchRoute} from './search.routes';
import {SimpleSearchComponent} from './simple-search/simple-search.component';
import {AdvanceSearchComponent} from './advance-search/advance-search.component';
import {SharedSearchDocumentModule} from '../../shared-modules/search-document/search-document.module';
import {SharedDataTableModule} from '../../shared-modules/data-table/data-table.module';
import {WorkflowService} from '../../services/workflow.service';
import {AccordionModule, ButtonModule, DataTableModule, PanelModule, TabViewModule, TreeModule} from 'primeng/primeng';
import {SharedRightPanelModule} from '../../shared-modules/right-panel/right-panel.module';
import {AsideModule} from 'ng2-aside';
import {ContentService} from '../../services/content-service.service';
import {BusyModule} from "angular2-busy";
import {CoreService} from "../../services/core.service";

@NgModule({
  imports: [
    TabViewModule,
    ButtonModule,
    CommonModule,
    SearchRoute,
    SharedSearchDocumentModule,BusyModule,
    SharedDataTableModule, SharedRightPanelModule, AsideModule,AccordionModule,
  ],
  declarations: [
    SearchComponent,
    SimpleSearchComponent,
    AdvanceSearchComponent
  ],
  providers: [
    WorkflowService,
    ContentService
  ]
})
export class SearchModule {
}
