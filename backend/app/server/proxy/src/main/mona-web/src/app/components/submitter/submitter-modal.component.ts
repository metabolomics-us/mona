/**
 * Created by Gert on 5/28/2014.
 * Updated by nolanguzman on 10/31/2021
 */

import {Submitter} from '../../services/persistence/submitter.resource';
import {NgbActiveModal} from '@ng-bootstrap/ng-bootstrap';
import {RegistrationService} from '../../services/registration.service';
import {AuthenticationService} from '../../services/authentication.service';
import {Component, Input} from '@angular/core';
import {first} from 'rxjs/operators';

@Component({
    selector: 'submitter-modal',
    templateUrl: '../../views/submitters/dialog/createDialog.html'
})
export class SubmitterModalComponent {
    public newSubmitter;
    public formErrors;
    @Input() public new: boolean;
    @Input() public submitter;
    public submitterFormStatus: boolean;

    constructor( public submitterResource: Submitter,  public activeModal: NgbActiveModal,
                 public registrationService: RegistrationService, public authenticationService: AuthenticationService){}

    /**
     * cancels any dialog in this controller
     */
    cancelDialog() {
        this.activeModal.close();
    }

    /**
     * takes care of updates
     */
    updateSubmitter() {
        const submitter = this.registrationService.newSubmitter;
        const token = this.authenticationService.getCurrentUser().accessToken;

        // update the submitter
        this.submitterResource.update(submitter, token).pipe(first()).subscribe(() => {
            this.activeModal.close();
        }, (error) => {
            this.handleDialogError(error);
        });
    }

    /**
     * takes care of creates
     */
    createNewSubmitter() {
      this.registrationService.submit().pipe(first()).subscribe(
        () => {
          this.registrationService.authorize().pipe(first()).subscribe((res: any) => {
            this.registrationService.registerAsSubmitter(res.token).pipe(first()).subscribe(
              () => {
                this.activeModal.close();
              },
              (error => {
                this.handleDialogError(error);
              }));

          }, (error => {
            this.handleDialogError(error);
          }));

        }, (error => {
          this.handleDialogError(error);
        }));
    }

    /**
     * handles our dialog errors
     * @param error error object
     */
     handleDialogError(error) {
        const errorReport = [];

        if (error.data) {
            for (let i = 0; i < error.data.errors.length; i++) {
                const obj = error.data.errors[i];

                // remove the none needed object
                delete obj.object;
                errorReport.push(obj);
            }

            this.formErrors = errorReport;
        }
        else {
            this.formErrors = 'we had an unexpected error, please check the JS console';
        }
    }

}
