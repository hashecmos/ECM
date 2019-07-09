import {AccessPolicyMappingComponent} from './accesspolicy-mapping/accesspolicy-mapping.component';
import {AccesspolicyComponent} from './accesspolicy/accesspolicy.component';
import {Routes, RouterModule} from '@angular/router';
import {ModuleWithProviders} from '@angular/core';
import {AdministrationComponent} from './administration.component';
import {ConfigurationsComponent} from './configurations/configurations.component';
import {EntrytemplateMappingComponent} from './entrytemplate-mapping/entrytemplate-mapping.component';
import {IntegrationComponent} from './integration/integration.component';
import {LookupmappingComponent} from './lookupmapping/lookupmapping.component';
import {LookupsComponent} from './lookups/lookups.component';
import {NewsComponent} from './news/news.component';
import {RolemanagementComponent} from './rolemanagement/rolemanagement.component';
import {ErrorlogManagementComponent} from './errorlog-management/errorlog-management.component';
import {EcmUsersComponent} from './ecm-users/ecm-users.component';
import {EcmReportUserComponent} from './ecm-report-user/ecm-report-user.component';
import {EcmAdminUsersComponent} from "./ecm-admin-users/ecm-admin-users.component";
import {EcmGlobalListComponent} from "./ecm-global-list/ecm-global-list.component";
import {EcmAdminLogsComponent} from "./ecm-admin-logs/ecm-admin-logs.component";
import {EcmExcludeUsersComponent} from "./ecm-exclude-users/ecm-exclude-users.component";

export const routes: Routes = [
  {
    path: '', component: AdministrationComponent,
    children: [
      {path: 'configurations', component: ConfigurationsComponent},
      {path: 'access-policies', component: AccesspolicyComponent},
      {path: 'lookups', component: LookupsComponent},
      {path: 'lookup-mapping', component: LookupmappingComponent},
      {path: 'news', component: NewsComponent},
      {path: 'role-management', component: RolemanagementComponent},
      {path: 'access-policy-mapping', component: AccessPolicyMappingComponent},
      {path: 'entry-template-mapping', component: EntrytemplateMappingComponent},
      {path: 'integration', component: IntegrationComponent}, {path: 'errorlog-management', component: ErrorlogManagementComponent},
      {path: 'ecm-users', component: EcmUsersComponent},
      {path: 'ecm-users', component: EcmUsersComponent},
      {path: 'ecm-report-user', component: EcmReportUserComponent},
      {path: 'ecm-admin-user', component: EcmAdminUsersComponent},
      {path: 'ecm-global-list', component: EcmGlobalListComponent},
      {path: 'ecm-admin-logs', component: EcmAdminLogsComponent},
      {path: 'ecm-exclude-users', component: EcmExcludeUsersComponent},
    ]
  },


];

export const AdministrationRoute: ModuleWithProviders = RouterModule.forChild(routes);
