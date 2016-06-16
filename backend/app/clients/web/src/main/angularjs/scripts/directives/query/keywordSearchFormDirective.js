(function () {
    'use strict';

    angular.module('moaClientApp')
        .directive('keywordSearchForm', keywordSearchForm);

    function keywordSearchForm() {
        var directive = {
            restrict: 'E',
            templateUrl: 'views/spectra/query/keywordSearchForm.html',
            link: linkFunc
        };
        function linkFunc(scope,elem,attrs) {
            //TODO: update DOM on search result data
        }
        return directive;
    }

})();
