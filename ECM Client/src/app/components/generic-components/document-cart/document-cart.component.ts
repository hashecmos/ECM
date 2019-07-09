import {Component, OnInit, Input, Output, EventEmitter, SimpleChanges, OnChanges, OnDestroy} from '@angular/core';
import {UserService} from '../../../services/user.service';
import {DocumentService} from '../../../services/document.service';
import {GrowlService} from '../../../services/growl.service';
import {CoreService} from "../../../services/core.service";

@Component({
  selector: 'app-document-cart',
  templateUrl: './document-cart.html'
})
export class DocumentCartComponent implements OnInit, OnChanges, OnDestroy {
  @Input() public cartItems: any[];
  @Input() public canRemoveLastItem = false;
  @Input() public showHeading = true;
  @Output() onItemSelect = new EventEmitter();
  @Output() onItemRemoved = new EventEmitter();
  @Output() onItemPreview = new EventEmitter();
  @Input() public isItemSelectable = false;
  private removeInProgress = false;
  private currentUser: any;
  private subscriptions: any[]=[];

  constructor(private userService: UserService, private documentService: DocumentService,
              private growlService: GrowlService,private coreService:CoreService) {

  }

  ngOnInit() {
    this.currentUser = this.userService.getCurrentUser();
  }

  mOnItemSelect(item) {
    this.onItemSelect.emit(item);
  }

  ngOnChanges(changes: SimpleChanges) {
  }

  removeFromCart(item) {
    if (this.removeInProgress) {
      return;
    }
    this.removeInProgress = true;
    const subscription = this.documentService.removeFromCart(this.currentUser.EmpNo, item.id).subscribe((data) => {
      if (data=== 'OK') {
        this.growlService.showGrowl({
          severity: 'info',
          summary: 'Success', detail: 'Document Removed From Cart'
        });
      }

      this.onItemRemoved.emit();
      this.refreshCart();
    }, (err) => {
      this.growlService.showGrowl({
        severity: 'error',
        summary: 'Failure', detail: 'Failed To Remove From Cart'
      });
      this.refreshCart();
    });
    this.coreService.progress={busy:subscription,message:'',backdrop:true};
    this.addToSubscriptions(subscription);

  }

  refreshCart() {
    const subscription=this.documentService.getCart(this.currentUser.EmpNo).subscribe(res => {
      this.documentService.refreshCart(res);
      this.removeInProgress = false;
    }, err => {
      this.removeInProgress = false;
    });
     this.coreService.progress={busy:subscription,message:'',backdrop:true};
    this.addToSubscriptions(subscription);
  }

   addToSubscriptions(subscription) {
    this.subscriptions.push(subscription);
  }

  clearSubscriptions() {
    this.subscriptions.map(s => {
      s.unsubscribe();
    });
  }
  showDocPreview(item){
    this.onItemPreview.emit(item);
  }

  ngOnDestroy() {
    this.clearSubscriptions();
  }


}
