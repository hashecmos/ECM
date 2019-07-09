import {Routes, RouterModule, PreloadAllModules} from '@angular/router';
import {ModuleWithProviders} from '@angular/core';
import {HomeComponent} from './components/generic-components/home/home.component';
import {AuthGuardHomeService} from './services/auth-guard-home.service';
import {DefaultUrlGuardService} from "./services/default-url-guard.service";

export const routes: Routes = [
  {
    path: '', component: HomeComponent, canActivate: [AuthGuardHomeService], children: [
      {path: 'workflow', loadChildren: 'app/modules/workflow/workflow.module#WorkflowModule'},
      {path: 'browse', loadChildren: 'app/modules/browse/browse.module#BrowseModule'},
      {path: 'search', loadChildren: 'app/modules/search/search.module#SearchModule'},
      {path: 'settings', loadChildren: 'app/modules/settings/settings.module#SettingsModule'},
      {path: 'administration', loadChildren: 'app/modules/administration/administration.module#AdministrationModule'},
      {path: 'report', loadChildren: 'app/modules/report/report.module#ReportModule'},
      {path: '', loadChildren: 'app/modules/main/main.module#MainModule', canActivate: [DefaultUrlGuardService]},

    ]
  },
  {path: 'auth', loadChildren: 'app/modules/auth/auth.module#AuthModule'}
];

export const AppRoutes: ModuleWithProviders = RouterModule.forRoot(routes,{preloadingStrategy: PreloadAllModules});
