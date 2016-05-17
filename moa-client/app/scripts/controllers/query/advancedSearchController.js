(function () {
    'use strict';

    angular.module('moaClientApp')
        .controller('AdvancedSearchController', AdvancedSearchController);

    /* @ngInject */
    function AdvancedSearchController($scope, $log) {

        initForm();
        function initForm() {
            $scope.query = {};
            $scope.selectedTags = {};
            $scope.metadataQuery = [];
            $scope.classQuery = [];
        }

        $scope.submitTest = function () {
            console.log($scope.classQuery);
            $log.info($scope.metadataQuery);
        };


    }
})();
