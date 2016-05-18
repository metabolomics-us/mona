(function () {
    'use strict';

    angular.module('moaClientApp')
        .controller('AdvancedSearchController', AdvancedSearchController);

    /* @ngInject */
    function AdvancedSearchController($scope, $log, rsqlService, $http) {

        initForm();
        function initForm() {
            $scope.compoundQuery = {
                metadata: [],
                operator: ['AND', 'AND', 'AND', 'AND']
            };

            $scope.metadataQuery = {
                metadata: [],
                operator: 'AND'
            }
        }

        $scope.isMetaQuery = function () {
            return $scope.metadataQuery.metadata.length > 1;
        };

        $scope.submitAdvQuery = function () {
           $log.info($scope.queryOptions);
        };


    }
})();
