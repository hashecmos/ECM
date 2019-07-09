import {
  Component,
  Input,
  Output,
  EventEmitter,
  OnDestroy,
  OnInit
} from '@angular/core';
import {DocumentService} from '../../../services/document.service';
import {WorkflowService} from '../../../services/workflow.service';
import {BrowserEvents} from '../../../services/browser-events.service';
import {CoreService} from '../../../services/core.service';
import {GrowlService} from "../../../services/growl.service";
import {UserService} from "../../../services/user.service";
import {User} from "../../../models/user/user.model";
import {assign} from "rxjs/util/assign";
import {Subscription} from "rxjs/Subscription";

@Component({
  selector: 'app-search-document',
  templateUrl: './search.component.html'
})
export class SearchDocumentComponent implements OnDestroy, OnInit {
  private savedSearches: any[] = [];
  private showSaveSearchModal = false;
  saveSearchObj: any = {};
  private user: User;
  @Input() public data: any;
  @Input() public isSimpleSearch = true;
  @Output() existsInList = new EventEmitter();
  @Output() onSearchComplete = new EventEmitter();
  private dynamicProps: any;
  private subscriptions: any = [];
  @Input() showSavedSearches = false;
  private selectedSearch: any;
  private savedSearchesHidden = false;
  private searchAlreadyExists = false;
  //busy: Subscription;
  temparray: any[];
  isClassChange = false;

  constructor(private documentService: DocumentService, private ws: WorkflowService,
              private browserEvents: BrowserEvents, private coreService: CoreService,
              private growlService: GrowlService, private userService: UserService) {

  }

  ngOnInit() {
    this.user = this.userService.getCurrentUser();
    if (this.isSimpleSearch) {
      if (this.data.model.contentSearch.mvalues[0] !== [] && this.data.model.contentSearch.oper) {
        this.documentService.savedSearch.simpleSearchText = this.data.model.contentSearch.mvalues[0];
        console.log("search being called");
        this.documentService.savedSearch.searchCriteria = this.data.model.contentSearch.oper;
        this.searchDocument();
      }
    } else {
      this.getEntryTemplateForSearch();
    }
    const eventSubscription = this.browserEvents.searchTextChanged$.subscribe(params => {
      if (this.data) {
        if (!this.isSimpleSearch || (this.data.model.contentSearch.mvalues[0] !== [] && this.data.model.contentSearch.oper)) {
          console.log("search text changed from header");
          this.searchDocument();
          //eventSubscription.unsubscribe();
        }
      }
    });

    this.getSavedSearches(true);
    if (this.documentService.savedSearch.searchResultsSaved) {
      this.data.continueData = this.documentService.savedSearch.searchResultsSaved.continueData;
      this.data.totalResults = this.documentService.savedSearch.searchResultsSaved.totalResults;
      this.data.searchResult = this.documentService.savedSearch.searchResultsSaved.row;
    }
    if (this.documentService.savedSearch.simpleSearchText) {
      this.data.model.contentSearch.mvalues[0] = this.documentService.savedSearch.simpleSearchText;
    }
     if (this.documentService.savedSearch.searchCriteria) {
      this.data.model.contentSearch.oper = this.documentService.savedSearch.searchCriteria;
    }
  }

  onSearchSelect() {

  }

  closeSaveSearchModal() {
    this.showSaveSearchModal = false;
  }

  onSaveSearchModalHide() {
    this.saveSearchObj = {};
  }

  changeProp(desc) {
    this.data.searchTemplate.props.map((prop) => {
      if (prop.desc === desc) {
        prop.show = false;

      }
    })
  }

  addDynamicProp(selectedProp?) {
    const propArr = [];
    let sProp;
    this.data.searchTemplate.props.map((prop) => {
      if ((!prop.selected || (selectedProp && prop.symName === selectedProp.symName)) && prop.hidden.toLowerCase() === 'false'
        && prop.dtype.toLowerCase() !== 'object') {
        propArr.push({
          label: prop.desc.length > 0 ? prop.desc : prop.symName, value: {
            dtype: prop.dtype, symName: prop.symName,
            lookups: prop.lookups
          }
        })
      }

      if (selectedProp && prop.symName === selectedProp.symName) {
        prop.selected = true;
        sProp = propArr[propArr.length - 1];
      }

    });
    if (!propArr[propArr.length - 1]) {
      return;
    }
    if (this.selectedSearch && selectedProp) {
      // console.log("prp arr " + JSON.stringify(propArr));
      this.dynamicProps.push({
        options: propArr, selectedOption: sProp.value,
        mvalues: selectedProp.mvalues
      });

    }
    else {
      this.dynamicProps.push({options: propArr, selectedOption: propArr[0].value, mvalues: []});


    }


    if (!this.selectedSearch || !selectedProp) {
      this.data.searchTemplate.props.map((prop, k) => {
        if (prop.symName === propArr[0].value.symName) {
          prop.selected = true;
        }
      });
    }


    this.updatePropOptions();
  }

  clearSearch() {
    if (this.isSimpleSearch) {
      if (this.data.model.contentSearch.mvalues[0]) {
        this.data.model.contentSearch.mvalues = [];
      }
    }
    else {
      this.dynamicProps.map(d => {
        d.mvalues = [];
      });
      this.data.model.contentSearch.mvalues = [];
    }

  }

  addAllProperties(selectedProp?) {
    let propArr = [];
    let sProp;
    this.data.searchTemplate.props.map((prop, index) => {
      if (!prop.selected && prop.hidden.toLowerCase() === 'false' && prop.dtype.toLowerCase() !== 'object') {
        propArr = [{
          label: prop.desc.length > 0 ? prop.desc : prop.symName, value: {
            dtype: prop.dtype, symName: prop.symName,
            lookups: prop.lookups
          }
        }];
        prop.selected = true;
        this.dynamicProps.push({options: propArr, selectedOption: propArr[0].value, mvalues: []});
      }
    });
    if (!propArr[propArr.length - 1]) {
      return;
    }
    if (this.documentService.savedSearch.advanceSearchSaved) {
      this.documentService.savedSearch.advanceSearchSaved.map((d, i) => {
        this.dynamicProps[i].mvalues = d.mvalues;

      })
    }
    this.updatePropOptions();
  }


  updatePropOptions() {
    this.dynamicProps.map((dynamicProp, i) => {
      dynamicProp.options.map((option, j) => {

        this.data.searchTemplate.props.map((prop, k) => {
          if (!prop.selected && prop.hidden.toLowerCase() === 'false' && prop.dtype.toLowerCase() !== 'object') {
            if (dynamicProp.options.map(opt => opt.value.symName).indexOf(prop.symName) === -1) {
              dynamicProp.options.push({
                label: prop.desc.length > 0 ? prop.desc : prop.symName,
                value: {dtype: prop.dtype, symName: prop.symName}
              });
            }
          } else if (prop.symName !== dynamicProp.selectedOption.symName) {
            if (dynamicProp.options.map(opt => opt.value.symName).indexOf(prop.symName) !== -1) {
              dynamicProp.options.splice(dynamicProp.options.map(opt => opt.value.symName).indexOf(prop.symName), k);
            }


          }
        })


      })
    })
  }

  propChanged(index) {
    this.dynamicProps[index].mvalues = [];
    this.setSelection();
    this.updatePropOptions();
  }

  setSelection() {
    this.data.searchTemplate.props.map((prop, k) => {
      if (this.dynamicProps.map(dProp => dProp.selectedOption.symName).indexOf(prop.symName) === -1) {
        prop.selected = false;
      } else {
        prop.selected = true;
      }

    });
  }

  removeProp(index) {
    this.dynamicProps.splice(index, 1);
    this.setSelection();
    this.updatePropOptions();

  }

  getEntryTemplateForSearch() {
    this.data.documentClasses = [];
    if (this.documentService.savedSearch.documentClassSaved) {
      this.data.documentClasses = this.documentService.savedSearch.et;
      this.data.model.selectedDocumentClass = this.documentService.savedSearch.documentClassSaved;
      this.getEntryTemplateForSearchId(this.data.model.selectedDocumentClass);
    }
    else {
      const subscription = this.ws.getEntryTemplatesForSearch().subscribe(data => {
        if (data.length > 0) {
          data.map((d) => {
            this.data.documentClasses.push({value: d.id, label: d.symName});
          });
          this.data.model.selectedDocumentClass = data[0].id;
          if (data[0]) {
            this.getEntryTemplateForSearchId(data[0].id);
          }
        }
      }, err => {

      });
      this.coreService.progress = {busy: subscription, message: ''};
      this.addToSubscriptions(subscription);
    }
  }

  getEntryTemplateForSearchId(id) {
    this.dynamicProps = [];
    // if(!this.isClassChange && this.documentService.savedSearch.ets.props && !this.selectedSearch){
    //   this.data.searchTemplate =this.documentService.savedSearch.ets;
    //    if (this.data.model.contentSearch.mvalues[0] && this.data.model.contentSearch.oper
    //       && this.isSimpleSearch) {
    //       this.searchDocument();
    //     }
    //      this.addAllProperties();
    // }
    // else {
    const subscription = this.ws.getEntryTemplateForSearchId(id).subscribe(data => {
      this.data.searchTemplate = data;
      if (this.selectedSearch) {
        this.data.searchTemplate.props.map((p, i) => {
          const index = this.selectedSearch.searchTemplate.props.map(dProp => dProp.symName).indexOf(p.symName);
          if (index !== -1) {
            const elm = this.selectedSearch.searchTemplate.props[index];
            if (elm.mvalues[0]) {
              p.mvalues = [];
              if (elm.dtype.toLowerCase() === 'date') {
                p.mvalues[0] = new Date(elm.mvalues[0]);
              } else {
                p.mvalues[0] = elm.mvalues[0];
              }
            }
            if (elm.mvalues[1]) {
              if (elm.dtype.toLowerCase() === 'date') {
                p.mvalues[1] = new Date(elm.mvalues[1]);
              } else {
                p.mvalues[1] = elm.mvalues[1];
              }
            }
            this.addDynamicProp(p);
          }
        })


      } else {
        //this.addDynamicProp();
        this.addAllProperties();
      }
      //console.log("data "+JSON.stringify(data));


      if (this.data.model.contentSearch.mvalues[0] && this.data.model.contentSearch.oper
        && this.isSimpleSearch) {
        console.log("search being called from entrytemplata search");
        this.searchDocument();
      }
    }, err => {

    });
    this.coreService.progress = {busy: subscription, message: ''};
    this.addToSubscriptions(subscription);
    // }
  }

  switchDocumentClass() {
    this.selectedSearch = undefined;
    this.data.model.contentSearch.mvalues = [];
    this.dynamicProps = [];
    this.isClassChange = true;
    this.documentService.savedSearch.advanceSearchSaved = [];
    this.getEntryTemplateForSearchId(this.data.model.selectedDocumentClass);

  }

  fromDateChanged(dynamicProp) {
    const d = new Date(dynamicProp.mvalues[0]);
    d.setDate(d.getDate());
    dynamicProp.minDate = d;

  }

  mapDynamicPropToSearchTemplate(saveForSearch) {
    let req: any;
    if (this.isSimpleSearch) {
      req = Object.assign({}, this.data.model);
    } else {
      req = Object.assign({}, {contentSearch: this.data.model.contentSearch});
      if (!this.data.model.contentSearch.mvalues[0]) {
        req.contentSearch.mvalues[0] = "";
      }
      req.id = this.data.searchTemplate.id;
      req.name = this.data.searchTemplate.name;
      req.symName = this.data.searchTemplate.symName;
      req.vsid = this.data.searchTemplate.vsid;
      req.type = this.data.searchTemplate.type;
      req.props = [];
      this.data.searchTemplate.props.map((p, i) => {
        if (saveForSearch) {
          req.props[i] = {dtype: p.dtype, symName: p.symName};
        } else {
          req.props[i] = {dtype: 'STRING', symName: p.symName};
        }

        const index = this.dynamicProps.map(dProp => dProp.selectedOption.symName).indexOf(p.symName);
        req.props[i].mvalues = [];
        //console.log("index "+index+" prop "+p.symName);
        if (index !== -1) {
          if (this.dynamicProps[index].mvalues[0]) {
            //console.log("found mvalues");
            if (p.dtype === 'DATE' && !saveForSearch) {
              const nDate = new Date(this.dynamicProps[index].mvalues[0]);
              nDate.setHours(0, 0, 0);
              nDate.setHours(nDate.getHours() - 3);
              req.props[i].mvalues[0] = this.coreService.formatDateForSearch(nDate);
              req.props[i].oper = "between";
            } else {
              //console.log("found mvalues string");
              req.props[i].mvalues[0] = this.dynamicProps[index].mvalues[0];
            }

          }
          if (this.dynamicProps[index].mvalues[1]) {
            if (p.dtype === 'DATE' && !saveForSearch) {
              const nDate = new Date(this.dynamicProps[index].mvalues[1]);
              nDate.setHours(23, 59, 0);
              nDate.setHours(nDate.getHours() - 3);
              req.props[i].mvalues[1] = this.coreService.formatDateForSearch(nDate);
              req.props[i].oper = "between";
            } else {
              req.props[i].mvalues[1] = this.dynamicProps[index].mvalues[1];
            }
          }
          if (!this.dynamicProps[index].mvalues[0] && p.dtype === 'DATE' && !saveForSearch) {
            req.props[i].mvalues[0] = undefined;
            req.props[i].mvalues[1] = undefined;
          }
        } else {
          req.props[i].mvalues[0] = null;
        }

      });


    }
    return req;

  }

  searchDocument(flag?) {
    if (!this.isSimpleSearch) {
      let noFilter = true;
      if (this.data.model.contentSearch.mvalues[0]) {
        noFilter = false;
      }
      if(this.dynamicProps){
       this.dynamicProps.map((p, i) => {
        if (p.mvalues[0]) {
          noFilter = false;
        }
      });
      }

      if (noFilter) {
        if (flag === 'isbutton') {
          this.growlService.showGrowl({
            severity: 'error',
            summary: 'Invalid Search', detail: 'Fill atleast one field'
          });
        }

        return;
      }
      if (flag === 'isbutton') {
        console.log("advanced search being called");
        const req = this.mapDynamicPropToSearchTemplate(false);
        this.documentService.savedSearch.advanceSearchSaved = req.props;
        req.pageSize = this.data.pageSize?this.data.pageSize:50;
        const subscriptions = this.documentService.searchDocuments(req).subscribe(data => {
          console.log(data);
          this.data.continueData = data.continueData;
          this.data.totalResults = data.totalResults;
          if(data.row.length>0){
            data.row.map(d => {
              d.name = d.props[0].mvalues[0];
              d.addOn2 = this.coreService.convertToTimeInbox(d.addOn);
              d.modOn2 = this.coreService.convertToTimeInbox(d.modOn);
            });
          }
          this.data.searchResult = data.row;
          this.documentService.savedSearch.searchResultsSaved = data;
          this.onSearchComplete.emit();

        }, err => {
          let errortext = 'Error in search';
          let summaryText = 'Error';
          if (err.status == 500) {
            err.error.slice(0, err.error.lastIndexOf(':') + 1);
            errortext = err.error.slice(err.error.lastIndexOf(':') + 1);
            summaryText = 'Too Many Results!';
          }
          this.growlService.showGrowl({
            severity: 'error',
            summary: summaryText, detail: errortext
          });
        });

        this.coreService.progress = {busy: subscriptions, message: ''};
        this.addToSubscriptions(subscriptions);
      }

    }
    else {
      console.log("simple search being called");
      const req = this.mapDynamicPropToSearchTemplate(false);
      req.pageSize = this.data.pageSize?this.data.pageSize:50;
      const subscriptions = this.documentService.searchDocuments(req).subscribe(data => {
          console.log(data);
          this.data.continueData = data.continueData;
          this.data.totalResults = data.totalResults;
          if(data.row.length>0){
            data.row.map(d => {
              d.name = d.props[0].mvalues[0];
              d.addOn2 = this.coreService.convertToTimeInbox(d.addOn);
              d.modOn2 = this.coreService.convertToTimeInbox(d.modOn);
            });
          }
          this.data.searchResult = data.row;
          this.documentService.savedSearch.searchResultsSaved = data;
          this.onSearchComplete.emit();
      }, err => {
        let errortext = 'Error in search';
        let summaryText = 'Error';
        if (err.status == 500) {
          err.error.slice(0, err.error.lastIndexOf(':') + 1);
          errortext = err.error.slice(err.error.lastIndexOf(':') + 1);
          summaryText = 'Too Many Results!';
        }
        this.growlService.showGrowl({
          severity: 'error',
          summary: summaryText, detail: errortext
        });
      });
      this.coreService.progress = {busy: subscriptions, message: ''};
      this.addToSubscriptions(subscriptions);
    }
  }

  getSavedSearches(init) {
    const tmpTreeData = [];
    const subscription = this.userService.getUserSearches().subscribe(settings => {
      settings.map(s => {
        if (s.key === 'Saved Searches') {
          this.savedSearches = JSON.parse(s.val);
          if (this.selectedSearch) {
            this.savedSearches.map(search => {
              if (search.name === this.selectedSearch.name) {
                this.selectedSearch = search;
              }
            })
          }
        }
      });
      if (!init && !this.selectedSearch && this.savedSearches.length > 0) {
        if (settings.length > 0) {
          this.selectedSearch = this.savedSearches[this.savedSearches.length - 1];
        }
      }
    });
    this.coreService.progress = {busy: subscription, message: ''};
    this.addToSubscriptions(subscription);
  }

  removeSearch(search) {
    this.savedSearches.map((s, i) => {
      if (s === search) {
        this.savedSearches.splice(i, 1);
      }
    });
    if (this.selectedSearch && this.selectedSearch === search) {
      this.selectedSearch = undefined;
      this.getEntryTemplateForSearchId(this.data.documentClasses[0].value);
    }

    this.updateUserSearchesMethod(this.savedSearches, true);
  }

  resetToDefault() {
    this.selectedSearch = undefined;
    this.getEntryTemplateForSearchId(this.data.documentClasses[0].value);
  }

  selectSearch(search) {
    this.selectedSearch = search;
    this.data.model = {
      contentSearch: {
        oper: this.selectedSearch.model.contentSearch.oper,
        mvalues: Object.assign([], this.selectedSearch.model.contentSearch.mvalues)
      }
    };
    this.data.model.selectedDocumentClass = this.selectedSearch.model.selectedDocumentClass;
    this.dynamicProps = [];
    this.getEntryTemplateForSearchId(this.data.model.selectedDocumentClass);
  }

  initSaveSearch() {
    // if (this.savedSearches && this.savedSearches.length === 3) {
    //   this.growlService.showGrowl({
    //     severity: 'error',
    //     summary: 'Fill Required', detail: 'A User Can Save Maximum Three Search'
    //   });
    //   return;
    // }
    this.searchAlreadyExists = false;
    if (this.selectedSearch) {
      this.saveSearchObj.name = this.selectedSearch.name;
      this.saveSearch();
    } else {
      this.showSaveSearchModal = true;
    }
  }

  saveSearch() {
    this.searchAlreadyExists = false;
    if (!this.selectedSearch) {
      this.savedSearches.map(search => {
        if (this.saveSearchObj.name === search.name) {
          this.searchAlreadyExists = true;
        }
      })
    }
    if (this.searchAlreadyExists) {
      return;
    }
    const searchObj = {
      name: this.saveSearchObj.name, model: {
        contentSearch: {
          oper: this.data.model.contentSearch.oper,
          mvalues: [this.data.model.contentSearch.mvalues[0]]
        },
        selectedDocumentClass: this.data.model.selectedDocumentClass
      }, searchTemplate: {props: []}
    };
    this.data.searchTemplate.props.map((p, i) => {
      const index = this.dynamicProps.map(dProp => dProp.selectedOption.symName).indexOf(p.symName);

      if (index !== -1) {
        const tmp = {
          symName: this.dynamicProps[index].selectedOption.symName, mvalues: [], dtype:
          this.dynamicProps[index].selectedOption.dtype
        };
        if (this.dynamicProps[index].mvalues[0]) {
          tmp.mvalues[0] = this.dynamicProps[index].mvalues[0];
        }
        if (this.dynamicProps[index].mvalues[1]) {
          tmp.mvalues[1] = this.dynamicProps[index].mvalues[1];
        }

        searchObj.searchTemplate.props.push(tmp);

      }
    });


    if (this.selectedSearch) {

      this.savedSearches.map((search, i) => {
        if (this.selectedSearch === search) {
          this.savedSearches[i] = searchObj;
        }
      });
      this.updateUserSearchesMethod(this.savedSearches, false);
    } else {
      const tmpSavedSearches = Object.assign([], this.savedSearches);
      tmpSavedSearches.push(searchObj);
      this.updateUserSearchesMethod(tmpSavedSearches, false);
    }


  }

  updateUserSearchesMethod(tmpSavedSearches, init) {
    let subscription = this.userService.getUserSearches().subscribe(settings => {
      let exists = false;
      settings.map(s => {
        if (s.key === 'Saved Searches') {
          s.val = JSON.stringify(tmpSavedSearches);
          exists = true;
        }
      });
      if (!exists) {
        settings.push({
          appId: 'ECM',
          empNo: this.user.EmpNo,
          key: 'Saved Searches',
          val: JSON.stringify(tmpSavedSearches)
        })
      }
      subscription = this.userService.updateUserSearches(settings).subscribe(res => {
        this.savedSearches = tmpSavedSearches;
        this.growlService.showGrowl({
          severity: 'info',
          summary: 'Success', detail: this.selectedSearch ? 'Search Updated Successfully' : 'Search Saved Successfully'
        });
        this.getSavedSearches(init);
        this.showSaveSearchModal = false;

      }, err => {
        console.log(err);
        if ((err.error).includes('value too large for column')) {
          this.growlService.showGrowl({
            severity: 'error',
            summary: 'Cant Save', detail: 'Maximum limit reached'
          });
        }
      });
      this.coreService.progress = {busy: subscription, message: ''};
      this.addToSubscriptions(subscription);


    });
    this.coreService.progress = {busy: subscription, message: ''};
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

  ngOnDestroy() {
    this.clearSubscriptions();
    this.documentService.savedSearch.et = this.data.documentClasses;
    this.documentService.savedSearch.ets = this.data.searchTemplate;
    this.documentService.savedSearch.advanceSearchSaved = this.dynamicProps;
    this.documentService.savedSearch.documentClassSaved = this.data.model.selectedDocumentClass;
    this.isClassChange = false;
    //this.documentService.savedSearch.simpleSearchText=this.data.model.contentSearch.mvalues[0];

  }
}


