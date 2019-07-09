import { Route } from '@angular/router';

import { HomeRoutes } from './home/home.routes';
import { ChartRoutes } from './charts/chart.route';
import { BlankPageRoutes } from './blank-page/blankPage.routes';
import { SentCountRoutes } from './sentcount/sentcount.routes';
import { InboxworkitemRoutes } from './inboxworkitem/inboxworkitem.routes';
import { SentworkitemRoutes } from './sentworkitem/sentworkitem.routes';
import { DocumentRoutes } from './document/document.routes';
import { EsignDocumentRoutes } from './esigndocument/esigndocument.routes';
import { TableRoutes } from './tables/table.routes';
import { FormRoutes } from './forms/forms.routes';
import { GridRoutes } from './grid/grid.routes';
import { BSComponentRoutes } from './bs-component/bsComponent.routes';
import { BSElementRoutes } from './bs-element/bsElement.routes';

import { DashboardComponent } from './index';

export const DashboardRoutes: Route[] = [
    {
      path: 'dashboard',
      component: DashboardComponent,
      children: [
        ...HomeRoutes,
        ...ChartRoutes,
        ...BSComponentRoutes,
        ...TableRoutes,
        ...BlankPageRoutes,
        ...FormRoutes,
        ...GridRoutes,
        ...BSElementRoutes,
        ...SentCountRoutes,
        ...InboxworkitemRoutes,
        ...SentworkitemRoutes,
        ...DocumentRoutes,
        ...EsignDocumentRoutes
      ]
    }
];
