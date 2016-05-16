(function () {
    'use strict';

    angular.module('moaClientApp')
        .controller('AdvancedSearchController', AdvancedSearchController);

    /* @ngInject */
    function AdvancedSearchController($scope, $log) {

        initForm();
        function initForm() {
            $scope.query ={};
            $scope.selectedTags = {};
            $scope.metadataQuery = [];
        }

        $scope.submitTest = function () {
            console.log(typeof($scope.metadataQuery));
        };

    }
})();
