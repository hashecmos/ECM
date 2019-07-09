import {Injectable} from '@angular/core';
import {
  HttpEvent, HttpInterceptor, HttpHandler, HttpRequest, HttpResponse,
  HttpErrorResponse
} from '@angular/common/http';
import {Observable} from "rxjs/Observable";
import {GrowlService} from "./growl.service";

@Injectable()
export class HttpInterceptorService implements HttpInterceptor {
  constructor(private growlService:GrowlService){

  }
  intercept(req: HttpRequest<any>, next: HttpHandler): Observable<HttpEvent<any>> {
    return next.handle(req).do((event:HttpEvent<any>)=>{
      if(event instanceof HttpResponse){

      }
    },(err:any)=>{
      console.log("error "+JSON.stringify(err));
      let message=err.statusText;
      const errorRegex=/(.+):(.+)/g;
      if(err.message){
        message=err.message;
      }
         if(errorRegex.test(err.error) && err.error.indexOf('{')===-1 && err.error.indexOf('}')===-1){
            errorRegex.exec(err.error);
        const matches=errorRegex.exec(err.error);
        if(matches!==null){
          message=matches[matches.length-1];
        }
      }else if(err.error && typeof err.error==="object"){
          message=err.error.responseMessage;
          }
      else if(err.error && typeof err.error==="string" && err.error.indexOf('{')!==-1 && err.error.indexOf('}')!==-1){
          message=JSON.parse(err.error).responseMessage;
          }


      this.growlService.showGrowl({
      severity: 'error',
      summary: 'Failure', detail: message
    });
    });
  }
}
