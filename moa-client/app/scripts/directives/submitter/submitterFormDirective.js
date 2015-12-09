/**
 * Created by Gert on 5/28/2014.
 */

(function() {
    'use strict';

    angular.module('moaClientApp')
      .directive('submitterForm', submitterForm);

    function submitterForm() {
        var directive = {
            restrict: "A",
            replace: true,
            templateUrl: '/views/submitters/template/createUpdateForm.html',
            controller: submitterFormController
        };

        return directive;
    }

    submitterFormController.$inject = ['$scope'];

    function submitterFormController($scope) {
        console.log($scope);
    }
})();