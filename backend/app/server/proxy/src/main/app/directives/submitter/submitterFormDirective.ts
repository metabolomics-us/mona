/**
 * Created by Gert on 5/28/2014.
 */

import * as angular from 'angular';

    submitterFormController.$inject = ['$scope'];
    angular.module('moaClientApp')
        .directive('submitterForm', submitterForm);

    function submitterForm() {
        return {
            restrict: "A",
            replace: true,
            templateUrl: '/views/submitters/template/createUpdateForm.html',
            controller: submitterFormController
        };
    }

    /* @ngInject */
    function submitterFormController($scope) {
        console.log($scope);
    }
