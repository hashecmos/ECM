import {
  Component, OnInit, AfterViewInit, Input, Output, EventEmitter, Renderer, NgZone, ElementRef, ViewChild,
  OnDestroy
} from '@angular/core';
import {Router, ActivatedRoute} from '@angular/router';
import {FormBuilder, FormGroup, Validators, FormControl} from '@angular/forms';
// services
import {WorkflowService} from '../../../services/workflow.service';
import {UserService} from '../../../services/user.service';
import {DocumentService} from '../../../services/document.service';
import {ContentService} from '../../../services/content-service.service';
import {Subscription} from 'rxjs/Rx';
import * as $ from 'jquery';
import {SelectItem, Message, FileUpload} from 'primeng/primeng';
// models
import {User} from '../../../models/user/user.model';
import {WorkitemDetails} from '../../../models/workflow/workitem-details.model';
import {EntryTemplate} from '../../../models/document/entry-template.model';
import {EntryTemplateDetails} from '../../../models/document/entry-template-details.model';
import {BrowserEvents} from '../../../services/browser-events.service';
import {GrowlService} from '../../../services/growl.service';
import {CoreService} from "../../../services/core.service";
import {BreadcrumbService} from "../../../services/breadcrumb.service";
import {AdminService} from "../../../services/admin.service";

@Component({
  selector: 'app-add-document',
  templateUrl: './add-document.component.html',
  styleUrls: ['./add-document.component.css'],
})
export class AddDocumentComponent implements OnInit, AfterViewInit, OnDestroy {
  @Output() onAddSuccess = new EventEmitter();
  private subscriptions: Subscription[] = [];
  @Input() public assignedPath: any;
  @Input() public assignedId: any;
  @Input() public screen: any;
  public currentUser: User;
  public entryTemplates: any[];
  public newClassDetails = new EntryTemplateDetails();
  public selectedEntryTemplate: SelectItem[];
  public entryTemplate: any[];
  public selectedTemplateName: any;
  public selectedTemplateId: any;
  public showClassDetails = false;
  public newDocumentForm: FormGroup;
  public newDocFormData = new FormData();
  removeEnabled = false;
  uploadedFiles: any;
  msgs: Message[] = [];
  public scanners: TwainSource[] = [];
  DWObject = null;
  selectedTwainSource: TwainSource = null;
  CurrentPath = null;
  folderId: any;
  folderpath: any;
  errorJson: any;
  public loaded = false;
  public displayScannerSettings = false;
  errorMessage: any;
  public docFromScanner = false;
  public selectedScanner: any;
  public ecmNo;

  constructor(private cs: ContentService, private fb: FormBuilder, private breadcrumbService: BreadcrumbService, private as: AdminService,
              private router: Router, private wfs: WorkflowService, private us: UserService, private coreService: CoreService,
              private ds: DocumentService, private bs: BrowserEvents, private growlService: GrowlService) {
    this.entryTemplate = [];
    this.currentUser = this.us.getCurrentUser();
    this.newDocumentForm = fb.group({
      'DocumentTitle': [null, Validators.required],
    });
  }

  setEcmNo() {
    const subscriptions = this.as.getNextECMNo().subscribe(data => {
      this.ecmNo = data;
    });
    this.addToSubscriptions(subscriptions);
  }

  ngOnInit() {
    this.setEcmNo();
    this.bs.addDocPath.subscribe(data => this.assignPath(data));
    this.bs.addDocId.subscribe(data => this.assignId(data));
    const subscription = this.cs.getEntryTemplates().subscribe(data => {
      this.entryTemplates = data;
      for (let i = 0; i < data.length; i++) {
        this.entryTemplate.push({label: data[i].symName, value: data[i].id});
      }
      if (this.entryTemplates.length > 0) {
        this.selectedEntryTemplate = this.entryTemplate[0].value;
        this.changeNewClass();
      }
    });
    this.coreService.progress = {busy: subscription, message: ''};
    this.addToSubscriptions(subscription);
    this.folderpath = this.assignedPath;
    this.folderId = this.assignedId;
  }

  assignPath(data) {
    this.folderpath = data;
  }

  assignId(data) {
    this.folderId = data;
  }

  noWhitespaceValidator(control: FormControl) {
    let isWhitespace = (control.value || '').trim().length === 0;
    let isValid = !isWhitespace;
    return isValid ? null : {'whitespace': true}
  }

  changeNewClass() {
    const subscription = this.cs.getEntryTemplate(this.selectedEntryTemplate).subscribe(data => {
        this.newDocumentForm = this.fb.group({
          'DocumentTitle': [null, Validators.required],
        });
        this.newClassDetails = data;
        this.selectedTemplateName = data.symName;
        this.selectedTemplateId = data.id;
        this.newClassDetails.props.forEach(control => {
          if (control.symName === 'ECMNo') {
            control.mvalues[0] = this.ecmNo;
          }
          if (control.req === 'true' && control.hidden === 'false') {
            if (control.dtype === 'DATE') {
              this.newDocumentForm.addControl(control.symName, new FormControl(null, Validators.required));
            } else {
              this.newDocumentForm.addControl(control.symName, new FormControl(null, [Validators.required, this.noWhitespaceValidator]));

            }
          } else if (control.hidden === 'false') {
            this.newDocumentForm.addControl(control.symName, new FormControl(null, Validators.maxLength(200)));
          }

          if (control.symName === 'OrgCode') {
            if (control.lookups) {
              const removables = [];
              control.lookups.map((d, i) => {
                if (d.label.trim().length > 4) {
                  removables.push(i);
                }
              });
              removables.map((d, i) => {
                control.lookups.splice(d - i, 1);
              });
            }
          }
        });
        if (this.newClassDetails !== null) {
          this.showClassDetails = true;
        } else {
          this.showClassDetails = false;
        }
      }, error => this.showClassDetails = false
    );
    this.coreService.progress = {busy: subscription, message: ''};
    this.addToSubscriptions(subscription);
  }

  onUpload(event) {
    if ((event.files[0].name).toLowerCase().includes('.jpg')||(event.files[0].name).toLowerCase().includes('.jpeg')
      || (event.files[0].name).toLowerCase().includes('.png') || (event.files[0].name).toLowerCase().includes('.gif')
      ||(event.files[0].name).toLowerCase().includes('.pdf') || (event.files[0].name).toLowerCase().includes('.doc')
      || (event.files[0].name).toLowerCase().includes('.zip')|| (event.files[0].name).toLowerCase().includes('.tiff')
      || (event.files[0].name).toLowerCase().includes('.docx') || (event.files[0].name).toLowerCase().includes('.xls')
      || (event.files[0].name).toLowerCase().includes('.xlsx')
      || (event.files[0].name).toLowerCase().includes('.ppt') || (event.files[0].name).toLowerCase().includes('.pptx')) {
      this.uploadedFiles = event.files[0];
      const docTitle = this.uploadedFiles.name.split('.').slice(0, -1).join(".");
      this.newDocumentForm.get('DocumentTitle').setValue(docTitle);
    }

  }

  addDocument(event) {
    if (!this.docFromScanner) {
      this.newDocFormData = new FormData();
      this.newDocFormData.append('document', this.uploadedFiles);

    }
    if ((Object.keys(this.newDocumentForm.controls)) &&
      (Object.keys(this.newDocumentForm.controls).length < 1)) {
    } else {
      const docInfo = {
        creator: this.currentUser.fulName,
        folder: this.folderId,
        docclass: this.selectedTemplateName,
        entryTemplate: this.selectedTemplateId,
        props: [{
          'name': 'Document Title', 'symName': 'DocumentTitle', 'dtype': 'STRING', 'mvalues': [''],
          'mtype': 'N', 'len': 255, 'rOnly': 'false', 'hidden': 'false', 'req': 'false'
        }],
        accessPolicies: []
      };

      for (const control of Object.keys(this.newDocumentForm.controls)) {
        if (control === 'DocumentTitle') {
          docInfo.props[0].mvalues = [this.newDocumentForm.get(control).value];
        } else if (this.getControlDataType(control) === 'DATE') {
          if (this.newDocumentForm.get(control).value !== null) {
            const prop = {
              'name': '',
              'symName': control,
              'dtype': 'DATE',
              'mvalues': [this.getFormatedDate(this.newDocumentForm.get(control).value)],
              'mtype': 'N',
              'len': 255,
              'rOnly': 'false',
              'hidden': 'false',
              'req': 'false'
            };
            docInfo.props.push(prop);
          } else {
            const prop = {
              'name': '',
              'symName': control,
              'dtype': 'DATE',
              'mvalues': [null],
              'mtype': 'N',
              'len': 255,
              'rOnly': 'false',
              'hidden': 'false',
              'req': 'false'
            };
            docInfo.props.push(prop);

          }


        }
        else {
          const prop = {
            'name': '', 'symName': control, 'dtype': 'STRING', 'mvalues': [this.newDocumentForm.get(control).value],
            'mtype': 'N', 'len': 255, 'rOnly': 'false', 'hidden': 'false', 'req': 'false'
          };
          if (prop.symName === 'ECMNo') {
            prop.mvalues[0] = this.ecmNo;
          }
          docInfo.props.push(prop);
        }

      }

      this.newDocFormData.append('DocInfo', JSON.stringify(docInfo));

      let allRequiredFieldsFilled = true;
      this.newClassDetails.props.forEach(control => {
        if (control.req === 'true' && control.hidden === 'false') {
          if (control.dtype === 'DATE') {
            if (this.newDocumentForm.get(control.symName).value === null || !this.folderpath) {
              allRequiredFieldsFilled = false;
            }
          } else {
            if (this.newDocumentForm.get(control.symName).value === null || this.newDocumentForm.get(control.symName).value === '' || this.newDocumentForm.get(control.symName).value.trim().length === 0 || !this.folderpath) {
              allRequiredFieldsFilled = false;
            }
          }
        }
      });
      if (allRequiredFieldsFilled === true) {
        const subscription = this.ds.addDocument(this.newDocFormData).subscribe(
          (data) => this.addDocSuccess(data),
          (err) => this.addDocFailed(err));
        this.coreService.progress = {busy: subscription, message: ''};
        this.addToSubscriptions(subscription);
      } else if (allRequiredFieldsFilled === false) {
        this.growlService.showGrowl({
          severity: 'error',
          summary: 'Fill Required', detail: 'Fill All Required Fields'
        });
      }
    }
  }

  getControlDataType(control) {
    if ((this.newClassDetails != null) && (this.newClassDetails.props != null)) {
      for (const prop of this.newClassDetails.props) {
        if (prop.symName === control) {
          return prop.dtype;
        }
      }
    }
    return null;
  }

  getFormatedDate(value) {
    const date = new Date(value);
    return date.getDate() + '/' + (date.getMonth() + 1) + '/' + date.getFullYear();
  }

  addDocSuccess(data) {
    this.growlService.showGrowl({
      severity: 'info',
      summary: 'Success', detail: 'Add Document Success'
    });
    const newDocID = data;
    this.clearForm();
    this.uploadedFiles = null;
    this.docFromScanner = false;
    const subscription = this.ds.getDocument(newDocID).subscribe(doc => {
      const document = doc;
      this.onAddSuccess.emit(document);
    });
    this.coreService.progress = {busy: subscription, message: ''};
    this.addToSubscriptions(subscription);
  }

  addDocFailed(error) {
    this.errorJson = JSON.parse(error.error).responseMessage;
    this.growlService.showGrowl({
      severity: 'error',
      summary: 'Failure', detail: this.errorJson
    });
  }

  selectFolder() {
    this.folderpath = localStorage.getItem('path');
    this.folderId = localStorage.getItem('folderId');
    this.removeEnabled = true;
  }


  clearForm() {
    for (const control of Object.keys(this.newDocumentForm.controls)) {
      if (this.getControlDataType(control) === 'DATE') {
        // this.newDocumentForm.patchValue({ control: null });
        this.newDocumentForm.get(control).setValue(null);
      } else {
        this.newDocumentForm.get(control).setValue(null);
      }
    }
    this.setEcmNo();
  }

  ngAfterViewInit() {
    // if (!this.loaded) {

    // if(typeof Dynamsoft !=='undefined'){
    //    Dynamsoft.WebTwainEnv.Load();
    //  Dynamsoft.WebTwainEnv.RegisterEvent('OnWebTwainReady', this.onTwainReady);
    // }

    // this.loaded = true;

    // this.loaded = true;

    // Dynamsoft.WebTwainEnv.RegisterEvent('OnWebTwainReady', this.onTwainReady);
    // this.loaded = true;

    //}
    //Dynamsoft.WebTwainEnv.AutoLoad = true;
  }

  loadDynamsoft() {
    Dynamsoft.WebTwainEnv.Load();
    Dynamsoft.WebTwainEnv.RegisterEvent('OnWebTwainReady', this.onTwainReady);
    setTimeout(() => {
      this.getTwainSources();
    }, 1000);


  }

  onTwainReady() {
    this.CurrentPath = decodeURI(location.pathname).substring(0, decodeURI(location.pathname).lastIndexOf('/') + 1);
    this.DWObject = Dynamsoft.WebTwainEnv.GetWebTwain('dwtcontrolContainer');
    if (this.DWObject) {
      if (!Dynamsoft.Lib.env.bMac) {
        let localPDFRVersion = this.DWObject._innerFun('GetAddOnVersion', '["pdf"]');
        if (Dynamsoft.Lib.env.bIE) {
          localPDFRVersion = this.DWObject.getSWebTwain().GetAddonVersion('pdf');
        }
        if (localPDFRVersion !== Dynamsoft.PdfVersion) {
          const ObjString = [];
          document.getElementById('info').style.display = 'block';
        }
      }
    }
  }

  getTwainSources(cb?) {
    this.scanners = [];
    this.CurrentPath = decodeURI(location.pathname).substring(0, decodeURI(location.pathname).lastIndexOf('/') + 1);
    this.DWObject = Dynamsoft.WebTwainEnv.GetWebTwain('dwtcontrolContainer');
    if (this.DWObject) {
      for (let i = 0; i < this.DWObject.SourceCount; i++) {
        this.scanners.push({idx: i, name: this.DWObject.GetSourceNameItems(i)});
      }
      this.DWObject.IfDisableSourceAfterAcquire = true;
      this.DWObject.SetViewMode(2, 2);
    }
    this.displayScannerSettings = true;
  }

  downloadPDFR() {
    this.DWObject.Addon.PDF.Download(
      'http://' + location.host + this.CurrentPath + 'Resources/addon/Pdf.zip',
      function () {
        document.getElementById('info').style.display = 'none';
      },
      function (errorCode, errorString) {
      });
  }


  AcquireImage() {
    const param = {
      IfShowUI: true,
      IfFeederEnabled: true,
      Resolution: 200,
      IfDuplexEnabled: false,
      PixelType: 2
    };
    this.DWObject.IfDisableSourceAfterAcquire = true;
    console.log(this.selectedScanner);
    this.selectedTwainSource = this.selectedScanner;
    if (this.selectedTwainSource) {
      console.log(this.selectedTwainSource);
      if (!this.DWObject.SelectSourceByIndex(this.selectedTwainSource.idx)
        || !this.DWObject.OpenSource()
        || !this.DWObject.AcquireImage(param, function () {
          },
          function (errorCode, errorString) {
            console.dir({errorCode: errorCode, errorString: errorString});
          })) {
        console.dir({errorCode: this.DWObject.ErrorCode, errorString: this.DWObject.ErrorString});
      }
    }
  }

  selectScanner(val) {
    this.selectedTwainSource = val;
  }

  saveScannedImages() {
    let imagedata = null;
    if (this.DWObject) {
      if (this.DWObject.HowManyImagesInBuffer > 0) {
        this.DWObject.SelectedImagesCount = this.DWObject.HowManyImagesInBuffer;
        for (let i = 0; i < this.DWObject.HowManyImagesInBuffer; i++) {
          this.DWObject.SetSelectedImageIndex(i, i);
        }
        this.DWObject.GetSelectedImagesSize(4); // 4 for PDF
        imagedata = this.DWObject.SaveSelectedImagesToBase64Binary();
        this.newDocFormData = new FormData();
        this.newDocFormData.append('document', imagedata);
        this.newDocumentForm.get('DocumentTitle').setValue('Scanned Document');
        this.docFromScanner = true;
      }
      this.DWObject.RemoveAllImages();
      this.displayScannerSettings = false;
      this.growlService.showGrowl({
        severity: 'info',
        summary: 'Success', detail: 'Saved Document Successfully'
      });
    } else {
      this.growlService.showGrowl({
        severity: 'error',
        summary: 'Failure', detail: 'Failed To Save'
      });
    }
  }

  removeFolderPath() {
    this.folderId = "";
    this.folderpath = "";
    this.removeEnabled = false;
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
    this.subscriptions = [];
    this.uploadedFiles = undefined;
    this.scanners = null;
    this.currentUser = undefined;
    this.entryTemplates = [];
    this.newClassDetails = undefined;
    this.selectedEntryTemplate = [];
    this.entryTemplate = [];
    this.selectedTemplateName = undefined;
    this.selectedTemplateId = undefined;
    this.showClassDetails = false;
    this.newDocumentForm = undefined;
    this.newDocFormData = undefined;
    this.msgs = [];
    this.CurrentPath = undefined;
    this.folderId = undefined;
    this.folderpath = undefined;
    this.loaded = false;
    this.scanners = [];
    this.DWObject = undefined;
    this.selectedTwainSource = undefined;
    this.displayScannerSettings = false;
    this.docFromScanner = undefined;
    if (typeof Dynamsoft !== 'undefined') {
      Dynamsoft.WebTwainEnv.Unload();
    }

  }
}

declare var Dynamsoft;

export interface TwainSource {
  idx: number;
  name: string;
}

export interface IMyMarkedDates {
  dates: Array<Date>;
  color: string;
}
