import {Routes, RouterModule} from '@angular/router';
import {ModuleWithProviders} from '@angular/core';
import {SearchComponent} from './search.component';
import {SimpleSearchComponent} from './simple-search/simple-search.component';
import {AdvanceSearchComponent} from './advance-search/advance-search.component';

export const routes: Routes = [
  {
    path: '', component: SearchComponent,
    children: [
      {path: 'simple-search', component: SimpleSearchComponent},
      {path: 'advance-search', component: AdvanceSearchComponent}
    ]
  },

];


export const SearchRoute: ModuleWithProviders = RouterModule.forChild(routes);
