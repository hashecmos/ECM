import {NgModule} from '@angular/core';
import {CommonModule} from '@angular/common';
import {BrowseRoute} from './browse.routes';
import {FormsModule, ReactiveFormsModule} from '@angular/forms';
import {
  ButtonModule, InputTextModule, DialogModule, PanelModule, TreeModule, ContextMenuModule
} from 'primeng/primeng';
import {BrowseComponent} from './browse.component';
import {SharedDataTableModule} from '../../shared-modules/data-table/data-table.module';
import {AsideModule} from 'ng2-aside';
import {SharedRightPanelModule} from '../../shared-modules/right-panel/right-panel.module';
import {BrowserEvents} from '../../services/browser-events.service';
import {AddDocumentComponent} from './add-document/add-document.component';
import {BrowseDocumentComponent} from './browse-documents/browse-document.component';
import {ConfigurationService} from '../../services/configuration.service';
import {ContentService} from '../../services/content-service.service';
import {WorkflowService} from '../../services/workflow.service';
import {SharedTreeModule} from '../../shared-modules/tree-module/tree.module';
import {SharedAddDocumentModule} from '../../shared-modules/add-document/add-document.module';
import {BusyModule} from 'angular2-busy';
import {FavouriteFoldersComponent} from './favourite-folders/favourite-folders.component';
import {GroupFoldersComponent} from './group-folders/group-folders.component';
import {SplitPaneModule} from "ng2-split-pane/lib/ng2-split-pane";
@NgModule({
  declarations: [
    BrowseComponent,
    AddDocumentComponent,
    BrowseDocumentComponent,
    FavouriteFoldersComponent,
    GroupFoldersComponent
  ],
  imports: [
    CommonModule,
    BrowseRoute,
    FormsModule, ReactiveFormsModule, DialogModule,SplitPaneModule,
    ButtonModule,
    InputTextModule,
    SharedDataTableModule, BusyModule,
    SharedRightPanelModule, SharedTreeModule, PanelModule, TreeModule, ContextMenuModule,
    AsideModule,
    SharedAddDocumentModule
  ],
  providers: [ConfigurationService, WorkflowService, ContentService],

})
export class BrowseModule {
}
