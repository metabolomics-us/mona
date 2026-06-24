import {Component, EventEmitter, Input, Output} from '@angular/core';
import {faInfoCircle} from '@fortawesome/free-solid-svg-icons';

// Action button with a toggleable info icon that reveals help text below it
@Component({
  selector: 'admin-button',
  template: `
    <div class="row justify-content-center mb-2">
      <button class="btn btn-primary mr-2 accessibility" (click)="action.emit()">{{label}}</button>
      <fa-icon [icon]="faInfoCircle" class="clickable align-self-center davis-blue" (click)="showInfo = !showInfo" title="Toggle info"></fa-icon>
      <p class="w-100 text-center mt-2" *ngIf="showInfo">{{info}}</p>
    </div>
  `
})
export class AdminButtonComponent {
  @Input() label: string;
  @Input() info: string;
  @Output() action = new EventEmitter<void>();
  faInfoCircle = faInfoCircle;
  showInfo = false;
}
