import {Routes, RouterModule} from '@angular/router';
import {ModuleWithProviders} from '@angular/core';
import {BrowseComponent} from './browse.component';
import {AddDocumentComponent} from './add-document/add-document.component';
import {BrowseDocumentComponent} from './browse-documents/browse-document.component';
import {FavouriteFoldersComponent} from './favourite-folders/favourite-folders.component';
import {GroupFoldersComponent} from './group-folders/group-folders.component';

export const routes: Routes = [
  {
    path: '', component: BrowseComponent,
    children: [
      {path: 'favourite-folders', component: FavouriteFoldersComponent},
      {path: 'browse-folders', component: BrowseDocumentComponent},
      {path: 'group-folders', component: GroupFoldersComponent},
      {path: 'add-doc', component: AddDocumentComponent},
      {path: '', redirectTo: 'browse-folders', pathMatch: 'full'},
    ]
  },


];


export const BrowseRoute: ModuleWithProviders = RouterModule.forChild(routes);
