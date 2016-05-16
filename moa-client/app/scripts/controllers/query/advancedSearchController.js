(function () {
    'use strict';

    angular.module('moaClientApp')
        .controller('AdvancedSearchController', AdvancedSearchController);

    /* @ngInject */
    function AdvancedSearchController($scope, $log) {

        initForm();
        function initForm() {
            $scope.selectedTags = {};
            $scope.metadataQuery = [];
        }
        $scope.submitAdvancedQuery = function() {
            $log.info($scope.metadataQuery);
        }

    }
})();
