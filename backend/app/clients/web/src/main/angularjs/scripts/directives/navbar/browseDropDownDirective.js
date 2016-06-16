/*
 * Component to render our Browse drop down menu
 */

(function() {
    'use strict';

    angular.module('moaClientApp')
      .directive('browseDropDown', browseDropDown);

    function browseDropDown() {
        var directive = {
            restrict: 'E',
            replace: true,
            templateUrl: '/views/navbar/browseDropdown.html',
            controller: browseController
        };

        return directive;
        /* */
        function browseController($scope, SpectraQueryBuilderService) {

            // reset query when user click browse
            $scope.resetQuery = function() {
                console.log('test');
                SpectraQueryBuilderService.prepareQuery();
            }
        }
    }
})();
