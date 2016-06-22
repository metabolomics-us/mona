/*
 * Component to render our Browse drop down menu
 */

(function() {
    'use strict';

    angular.module('moaClientApp')
      .directive('browseDropDown', browseDropDown);

    function browseDropDown() {
        browseController.$inject = ['$scope', 'SpectraQueryBuilderService'];
        var directive = {
            restrict: 'E',
            replace: true,
            templateUrl: '/views/navbar/browseDropdown.html',
            controller: browseController
        };

        return directive;
        /* @ngInject */
        function browseController($scope, SpectraQueryBuilderService) {

            // reset query when user click browse
            $scope.resetQuery = function() {
                SpectraQueryBuilderService.prepareQuery();
            }
        }
    }
})();
