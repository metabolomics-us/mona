/**
 * Created by sajjan on 4/24/15.
 */

import * as angular from 'angular';

class SubmitterProfileController {
    private static $inject = ['$scope', 'AuthenticationService', 'SpectraQueryBuilderService'];
    private $scope;
    private AuthenticationService;
    private SpectraQueryBuilderService;
    private user;

    constructor($scope, AuthenticationService, SpectraQueryBuilderService) {
        this.$scope = $scope;
        this.AuthenticationService = AuthenticationService;
        this.SpectraQueryBuilderService = SpectraQueryBuilderService;
    }

    $onInit = () => {
        this.$scope.$on('auth:login-success', this.setUserData);
        this.$scope.$on('auth:user-update', this.setUserData);
        this.setUserData();
    }

     setUserData = () => {
        this.AuthenticationService.getCurrentUser().then((data) => {
            this.user = data;
        });
    }

    /**
     * Executes a new query based on username
     */
    queryUserSpectra = () => {
        this.SpectraQueryBuilderService.prepareQuery();
        this.SpectraQueryBuilderService.addUserToQuery(this.user.username);
        this.SpectraQueryBuilderService.executeQuery();
    };

}

let SubmitterProfileComponent = {
    selector: "submitterProfile",
    templateUrl: "../../views/submitters/profile.html",
    bindings: {},
    controller: SubmitterProfileController
}

angular.module('moaClientApp')
    .controller(SubmitterProfileComponent.selector, SubmitterProfileComponent);

