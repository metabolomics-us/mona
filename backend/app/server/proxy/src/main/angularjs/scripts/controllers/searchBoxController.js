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
                path = '/spectra/browse?query=compound.inchiKey=='+ searchBoxQuery;
            }

            // Handle SPLASH
            else if (/^(splash[0-9]{2}-[a-z0-9]{4}-[0-9]{10}-[a-z0-9]{20})$/.test(searchBoxQuery)) {
                path = '/spectra/browse?query=splash.splash=='+ searchBoxQuery;
            }

            // Handle name query
            else {
                path = '/spectra/browse?query=compound.names=q=\'name=match=".*'+ searchBoxQuery +'.*"\'';
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
