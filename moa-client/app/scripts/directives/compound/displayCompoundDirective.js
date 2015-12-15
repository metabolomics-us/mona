/**
 * Created by wohlgemuth on 10/16/14.
 */

(function() {
    'use strict';

    angular.module('moaClientApp')
      .directive('displayCompoundInfo', displayCompoundInfo);

    function displayCompoundInfo() {
        var directive = {
            require: "ngModel",
            restrict: "A",
            replace: true,
            scope: {
                compound: '=compound'
            },
            templateUrl: '/views/compounds/display/template/displayCompound.html',
            controller: displayCompoundInfoController
        };

        return directive;
    }


    function displayCompoundInfoController($scope) {
        //calculate some unique id for the compound picture
        $scope.pictureId = Math.floor(Math.random() * 1000);
    }
})();