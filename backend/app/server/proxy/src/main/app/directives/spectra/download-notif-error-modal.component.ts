import {Component, Inject, Input} from "@angular/core";
import {downgradeComponent} from "@angular/upgrade/static";
import * as angular from 'angular';
import {NgbActiveModal} from "@ng-bootstrap/ng-bootstrap";

@Component({
    selector: 'ngbd-modal-download-notif-error',
    template: `
    <div class="modal-header">
        <h3>Error submitting request!</h3>
        <button type="button" class="close" aria-label="Close" (click)="activeModal.dismiss('Cross click')">
            <span aria-hidden="true">&times;</span>
        </button>
    </div>
    <div class="modal-body">
        <p>{{message}}</p>
    </div>
    <div class="modal-footer">
        <button type="button" class="btn btn-outline-dark" (click)="activeModal.close('Close click')">Close</button>
    </div>
    `
})
export class DownloadNotifErrorModalComponent {
    @Input() message;
    constructor(@Inject(NgbActiveModal) public activeModal: NgbActiveModal) {
    }
}

angular.module('moaClientApp')
    .directive('ngbd-modal-download-notif-error', downgradeComponent({
        component: DownloadNotifErrorModalComponent
    }));
