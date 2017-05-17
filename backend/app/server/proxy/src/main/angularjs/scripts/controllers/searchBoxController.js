/**
 * Created by sajjan on 5/12/15.
 */

(function() {
    'use strict';
    searchBoxController.$inject = ['$scope', '$location', '$route', 'SpectraQueryBuilderService'];
    angular.module('moaClientApp')
        .controller('SearchBoxController', searchBoxController);

    /* @ngInject */
    function searchBoxController($scope, $location, $route, SpectraQueryBuilderService) {
        $scope.inputError = false;


        $scope.performSimpleQuery = function(searchBoxQuery) {
            // Handle empty query
            if (angular.isUndefined(searchBoxQuery) || searchBoxQuery === '') {
                return;
            }

            searchBoxQuery = searchBoxQuery.replace(/^\s\s*/, '').replace(/\s\s*$/, '');
            SpectraQueryBuilderService.prepareQuery();

            // Handle InChIKey
            if (/^[A-Z]{14}-[A-Z]{10}-[A-Z]$/.test(searchBoxQuery)) {
                SpectraQueryBuilderService.addCompoundMetaDataToQuery('InChIKey', searchBoxQuery);
            }

            else if (/^[A-Z]{14}$/.test(searchBoxQuery)) {
                SpectraQueryBuilderService.addCompoundMetaDataToQuery('InChIKey', searchBoxQuery, true);
            }

            // Handle SPLASH
            else if (/^splash[0-9]{2}/.test(searchBoxQuery)) {
                SpectraQueryBuilderService.addSplashToQuery(searchBoxQuery);
            }

            // Handle full text search
            else {
                SpectraQueryBuilderService.addNameToQuery(searchBoxQuery);
            }

            SpectraQueryBuilderService.executeQuery();
        };
    }
})();
