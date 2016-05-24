(function () {
    'use strict';

    angular.module('moaClientApp')
        .controller('AdvancedSearchController', AdvancedSearchController);

    /* @ngInject */
    function AdvancedSearchController($scope, $log, $http) {

        initForm();
        $scope.queryStrings = [];

        function initForm() {
            $scope.compoundQuery = {
                metadata: [],
                operator: ['AND', 'AND', 'AND']
            };

            $scope.metadataQuery = {
                metadata: [],
                operator: 'AND'
            };
        }



        $scope.submitAdvQuery = function () {

            // compile and submit if user search for both compound and metadata
            if (isCompoundQuery() && isMetaQuery()) {
                // filter compound
                filterCompound();
                filterMetadata();


            }
            else if (isCompoundQuery) {

            }
            else if (isMetaQuery) {

            }
            else {
                // inform user query is empty
            }

        };

        function filterCompound() {
            // filter compound
            if (/^([A-Z]{14}-[A-Z]{10}-[A-Z,0-9])+$/.test($scope.compoundQuery.compound.name)) {
                $scope.compoundQuery.compound.inchiKey = $scope.compoundQuery.compound.name;
                delete $scope.compoundQuery.compound.name;
            }
            else {
                delete $scope.compoundQuery.compound.inchiKey;
            }
        }

        function filterMetadata() {
            // it's already filtered
        }

        function isMetaQuery() {
            return $scope.metadataQuery.metadata.length > 1;
        }

        function isCompoundQuery() {
            return typeof($scope.compoundQuery.name) === 'string';
        }
    }
})();
