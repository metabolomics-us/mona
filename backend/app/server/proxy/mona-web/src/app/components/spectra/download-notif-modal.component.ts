import {Component} from '@angular/core';
import {NgbActiveModal} from '@ng-bootstrap/ng-bootstrap';

@Component({
    selector: 'ngbd-modal-download-notif',
    template: `
    <div class="modal-header">
        <h3>Export request successful! </h3>
        <button type="button" class="close" aria-label="Close" (click)="activeModal.dismiss('Cross click')">
            <span aria-hidden="true">&times;</span>
        </button>
    </div>
    <div class="modal-body">
        <p>Your query export request has been submitted.
            You will receive an email with a download link when the export has been completed.
            This can take up to 24 hours for very large queries</p>
    </div>
    <div class="modal-footer">
        <button type="button" class="btn btn-outline-dark" (click)="activeModal.close('Close click')">Close</button>
    </div>
    `
})
export class DownloadNotifModalComponent {
    constructor(public activeModal: NgbActiveModal) {
    }
}
