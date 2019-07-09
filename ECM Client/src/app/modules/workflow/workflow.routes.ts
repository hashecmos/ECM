import {Routes, RouterModule} from '@angular/router';
import {ModuleWithProviders} from '@angular/core';
import {InboxComponent} from './inbox/inbox.component';
import {LaunchComponent} from './launch/launch.component';
import {SentComponent} from './sent/sent.component';
import {DraftComponent} from './drafts/draft.component';
import {WorkflowComponent} from './workflow.component';
import {TaskDetailComponent} from './task-detail/task-detail.component';
import {ArchiveComponent} from './archive/archive.component';
import {FilterResultComponent} from './filter-result/filter-result.component';
export const routes: Routes = [
  {
    path: '', component: WorkflowComponent,
    children: [
      {path: '', redirectTo: 'inbox', pathMatch: 'full'},
      {path: 'inbox', component: InboxComponent},
      {path: 'draft', component: DraftComponent},
      {path: 'launch/:actionType', component: LaunchComponent},
      {path: 'launch', component: LaunchComponent},
      {path: 'sent', component: SentComponent},
      {path: 'archive', component: ArchiveComponent},
      {path: 'actioned', component: FilterResultComponent},
      {path: 'inbox/taskdetail/:id', component: TaskDetailComponent},
      {path: 'sent/taskdetail/:id', component: TaskDetailComponent},
      {path: 'archive/taskdetail/:id', component: TaskDetailComponent},
      {path: 'actioned/taskdetail/:id', component: TaskDetailComponent},
    ]
  },
];

export const WorkflowRoute: ModuleWithProviders = RouterModule.forChild(routes);
