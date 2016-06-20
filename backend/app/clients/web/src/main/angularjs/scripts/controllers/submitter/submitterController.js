/**
 * Created by Gert on 5/28/2014.
 */

(function() {
    'use strict';
    angular.module('moaClientApp')
        .controller('SubmitterController', SubmitterController);

    /* @ngInject */
    function SubmitterController($scope, Submitter, $uibModal) {

        /**
         * contains all local objects
         * @type {Array}
         */
        $scope.submitters = [];

        /**
         * list all our submitters in the system
         */
        $scope.listSubmitter = list();

        /**
         * deletes our submitter from the system
         * @param submitterId
         */
        $scope.remove = function(index) {
            var submitterToRemove = $scope.submitters[index];

            Submitter.delete({id: submitterToRemove.id},
                function(data) {
                    //remove it from the scope and update our table
                    $scope.submitters.splice(index, 1);
                },
                function(errors) {
                    alert('oh noes an error...');
                }
            );
        };

        /**
         * displays our dialog to create a new submitter
         */
        $scope.displayCreateDialog = function() {
            var modalInstance = $uibModal.open({
                templateUrl: '/views/submitters/dialog/createDialog.html',
                /* @ngInject */
                controller: 'SubmitterModalController',
                size: 'lg',
                backdrop: 'static',
                resolve: {
                    //just an empty object
                    newSubmitter: function() {
                        return {};
                    }
                }
            });

            //retrieve the result from the dialog and save it
            modalInstance.result.then(function(submitter) {
                //push our object to the scope now so that our table can show it
                $scope.submitters.push(submitter);
            })
        };

        /**
         * displays the edit dialog for the select submitter
         * @param index
         */
        $scope.displayEditDialog = function(index) {
            var modalInstance = $uibModal.open({
                templateUrl: '/views/submitters/dialog/editDialog.html',
                /* @ngInject */
                controller: 'SubmitterModalController',
                size: 'lg',
                backdrop: 'static',
                resolve: {
                    //populate the dialog with the given submitter at this index
                    newSubmitter: function() {
                        return $scope.submitters[index];
                    }
                }
            });

            //retrieve the result from the dialog and save it
            modalInstance.result.then(function(submitter) {
                //will be handled automatically by angular js
            });
        };

        /**
         * helper function
         */
        function list() {
            $scope.submitters = Submitter.query(function(data) {
            }, function(error) {
                alert('failed: ' + error);
            });
        }
    }
})();
