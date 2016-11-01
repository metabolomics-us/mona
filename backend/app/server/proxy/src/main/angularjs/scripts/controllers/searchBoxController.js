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

            // Handle InChIKey
            if (/^[A-Z]{14}-[A-Z]{10}-[A-Z]$/.test(searchBoxQuery)) {
                SpectraQueryBuilderService.setQueryString("compound.metaData=q='name==\"InChIKey\" and value==\""+ searchBoxQuery +"\"'");
            }

            else if (/^[A-Z]{14}$/.test(searchBoxQuery)) {
                SpectraQueryBuilderService.setQueryString("compound.metaData=q='name==\"InChIKey\" and value=match=\""+ searchBoxQuery +"-.*\"'");
            }

            // Handle SPLASH
            else if (/^(splash[0-9]{2}-[a-z0-9]{4}-[0-9]{10}-[a-z0-9]{20})$/.test(searchBoxQuery)) {
                SpectraQueryBuilderService.setQueryString("splash.splash==" + searchBoxQuery);
            }

            else if (/^splash[0-9]{2}/.test(searchBoxQuery)) {
                SpectraQueryBuilderService.setQueryString("splash.splash=match="+ searchBoxQuery +".*");
            }

            // Handle name query
            else {
                SpectraQueryBuilderService.setQueryString("compound.names=q='name=match=\".*"+ searchBoxQuery +".*\"'");
            }

            // Update view
            if ($location.path().indexOf('/spectra/browse') > -1) {
                $route.reload();
            } else {
                $location.path('/spectra/browse');
            }
        };
    }
})();
