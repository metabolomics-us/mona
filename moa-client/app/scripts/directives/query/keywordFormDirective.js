(function () {
    'use strict';

    angular.module('moaClientApp')
        .directive('keywordForm', keywordForm);

    function keywordForm() {
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
