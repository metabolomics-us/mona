/**
 * disables automatic form submission when you press enter in an input element
 */

(function() {
    'use strict';

    angular.module('moaClientApp')
      .directive('gwTag', gwTag);

    function gwTag() {
        var directive = {
            restrict: 'A',
            templateUrl: '/views/templates/tags.html',
            replace: true,
            scope: {
                ruleBased: '=',
                type: '@',
                tag: '=value',
                size: '@'
            },
            priority: 1001,
            link: linkFunc,
            controller: gwTagController
        };

        return directive;
    }


    //decorate our elements based on there properties
    function linkFunc(scope, element, attrs, ctrl) {
        scope.status = {
            active: false
        };
    }

    //controller to handle building new queries
    function gwTagController($scope, SpectraQueryBuilderService, $location) {
        $scope.options = [];

        if ($scope.type === 'spectrum') {
            $scope.options = [
                {
                    name: 'Create new query',
                    action: function(tag, status) {
                        //build a mona query based on this label
                        SpectraQueryBuilderService.prepareQuery();
                        SpectraQueryBuilderService.addTagToQuery(tag.text);

                        //run the query and show it's result in the spectra browser
                        $location.path("/spectra/browse/");
                    }
                },
                {
                    name: 'Add to query',
                    action: function(tag, status) {
                        SpectraQueryBuilderService.addTagToQuery(tag.text);
                        $location.path("/spectra/browse/");
                    }
                },
                {
                    name: 'Remove from query',
                    action: function(tag, status) {
                        SpectraQueryBuilderService.removeTagFromQuery(tag.text);
                        $location.path("/spectra/browse/");
                    }
                }
            ];
        }
    }
})();