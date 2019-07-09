import {Component, OnInit, Input, Output, EventEmitter, OnChanges} from '@angular/core';
import {MenuItem} from 'primeng/primeng';

@Component({
  selector: 'action-button',
  templateUrl: './action-button.component.html'
})
export class ActionButtonComponent implements OnInit, OnChanges {
  @Input() public activePage: any;
  @Input() public selectedItemCount: any;
  @Input() public actions: any;
  @Input() public disableSelect: any;
  @Input() public hasFilterRes: boolean;
  @Input() public userId: number;
  @Input() public activeTab: string;
  @Input() public totalTableRecords: number;
  @Output() selectedAction = new EventEmitter();
  @Output() toggleFilter = new EventEmitter();
  @Output() clearFilter = new EventEmitter();
  public tieredItems: any[] = [];

  ngOnChanges() {
    this.tieredItems = [];
    if (this.activePage === 'sent') {
      this.tieredItems.push({
        label: 'Actions',
        items: [],
        disabled: this.disableSelect
      });
      this.tieredItems[0].disabled = this.disableSelect;
      this.tieredItems[0].items = [];
    }
    if (this.selectedItemCount >= 1 && this.activePage === 'sent') {
      this.actions.map((action, index) => {
        if (action === 'Recall') {
          this.tieredItems[0].items.push({
            label: 'Recall',
            icon: 'ui-icon-replay', command: (event) => {
              this.selectedAction.emit('Recall');
            }
          });
        } else if (action === 'Archive') {
          this.tieredItems[0].items.push({
            label: 'Archive',
            icon: 'ui-icon-archive', command: (event) => {
              this.selectedAction.emit('Archive');
            }
          });
        }
      });
    }
    if (this.activePage === 'sent') {
      this.actions.map((action, index) => {
        if (this.totalTableRecords > 0 && action === 'Archive Before') {
          this.tieredItems.push({
            label: 'Archive Before',
            icon: 'ui-icon-today', command: (event) => {
              this.selectedAction.emit('Archive Before');
            }
          });
        } else if (this.totalTableRecords <= 0 && action === 'Archive Before') {
          this.tieredItems.push({
            label: 'Archive Before',
            icon: 'ui-icon-today', command: (event) => {
              this.selectedAction.emit('Archive Before');
            },
            disabled: true
          });
        }
      });
      if (this.totalTableRecords <= 0) {
        this.tieredItems.push({
          label: 'Filter',
          icon: 'ui-icon-filter-list', command: (event) => {
            this.toggleFilter.emit('toggled');
          },
          disabled: true
        });
      } else if (this.totalTableRecords > 0) {
        this.tieredItems.push({
          label: 'Filter',
          icon: 'ui-icon-filter-list', command: (event) => {
            this.toggleFilter.emit('toggled');
          },
        });
      }
    }
    if (this.activePage === 'inbox') {
      this.actions.map((action, index) => {
        if (this.totalTableRecords > 0 && this.selectedItemCount > 0 && action === 'Finish') {
          this.tieredItems.push({
            label: 'Finish',
            icon: 'ui-icon-remove-circle', command: (event) => {
              this.selectedAction.emit('Finish');
            }
          });
        } else if ((this.totalTableRecords <= 0 || this.selectedItemCount <= 0) && action === 'Finish') {
          this.tieredItems.push({
            label: 'Finish',
            icon: 'ui-icon-remove-circle', command: (event) => {
              this.selectedAction.emit('Finish');
            },
            disabled: true
          });
        }
        if (this.totalTableRecords > 0 && action === 'Finish Before') {
          this.tieredItems.push({
            label: 'Finish Before',
            icon: 'ui-icon-today', command: (event) => {
              this.selectedAction.emit('Finish Before');
            }
          });
        } else if (this.totalTableRecords <= 0 && action === 'Finish Before') {
          this.tieredItems.push({
            label: 'Finish Before',
            icon: 'ui-icon-today', command: (event) => {
              this.selectedAction.emit('Finish Before');
            },
            disabled: true
          });
        }
      });
      if (this.totalTableRecords <= 0) {
        this.tieredItems.push({
          label: 'Filter',
          icon: 'ui-icon-filter-list', command: (event) => {
            this.toggleFilter.emit('toggled');
          },
          disabled: true
        });
      } else if (this.totalTableRecords > 0) {
        this.tieredItems.push({
          label: 'Filter',
          icon: 'ui-icon-filter-list', command: (event) => {
            this.toggleFilter.emit('toggled');
          },
        });
      }
    }
    if (this.activePage === 'archive') {
      if (this.totalTableRecords <= 0) {
        this.tieredItems.push({
          label: 'Filter',
          icon: 'ui-icon-filter-list', command: (event) => {
            this.toggleFilter.emit('toggled');
          },
          disabled: true
        });
      } else if (this.totalTableRecords > 0) {
        this.tieredItems.push({
          label: 'Filter',
          icon: 'ui-icon-filter-list', command: (event) => {
            this.toggleFilter.emit('toggled');
          },
        });
      }
    }
    if (this.activePage !== 'draft') {
      if (this.hasFilterRes) {
        this.tieredItems.push({
          label: 'Clear',
          icon: 'ui-icon-clear', command: (event) => {
            if (this.activePage === 'archive') {
              this.clearFilter.emit({'bool': true, 'id': this.userId + '@' + this.activeTab});
            } else {
              this.clearFilter.emit({'bool': true, 'id': this.userId});
            }
          }
        });
      }
    }
  }

  ngOnInit() {

  }
}
