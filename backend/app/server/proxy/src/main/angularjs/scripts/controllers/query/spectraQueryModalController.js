/**
 * Created by wohlgemuth on 7/11/14.
 */

(function() {
    'use strict';
    QuerySpectrumModalController.$inject = ['$scope', '$uibModalInstance', 'SpectraQueryBuilderService'];
    angular.module('moaClientApp')
        .controller('QuerySpectrumModalController', QuerySpectrumModalController);

    /!* @ngInject *!/
    function QuerySpectrumModalController($scope, $uibModalInstance, SpectraQueryBuilderService) {
        /**
         * Store accordion status
         * @type {{name: boolean}}
         */
        $scope.queryAccordion = {name: true};

        /**
         * Tags selected in query window
         * @type {{}}
         */
        $scope.selectedTags = {};

        /**
         * Store all metadata query data
         * @type {{name: string, value: string}[]}
         */
        $scope.metadataQuery = [];


        /**
         * contains our build query object
         * @type {{}}
         */
        $scope.query = {};

        $scope.cancelDialog = function() {
            $uibModalInstance.dismiss('cancel');
        };

        /**
         * closes the dialog and finishes and builds the query
         */
        $scope.submitQuery = function() {

            //compile initial query
            SpectraQueryBuilderService.compileQuery($scope.query);

            //refine by metadata
            for (var i = 0, l = $scope.metadataQuery.length; i < l; i++) {
                SpectraQueryBuilderService.addMetaDataToQuery($scope.metadataQuery[i]);
            }

            //add tags to query
            for (var key in $scope.selectedTags) {
                if ($scope.selectedTags.hasOwnProperty(key) && $scope.selectedTags[key] !== false) {
                    SpectraQueryBuilderService.addTagToQuery(key, false, $scope.selectedTags[key]);
                }
            }

            //submit the final query
            $uibModalInstance.close(SpectraQueryBuilderService.getQuery());
        };
    }
})();

