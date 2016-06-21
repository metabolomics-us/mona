/**
 * allows us to easily modify meta data on the fly
 */

(function() {
    'use strict';

    gwMetaEditController.$inject = ['$scope', '$element', 'MetaData', '$filter'];
    angular.module('moaClientApp')
      .directive('gwMetaEdit', gwMetaEdit);

    function gwMetaEdit() {
        var directive = {
            restrict: 'A',
            templateUrl: '/views/templates/meta/editMeta.html',
            replace: true,
            transclude: true,
            scope: {
                value: '=value'
            },
            link: linkFunc,
            controller: gwMetaEditController
        };

        return directive;
    }


    function linkFunc($scope, element, attrs, ngModel) {

    }

    //controller to handle building new queries
    /* @ngInject */
    function gwMetaEditController($scope, $element, MetaData, $filter) {

        //receive a click
        $scope.hide = function() {

            MetaData.get({id: $scope.value.metaDataId}, function(value) {

                value.hidden = true;

                console.log($filter('json')(value));

                value.$update(function() {
                    $scope.value.hidden = true;

                });
            });
        };

        //receive a click
        $scope.unhide = function() {

            MetaData.get({id: $scope.value.metaDataId}, function(value) {

                value.hidden = false;

                console.log($filter('json')(value));

                value.$update(function() {
                    $scope.value.hidden = false;
                });
            });
        };
    }
})();