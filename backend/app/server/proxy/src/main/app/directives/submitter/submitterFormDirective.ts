/**
 * Created by Gert on 5/28/2014.
 */

import * as angular from 'angular';

let submitterFormComponent = {
    selector: 'submitterForm',
    templateUrl: '../../views/submitters/template/createUpdateForm.html',
    bindings: {},
    controller: class SubmitterFormController{
        private static $inject = ['$scope', 'RegistrationService'];
        private $scope;
        private RegistrationService;
        constructor($scope, RegistrationService){
            this.$scope = $scope;
            this.RegistrationService = RegistrationService;
        }

        $onInit(){
            console.log(this.$scope);
        }
    }
}

angular.module('moaClientApp')
    .component(submitterFormComponent.selector, submitterFormComponent)

/*
    submitterFormController.$inject = ['$scope'];
    angular.module('moaClientApp')
        .directive('submitterForm', submitterForm);

    function submitterForm() {
        return {
            restrict: "A",
            replace: true,
            templateUrl: '../../views/submitters/template/createUpdateForm.html',
            controller: submitterFormController
        };
    }
*/
    /* @ngInject */
/*
    function submitterFormController($scope) {
        console.log($scope);
    } */