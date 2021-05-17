/*
 * Component to render our Browse drop down menu
 */

import * as angular from 'angular';

    browseController.$inject = ['$scope', 'SpectraQueryBuilderService'];
    angular.module('moaClientApp')
        .directive('browseDropDown', browseDropDown);

    function browseDropDown() {
        return {
            restrict: 'E',
            replace: true,
            templateUrl: '/views/navbar/browseDropdown.html',
            controller: browseController
        };
    }

    /* @ngInject */
    function browseController($scope, SpectraQueryBuilderService) {
        
        // Reset query when user click browse
        $scope.resetQuery = function() {
            SpectraQueryBuilderService.prepareQuery();
        }
    }
