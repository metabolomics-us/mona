/**
 * Created by Gert on 5/28/2014.
 */

import * as angular from 'angular';

export default class SubmitterModalController{
    private static $inject = ['$scope', 'Submitter'];
    private $scope;
    private Submitter;
    private modalInstance;
    private newSubmitter;
    private formErrors;

    constructor($scope, Submitter){
        this.$scope = $scope;
    }



    /**
     * cancels any dialog in this controller
     */
    cancelDialog = () => {
        this.modalInstance.dismiss('cancel');
    };

    /**
     * takes care of updates
     */
    updateSubmitter = () => {
        let submitter = this.createSubmitterFromScope();

        //update the submitter
        this.Submitter.update(submitter, function(data) {
            this.modalInstance.close(submitter);
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
        this.Submitter.save(submitter, (savedSubmitter) => {
            this.modalInstance.close(savedSubmitter);
        }, (error) => {
            this.handleDialogError(error);
        });
    };

    /**
     * creates our submitter object
     */
     createSubmitterFromScope = () => {
        //build our object
        let submitter = new this.Submitter();
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

let SubmitterModalComponent = {
    selector: "submitterModal",
    bindings: {
        modalInstance: '<',
        resolve: '<',
        close: '&',
        dismiss: '&',
    },
    controller: SubmitterModalController
}

angular.module('moaClientApp')
    .component(SubmitterModalComponent.selector, SubmitterModalComponent);

