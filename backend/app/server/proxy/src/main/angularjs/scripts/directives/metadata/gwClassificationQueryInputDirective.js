/**
 * Built this for multiple choice search on query input. Not using it now, saving until we deprecate
 *
 */

(function() {
    'use strict';

    gwClassInputController.$inject = ['$scope'];
    angular.module('moaClientApp')
        .directive('gwClassQueryInput', gwClassQueryInput);

    function gwClassQueryInput() {
        var directive = {
            restrict: 'E',
            replace: true,
            templateUrl: 'views/templates/classQueryInput.html',
            scope: {
                query: '='
            },
            controller: gwClassInputController
        };
        return directive;
    }

    /* @ngInject */
    function gwClassInputController($scope) {
        $scope.select = [
            {name: "equal", value: "eq"},
            {name: "not equal", value: "ne"}
        ];

        $scope.addClassQuery = function() {
            $scope.query.push({
                name: '',
                selected: '',
                value: ''});
        };

        // init form
        (function() {
            $scope.addClassQuery();
        })();

    }
})();
