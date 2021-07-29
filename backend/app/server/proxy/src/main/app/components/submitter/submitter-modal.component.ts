/**
 * Created by Gert on 5/28/2014.
 */

import {Submitter} from "../../services/persistence/submitter.resource";
import {NgbActiveModal} from "@ng-bootstrap/ng-bootstrap";
import {NewSubmitter} from "../../mocks/newSubmitter.model";
import {Component, Inject, EventEmitter, Input} from "@angular/core";
import {downgradeComponent} from "@angular/upgrade/static";
import * as angular from 'angular';
import {first} from "rxjs/operators";

@Component({
    selector: 'submitter-modal',
    templateUrl: '../../views/submitters/dialog/createDialog.html'
})
export class SubmitterModalComponent {
    private newSubmitter;
    private formErrors;
    @Input() public new: boolean;
    public submitterFormStatus: boolean;

    constructor(@Inject(Submitter) private submitterResource: Submitter, @Inject(NgbActiveModal) private activeModal: NgbActiveModal){}

    /**
     * cancels any dialog in this controller
     */
    cancelDialog = () => {
        this.activeModal.dismiss('cancel');
    };

    /**
     * takes care of updates
     */
    updateSubmitter = () => {
        let submitter = this.createSubmitterFromScope();

        //update the submitter
        this.submitterResource.update(submitter).pipe(first()).subscribe((data) => {
            this.activeModal.close(submitter);
        }, (error) => {
            this.handleDialogError(error);
        });
    };

    /**
     * takes care of creates
     */
    createNewSubmitter = () => {
        let submitter = this.createSubmitterFromScope();

        //no submitter id so create a new one
        this.submitterResource.update(submitter).pipe(first()).subscribe((savedSubmitter) => {
            this.activeModal.close(savedSubmitter);
        }, (error) => {
            this.handleDialogError(error);
        });
    };

    /**
     * creates our submitter object
     */
     createSubmitterFromScope = () => {
        //build our object
        let submitter = new NewSubmitter();
        submitter.firstName = this.newSubmitter.firstName;
        submitter.lastName = this.newSubmitter.lastName;
        submitter.institution = this.newSubmitter.institution;
        submitter.emailAddress = this.newSubmitter.emailAddress;
        submitter.password = this.newSubmitter.password;

        if (this.newSubmitter.id) {
            submitter.id = this.newSubmitter.id;
        }

        return submitter;
    }

    /**
     * handles our dialog errors
     * @param error
     */
     handleDialogError = (error) => {
        let errorReport = [];

        if (error.data) {
            for (let i = 0; i < error.data.errors.length; i++) {
                let obj = error.data.errors[i];

                //remove the none needed object
                delete obj.object;
                errorReport.push(obj);
            }

            this.formErrors = errorReport;
        }
        else {
            this.formErrors = "we had an unexpected error, please check the JS console";
        }
    }

}

angular.module('moaClientApp')
    .directive('submitterModal', downgradeComponent({
        component: SubmitterModalComponent
    }));

