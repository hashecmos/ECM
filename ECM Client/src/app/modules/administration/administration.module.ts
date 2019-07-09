import {AdminService} from '../../services/admin.service';
import {NewsService} from '../../services/news.service';
import {NgModule} from '@angular/core';
import {AdministrationComponent} from './administration.component';
import {
  ButtonModule, CalendarModule, CheckboxModule, DataTableModule, DropdownModule, InputTextModule,
  DialogModule,
  ConfirmDialogModule, InputTextareaModule,
  TabViewModule, SpinnerModule,
  AutoCompleteModule, PanelModule, EditorModule, AccordionModule, SelectButtonModule, TooltipModule, RadioButtonModule, InputSwitchModule
} from 'primeng/primeng';

import {FormsModule} from '@angular/forms';
import {CommonModule} from '@angular/common';
import {AdministrationRoute} from './administration.routes';
import {ContentService} from '../../services/content-service.service';
import {ConfigurationService} from '../../services/configuration.service';
import {AccessPolicyService} from '../../services/access-policy.service';

import {SharedRoleTreeModule} from '../../shared-modules/role-tree/role-tree.module';
import {SharedUserListModule} from '../../shared-modules/user-list/user-list.module';
import {AccesspolicyComponent} from './accesspolicy/accesspolicy.component';
import {ConfigurationsComponent} from './configurations/configurations.component';
import {LookupmappingComponent} from './lookupmapping/lookupmapping.component';
import {LookupsComponent} from './lookups/lookups.component';
import {NewsComponent} from './news/news.component';
import {RolemanagementComponent} from './rolemanagement/rolemanagement.component';
import {EntrytemplateMappingComponent} from './entrytemplate-mapping/entrytemplate-mapping.component';
import {AccessPolicyMappingComponent} from './accesspolicy-mapping/accesspolicy-mapping.component';
import {IntegrationComponent} from './integration/integration.component';
import {BusyModule} from 'angular2-busy';
import {ErrorlogManagementComponent} from './errorlog-management/errorlog-management.component';
import { EcmUsersComponent } from './ecm-users/ecm-users.component';
import {SharedHTMLPipeModule} from '../../shared-modules/safe-html-pipe/safe-html-pipe';
import { EcmReportUserComponent } from './ecm-report-user/ecm-report-user.component';
import { EcmAdminUsersComponent } from './ecm-admin-users/ecm-admin-users.component';
import {SharedUserListsModule} from "../../shared-modules/user-lists/user-lists.module";
import {SharedEditApPermissionModule} from "../../shared-modules/edit-ap-permission/edit-ap-permission.module";
import { EcmGlobalListComponent } from './ecm-global-list/ecm-global-list.component';
import {SplitPaneModule} from "ng2-split-pane/lib/ng2-split-pane";
import { EcmAdminLogsComponent } from './ecm-admin-logs/ecm-admin-logs.component';
import { EcmExcludeUsersComponent } from './ecm-exclude-users/ecm-exclude-users.component';

@NgModule({
  declarations: [
    AdministrationComponent, ConfigurationsComponent, AccesspolicyComponent,
    LookupsComponent, LookupmappingComponent, NewsComponent, RolemanagementComponent, EntrytemplateMappingComponent,
    AccessPolicyMappingComponent, IntegrationComponent, ErrorlogManagementComponent,
    EcmUsersComponent,
    EcmReportUserComponent,
    EcmAdminUsersComponent,
    EcmGlobalListComponent,
    EcmAdminLogsComponent,
    EcmExcludeUsersComponent],
  imports: [
    PanelModule,
    CommonModule,
    FormsModule,
    TabViewModule,AccordionModule,SelectButtonModule,TooltipModule,ButtonModule,SplitPaneModule,
    AdministrationRoute, InputTextModule, InputTextareaModule, ConfirmDialogModule, CheckboxModule,EditorModule,SharedHTMLPipeModule,SpinnerModule,
    CalendarModule, BusyModule,SharedUserListsModule,RadioButtonModule, InputSwitchModule,
    ButtonModule,
    DataTableModule,
    DialogModule,
    DropdownModule,
    AutoCompleteModule,
    FormsModule,
    SharedRoleTreeModule,
    SharedUserListModule,
    SharedEditApPermissionModule
  ],
  providers: [AdminService, ContentService, ConfigurationService, NewsService, AccessPolicyService],
})
export class AdministrationModule {
}
