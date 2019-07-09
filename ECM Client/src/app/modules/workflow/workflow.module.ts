import {NgModule} from '@angular/core';
import {FormsModule, ReactiveFormsModule} from '@angular/forms';
import {CommonModule} from '@angular/common';
import {WorkflowRoute} from './workflow.routes';
import {
  AccordionModule, AutoCompleteModule,
  ButtonModule, CalendarModule,
  ConfirmDialogModule, DataTableModule, DialogModule,
  DropdownModule, FileUploadModule,
  InputTextareaModule, InputTextModule, MenubarModule, MultiSelectModule, OrderListModule,
  OverlayPanelModule, PanelModule, SelectButtonModule, SplitButtonModule, StepsModule, TabViewModule,
  TooltipModule
} from 'primeng/primeng';
import {HttpModule} from '@angular/http';
import {AsideModule} from 'ng2-aside';
import {WorkflowComponent} from './workflow.component';
import {DraftComponent} from './drafts/draft.component';
import {LaunchComponent} from './launch/launch.component';
import {SentComponent} from './sent/sent.component';
import {InboxComponent} from './inbox/inbox.component';
import {TaskDetailComponent} from './task-detail/task-detail.component';
import {ActionButtonComponent} from '../../components/generic-components/action-button/action-button.component';
import {FilterComponent} from '../../components/generic-components/filter/filter.component';
import {SharedDataTableModule} from '../../shared-modules/data-table/data-table.module';
import {AdminService} from '../../services/admin.service';
import {ConfigurationService} from '../../services/configuration.service';
import {WorkflowService} from '../../services/workflow.service';
import {ContentService} from '../../services/content-service.service';
import {ArchiveComponent} from './archive/archive.component';
import {SharedAddDocumentModule} from '../../shared-modules/add-document/add-document.module';
import {SharedSearchDocumentModule} from '../../shared-modules/search-document/search-document.module';
import {SharedDocumentCartModule} from '../../shared-modules/document-cart/document-cart.module';
import {SharedRecipientsModule} from '../../shared-modules/recipients/recipients.module';
import {SharedUserListModule} from '../../shared-modules/user-list/user-list.module';
import {
  MatOptionModule, MatSelectModule,
  MatTabsModule
} from '@angular/material';
import {APP_BASE_HREF} from '@angular/common';
import {BusyModule} from 'angular2-busy';
import {SharedRecipientsFilterModule} from "../../shared-modules/recipients-filter/user-list.module";
import {SharedDetailsModalModule} from "../../shared-modules/details-modal/details-modal.module";
import {SharedDocumentStatusModule} from '../../shared-modules/document-status/document-status.module';
import { FilterResultComponent } from './filter-result/filter-result.component';
import {SharedHTMLPipeModule} from "../../shared-modules/safe-html-pipe/safe-html-pipe";
import {PdfViewerModule} from "ng2-pdf-viewer";

@NgModule({
  declarations: [
    WorkflowComponent,
    DraftComponent,
    LaunchComponent,
    SentComponent,
    InboxComponent,
    ActionButtonComponent, FilterComponent,
    TaskDetailComponent,
    ArchiveComponent,
    FilterResultComponent,
  ],
  imports: [
    CommonModule,
    FormsModule,
    ReactiveFormsModule,
    BusyModule,
    WorkflowRoute,
    SharedDataTableModule,PdfViewerModule,
    HttpModule,
    AccordionModule,
    AutoCompleteModule,
    ButtonModule,
    CalendarModule,
    DropdownModule,
    InputTextModule,
    InputTextareaModule,
    OrderListModule,
    OverlayPanelModule,
    PanelModule,
    StepsModule,
    TabViewModule,
    TooltipModule,
    AsideModule,
    MatTabsModule,
    ConfirmDialogModule,
    SharedAddDocumentModule,
    SharedSearchDocumentModule,
    SharedDocumentCartModule,
    SharedRecipientsModule,
    SharedUserListModule,
    SharedRecipientsFilterModule,SharedDocumentStatusModule,
    SelectButtonModule,
    MatOptionModule,
    MatSelectModule,
    MenubarModule,
    DialogModule,
    FileUploadModule,
    SplitButtonModule,
    DataTableModule,
    MultiSelectModule,
    SharedDetailsModalModule,
    SharedHTMLPipeModule
  ],
  providers: [
    ContentService, AdminService,
    ConfigurationService, {provide: APP_BASE_HREF, useValue: '/'}
  ]

})
export class WorkflowModule {
}
