/**
 * Created by Gert on 5/28/2014.
 */

import * as angular from 'angular';
import SubmitterModalController from "./submitter-modal.component";

class SubmitterController{
    private static $inject = ['$scope', 'Submitter', '$uibModal'];
    private $scope;
    private Submitter;
    private $uibModal;
    private submitters;
    private listSubmitter;

    constructor($scope, Submitter, $uibModal){
        this.$scope = $scope;
        this.Submitter = Submitter;
        this.$uibModal = $uibModal;
    }

    $onInit = () => {
        /**
         * contains all local objects
         * @type {Array}
         */
        this.submitters = [];

        /**
         * list all our submitters in the system
         */
        this.listSubmitter = this.list();
    }

    /**
     * deletes our submitter from the system
     * @param submitterId
     */
    remove = (index) => {
        let submitterToRemove = this.submitters[index];

        this.Submitter.delete({id: submitterToRemove.id},
            (data) => {
                //remove it from the scope and update our table
                this.submitters.splice(index, 1);
            },
            (errors) => {
                alert('oh noes an error...');
            }
        );
    };

    /**
     * displays our dialog to create a new submitter
     */
    displayCreateDialog = () => {
        let modalInstance = this.$uibModal.open({
            templateUrl: '../../views/submitters/dialog/createDialog.html',
            /* @ngInject */
            controller: SubmitterModalController,
            size: 'lg',
            backdrop: 'static',
            resolve: {
                //just an empty object
                newSubmitter: () => {
                    return {};
                }
            }
        });

        //retrieve the result from the dialog and save it
        modalInstance.result.then((submitter) => {
            //push our object to the scope now so that our table can show it
            this.submitters.push(submitter);
        })
    };

    /**
     * displays the edit dialog for the select submitter
     * @param index
     */
    displayEditDialog = (index) => {
        let modalInstance = this.$uibModal.open({
            templateUrl: '../../views/submitters/dialog/editDialog.html',
            /* @ngInject */
            controller: SubmitterModalController,
            size: 'lg',
            backdrop: 'static',
            resolve: {
                //populate the dialog with the given submitter at this index
                newSubmitter: () => {
                    return this.submitters[index];
                }
            }
        });

        //retrieve the result from the dialog and save it
        modalInstance.result.then((submitter) => {
            //will be handled automatically by angular js
        });
    };

    /**
     * helper function
     */
     list() {
        this.submitters = this.Submitter.query((data) => {
        }, (error) => {
            alert('failed: ' + error);
        });
    }
}

let SubmitterComponent = {
    selector: "submitter",
    templateUrl: "../../views/submitters/list.html",
    bindings: {},
    controller: SubmitterController
}

angular.module('moaClientApp')
    .component(SubmitterComponent.selector, SubmitterComponent);





