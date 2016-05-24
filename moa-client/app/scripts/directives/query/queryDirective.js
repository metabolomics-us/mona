/**
 * Created by wohlgemuth on 11/3/14.
 */

(function() {
    'use strict';

    angular.module('moaClientApp')
      .directive('showQuery', showQuery);

    function showQuery() {
        var directive = {
            restrict: 'A',
            templateUrl: '/views/templates/showQuery.html',
            replace: true,
            link: linkFunc,
            controller: showQueryController
        };

        return directive;
    }

    //decorate our elements based on there properties
    function linkFunc($scope, element, attrs, ngModel) {

    }

    /**
     * watches for changes and is used to modify the query terms on the fly
     * @param $scope
     * @param $log
     * @param $rootScope
     */

    /* @ngInject */
    function showQueryController($scope, $log, $rootScope, SpectraQueryBuilderService, Spectrum) {
        $scope.result = [];
        $scope.status = {isOpen: false};

        var curQuery = SpectraQueryBuilderService.getQuery();

        $scope.query = curQuery === '' ? '/rest/spectra' : curQuery;

        $scope.$on('spectra:query', function(event, data) {
            $scope.query = data;
        });

        $scope.$on('spectra:loaded', function(event, data) {
            $scope.result = data;
        });


        $scope.$on('spectra:query:show', function(event, data) {
            $scope.status.isOpen = !$scope.status.isOpen;
        });

        $scope.curateSpectra = function() {
            Spectrum.curateSpectraByQuery($scope.query, function(data) {
            });
        };

        $scope.associateSpectra = function() {
            Spectrum.associateSpectraByQuery($scope.query, function(data) {
            });
        };
    }
})();
