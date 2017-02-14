/**
 * a directive to display our tags and keep track of selections/deselections
 */

(function() {
    'use strict';

    tagDisplayController.$inject = ['$scope', '$log', '$timeout', 'TagService'];
    angular.module('moaClientApp')
        .directive('tagDisplay', tagDisplay);

    function tagDisplay() {
        return {
            restrict: 'A',
            templateUrl: '/views/templates/query/tagDisplay.html',
            replace: true,
            transclude: true,
            scope: {
                tags: '='
            },
            controller: tagDisplayController
        };
    }

    // Controller to handle building of the queries
    /* @ngInject */
    function tagDisplayController($scope, $log, $timeout, TagService) {

        $scope.tagClass = function(tag) {
            var tagClass = [];

            // Button color based on selection
            if (tag.selected === '+') {
                tagClass.push('btn-success');
            } else if (tag.selected === '-') {
                tagClass.push('btn-danger');
            } else {
                tagClass.push('btn-default');
            }

            // Button size based on count
            if ($scope.maxTagsCount > 0) {
                if (tag.count / $scope.maxTagsCount < 0.25) {
                    tagClass.push('btn-xs');
                } else if (tag.count / $scope.maxTagsCount < 0.5) {
                    tagClass.push('btn-sm');
                } else if (tag.count / $scope.maxTagsCount > 0.75) {
                    tagClass.push('btn-lg');
                }
            }

            return tagClass;
        };

        $timeout(function() {
            $scope.maxTagsCount = 0;

            if (angular.isUndefined($scope.tags)) {
                $scope.tags = [];

                TagService.query(
                    function(data) {
                        $scope.tags = data;

                        for (var i = 0; i < data.length; i++) {
                            if (data[i].count > $scope.maxTagsCount)
                                $scope.maxTagsCount = data[i].count;
                        }
                    },
                    function(error) {
                        $log.error('Tag pull failed: '+ error);
                    }
                );
            } else {
                for (var i = 0; i < $scope.tags.length; i++) {
                    if ($scope.tags[i].count > $scope.maxTagsCount)
                        $scope.maxTagsCount = $scope.tags[i].count;
                }
            }
        });
    }
})();