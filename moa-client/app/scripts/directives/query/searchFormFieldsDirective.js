(function () {
    'use strict';

    angular.module('moaClientApp')
        .directive('searchFormFields', searchFormFields);

    function searchFormFields() {
        var directive = {
            restrict: 'E',
            templateUrl: 'views/spectra/query/searchForm.html',
            link: linkFunc
        };
        function linkFunc(scope,elem,attrs) {
            //TODO: update DOM on search result data
        }
        return directive;
    }

})();
