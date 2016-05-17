(function () {
    'use strict';

    angular.module('moaClientApp')
        .controller('AdvancedSearchController', AdvancedSearchController);

    /* @ngInject */
    function AdvancedSearchController($scope, $log, rsqlService, $http) {

        initForm();
        function initForm() {
            $scope.query = {};
            $scope.selectedTags = {};
            $scope.metadataQuery = [];
        }

        $scope.searchIn = [{name: 'compound'}, {name: 'spectra'}];

        $scope.submitAdvQuery = function () {
            $log.info($scope.metadataQuery);
            $log.info($scope.searchIn);
        };


    }
})();
