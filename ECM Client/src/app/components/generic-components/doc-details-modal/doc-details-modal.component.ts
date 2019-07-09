import {Component, OnInit, Input, Output, EventEmitter, OnDestroy} from '@angular/core';
import {DocumentService} from '../../../services/document.service';
import {DomSanitizer, SafeResourceUrl} from '@angular/platform-browser';
import {ConfirmationService} from 'primeng/primeng';
import {ContentService} from '../../../services/content-service.service';
import {CoreService} from "../../../services/core.service";
import {saveAs} from 'file-saver';

@Component({
  selector: 'app-doc-details-modal',
  templateUrl: './doc-details-modal.component.html',
  styleUrls: ['./doc-details-modal.component.css']
})
export class DocDetailsModalComponent implements OnInit, OnDestroy {
  @Input() public docInfo: any;
  @Input() public docSysProp: any;
  @Input() public docVersion: any;
  @Input() public docSecurity: any;
  @Input() public linkedDocuments: any;
  @Input() public docHistory: any;
  @Input() public foldersFiledIn: any;
  @Input() public docTitle: any;
  @Input() public noLink: any;
  @Input() public docTrack: any;
  public showIframe = false;
  public attach_url: any;
  public viewer = false;
  public headId: any;
  public selectedVersion: any = {props: []};
  public privilage: any;
  private subscriptions: any[] = [];

  constructor(private ds: DocumentService, private sanitizer: DomSanitizer, private confirmationService: ConfirmationService,
              private contentService: ContentService, private coreService: CoreService) {
  }

  ngOnInit() {
  }

  viewDoc(doc, type) {
    this.showIframe = true;
    if (type === 'link') {
      this.docTitle = doc.desc;
      this.attach_url = this.transform(this.ds.getViewUrl(doc.tail));
    } else {
      this.attach_url = this.transform(this.ds.getViewUrl(doc.id));
    }
    this.viewer = true;

  }

  transform(url) {
    return this.sanitizer.bypassSecurityTrustResourceUrl(url);
  }

  confirmRemoveLink(docLink) {
    this.confirmationService.confirm({
      message: 'Are you sure that you want to perform this action?',
      key: 'confirmRemoveLink',
      accept: () => {
        this.removeLink(docLink);
      }
    });
  }

  removeLink(doc) {
    this.headId = doc.head;
    const subscription = this.ds.removeLink(doc.head, doc.tail)
      .subscribe(data => this.successremoveLink());
    this.coreService.progress = {busy: subscription, message: ''};
    this.addToSubscriptions(subscription);
  }

  successremoveLink() {
    const subscription = this.ds.getLinks(this.headId)
      .subscribe(data => this.assignDocLink(data));
    this.coreService.progress = {busy: subscription, message: ''};
    this.addToSubscriptions(subscription);
  }

  assignDocLink(data) {
    if (data.length > 0) {
      this.linkedDocuments = data;
      this.noLink = false;
    }
    else {
      this.noLink = true;
    }
  }

  downloadDoc(doc) {
   //  window.location.assign(this.ds.downloadThisDocument(doc.id));
    const fileName = this.docInfo[0].mvalues;
    this.ds.downloadPDF(doc.id).subscribe(res => {
      const file = new Blob([res], {type: res.type});
      saveAs(file, fileName);
    });
  }

  selectVersion(version) {
    if (this.selectedVersion.id === version.id) {
      this.selectedVersion.id = undefined;
      return;
    }
    const props = [];
    this.selectedVersion.id = version.id;
    this.selectedVersion.props = [];
    if (version.props) {
      version.props.map(p => {
        this.selectedVersion.props.push({prop: p.prop, value: p.value});
      });
    } else {
      const subscription = this.ds.getThisDocument(version.id).subscribe(res => {
        res.props.map(p => {
          if (p.hidden === 'false') {
            props.push({prop: p.desc, value: p.mvalues[0]});
          }
        });
        version.props = props;
        this.selectedVersion.props = props;


      });
      this.coreService.progress = {busy: subscription, message: ''};
      this.addToSubscriptions(subscription);
    }
  }

  showPrivilages(data) {
    const subscription = this.contentService.getAccessPrivileges(data.accessMask).subscribe(val => this.assignPrivilages(val));
    this.coreService.progress = {busy: subscription, message: ''};
    this.addToSubscriptions(subscription);
  }

  assignPrivilages(data) {
    this.privilage = data;
  }

  closeViewPopUp() {
    this.showIframe = false;
  }

  addToSubscriptions(subscription) {
    this.subscriptions.push(subscription);
  }

  clearSubscriptions() {
    this.subscriptions.map(s => {
      s.unsubscribe();
    });
  }

  ngOnDestroy() {
    this.clearSubscriptions();
  }
}
