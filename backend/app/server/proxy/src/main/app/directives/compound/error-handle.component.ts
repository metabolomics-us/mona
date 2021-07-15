import {Component, Inject} from "@angular/core";
import {downgradeComponent} from "@angular/upgrade/static";
import * as angular from 'angular';
import {NgbActiveModal} from "@ng-bootstrap/ng-bootstrap";

@Component({
    selector: 'ngbd-modal-error-content',
    template: `
    <div class="modal-header">
        <h3>Error generating MOL file!</h3>
        <button type="button" class="close" aria-label="Close" (click)="activeModal.dismiss('Cross click')">
            <span aria-hidden="true">&times;</span>
        </button>
    </div>
    <div class="modal-body">
        <p>MOL file is unavailable!</p>
    </div>
    <div class="modal-footer">
        <button type="button" class="btn btn-outline-dark" (click)="activeModal.close('Close click')">Close</button>
    </div>
    `
})
export class ErrorHandleComponent {
    constructor(@Inject(NgbActiveModal) public activeModal: NgbActiveModal) {
    }
}

angular.module('moaClientApp')
    .directive('ngbd-modal-error-content', downgradeComponent({
        component: ErrorHandleComponent
    }));
