import {Routes, RouterModule} from '@angular/router';
import {ModuleWithProviders} from '@angular/core';
import {SettingsComponent} from './settings.component';

export const routes: Routes = [
  {
    path: '', component: SettingsComponent,
    children: [{
      path: '', component: SettingsComponent
    }
    ]
  },


];


export const SettingsRoute: ModuleWithProviders = RouterModule.forChild(routes);
