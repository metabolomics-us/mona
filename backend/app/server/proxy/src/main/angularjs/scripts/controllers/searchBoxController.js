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

            var path = '/';
            searchBoxQuery = searchBoxQuery.replace(/^\s\s*/, '').replace(/\s\s*$/, '');

            // Handle InChIKey
            if (/^([A-Z]{14}-[A-Z]{10}-[A-Z,0-9])+$/.test(searchBoxQuery)) {
                SpectraQueryBuilderService.compileQuery({inchiFilter: searchBoxQuery});
                path = '/spectra/browse';
            }

            // Splash search
            else if (/^(splash[0-9]{2}-[a-z0-9]{10}-[a-z0-9]{20})$/.test(searchBoxQuery)) {
                path = '/spectra/splash/'+ searchBoxQuery;
            }

            // Handle MoNA ID
            else if (/^[0-9]+$/.test(searchBoxQuery)) {
                path = '/spectra/display/'+ searchBoxQuery;
            }

            // Handle MoNA hash
            else if (searchBoxQuery.indexOf('mona-') === 0) {
                SpectraQueryBuilderService.prepareQuery();
                SpectraQueryBuilderService.addSpectraIdToQuery(searchBoxQuery);
                path = '/spectra/browse';
            }

            // Handle name query
            else {
                SpectraQueryBuilderService.compileQuery({nameFilter: searchBoxQuery});
                path = '/spectra/browse';
            }

            // Update view
            if ($location.path() === path) {
                $route.reload();
            } else {
                $location.path(path);
            }
        };

    }
})();
