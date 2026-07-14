import {Component, Input} from '@angular/core';
import {NgbActiveModal} from '@ng-bootstrap/ng-bootstrap';
import {faTrash} from '@fortawesome/free-solid-svg-icons';

/**
 * Reusable confirmation dialog for spectrum deletion. Opened via NgbModal; the returned
 * modalRef resolves on confirm (close) and rejects on cancel/dismiss, matching the styled
 * delete dialogs used elsewhere in MoNA (e.g. the uploads and submitter delete flows)
 */
@Component({
    selector: 'delete-confirm-modal',
    template: `
    <div class="modal-header">
        <h4 class="modal-title">{{title}}</h4>
        <button type="button" class="close" aria-label="Close" (click)="modal.dismiss()">
            <span aria-hidden="true">&times;</span>
        </button>
    </div>
    <div class="modal-body">
        <p style="white-space: normal;" [innerHTML]="message"></p>
    </div>
    <div class="modal-footer">
        <button type="button" class="btn btn-secondary accessibility" (click)="modal.dismiss()">Cancel</button>
        <button type="button" class="btn btn-danger accessibility" (click)="modal.close(true)"><fa-icon [icon]="faTrash"></fa-icon> Delete</button>
    </div>
  `
})
export class DeleteConfirmModalComponent {
    @Input() title = 'Delete spectrum';
    @Input() message = 'Are you sure you want to delete this spectrum?';
    faTrash = faTrash;

    constructor(public modal: NgbActiveModal) {}
}
