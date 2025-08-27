/**
 * Created by Gert on 5/28/2014.
 * Updated by nolanguzman on 10/31/2021
 */

import {Submitter} from '../../services/persistence/submitter.resource';
import {AuthenticationService} from '../../services/authentication.service';
import {NgbModal} from '@ng-bootstrap/ng-bootstrap';
import {Component, OnInit} from '@angular/core';
import {first} from 'rxjs/operators';
import {SubmitterModalComponent} from './submitter-modal.component';
import {faEdit, faMinusSquare, faUser} from '@fortawesome/free-solid-svg-icons';

@Component({
    selector: 'submitter',
    templateUrl: '../../views/submitters/list.html'
})
export class SubmitterComponent implements OnInit{
    submitters;
    listSubmitter;
    pendingUser;
    showDeletionConfirmation = false;
    faEdit = faEdit;
    faMinusSquare = faMinusSquare;
    faUser = faUser;

    constructor( public submitter: Submitter,  public modal: NgbModal,
                 public auth: AuthenticationService){}

    ngOnInit() {
        /**
         * Try loading submitters
         * This fixed the refresh bug where submitters were not being loaded when you refresh the page
         */
        this.submitters = [];

        const tryLoad = () => {
          const user = this.auth.getCurrentUser?.();
          if (user?.accessToken && this.auth.isAdmin()) {
            clearInterval(timer);
            this.list();
          }
        };
        const timer = setInterval(tryLoad, 200);
        tryLoad(); // attempt immediately as well
    }

    /**
     * Prompts toast to confirm deletion of user
     */
    askToDelete(submitter) {
      this.showDeletionConfirmation = true;
      this.pendingUser = submitter;
    }

    /**
     * Deletes user
     */
    confirmUserDelete() {
      if (!this.pendingUser) { return; }

      const index = this.submitters.indexOf(this.pendingUser);
      if (index > -1) {
        this.remove(index);
      }

      this.cancelUserDelete();
    }

    /**
     * Cancels user deletion toast
     */
    cancelUserDelete() {
      this.showDeletionConfirmation = false;
      this.pendingUser = null;
    }

    /**
     * deletes our submitter from the system
     * @param index unique ID of submitter to delete
     */
    remove(index) {
        const submitterToRemove = this.submitters[index];
        const token = this.auth.getCurrentUser().accessToken;

        this.submitter.delete({id: submitterToRemove.id}, token).pipe(first()).subscribe(
            () => {
                // remove it from the scope and update our table
                this.submitters.splice(index, 1);
            },
            () => {
                alert('Error Has Occurred while removing submitter');
            }
        );
    }

    /**
     * displays our dialog to create a new submitter
     */
    displayCreateDialog() {
        const modalInstance = this.modal.open(SubmitterModalComponent);
        modalInstance.componentInstance.new = true;
        modalInstance.componentInstance.submitter = undefined;

        // refresh the results after finish
        modalInstance.result.then(() => {
            this.list();
        });
    }

    /**
     * displays the edit dialog for the select submitter
     */
    displayEditDialog(sub) {
        const modalInstance = this.modal.open(SubmitterModalComponent, {
            size: 'lg',
            backdrop: 'static',
        });
        modalInstance.componentInstance.new = false;
        modalInstance.componentInstance.submitter = sub;

        // refresh the results after finish
        modalInstance.result.then(() => {
          this.list();
        });
    }

    /**
     * helper function
     */
     list() {
        this.submitter.get(this.auth.getCurrentUser().accessToken).pipe(first()).subscribe((data) => {
            this.submitters = data;
        }, (error) => {
            alert('failed: ' + error);
        });
    }
}
