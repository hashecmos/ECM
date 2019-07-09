import {Routes, RouterModule} from '@angular/router';
import {ModuleWithProviders} from '@angular/core';
import {DashboardComponent} from './dashboard/dashboard.component';
import {MainComponent} from './main.component';
import {RecentsComponent} from '../../components/shortcut-components/recents/recents.component';
import {FavouritesComponent} from '../../components/shortcut-components/favourites/favourites.component';
import {TeamsharedDocsComponent} from '../../components/shortcut-components/teamshared-docs/teamshared-docs.component';
import {FavouriteFoldersComponent} from '../browse/favourite-folders/favourite-folders.component';

export const routes: Routes = [
  {
    path: '', component: MainComponent,
    children: [
      {path: '', component: DashboardComponent},
      {path: 'favourites', component: FavouritesComponent},
      {path: 'recents', component: RecentsComponent},
      {path: 'teamshared', component: TeamsharedDocsComponent},
      {path: 'dashboard', redirectTo: '/'}
    ]
  },


];

export const MainRoute: ModuleWithProviders = RouterModule.forChild(routes);
