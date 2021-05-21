import * as angular from 'angular';

class RegistrationModalController{
    private static $inject = ['$scope', '$rootScope', '$http', 'AuthenticationService', 'REST_BACKEND_SERVER', 'RegistrationService'];
    private $scope;
    private $rootScope;
    private $http;
    private AuthenticationService;
    private REST_BACKEND_SERVER;
    private RegistrationService;
    private errors;
    private state;
    private modalInstance;

    constructor($scope, $rootScope, $http, AuthenticationService, REST_BACKEND_SERVER, RegistrationService){
        this.$scope = $scope;
        this.$rootScope = $rootScope;
        this.$http = $http;
        this.AuthenticationService = AuthenticationService;
        this.REST_BACKEND_SERVER = REST_BACKEND_SERVER;
        this.RegistrationService = RegistrationService;
    }

    $onInit() {
        this.errors = [];
        this.state = 'register';
    }

    cancelDialog() {
        this.modalInstance.dismiss('cancel');
    };

    /**
     * closes the dialog and finishes and builds the query
     */
    submitRegistration(){
        this.errors = [];
        this.state = 'registering';

        this.$http({
            method: 'POST',
            url: this.REST_BACKEND_SERVER + '/rest/users',
            data: {
                username: this.RegistrationService.newSubmitter.emailAddress,
                password: this.RegistrationService.newSubmitter.password
            },
            headers: {
                'Content-Type': 'application/json'
            }
        }).then(
            (response) => {
                this.$http({
                    method: 'POST',
                    url: this.REST_BACKEND_SERVER + '/rest/auth/login',
                    data: {
                        username: this.RegistrationService.newSubmitter.emailAddress,
                        password: this.RegistrationService.newSubmitter.password
                    },
                    headers: {
                        'Content-Type': 'application/json'
                    }
                }).then(
                    (response) => {
                        this.$http({
                            method: 'POST',
                            url: this.REST_BACKEND_SERVER + '/rest/submitters',
                            data: {
                                emailAddress: this.RegistrationService.newSubmitter.emailAddress,
                                firstName: this.RegistrationService.newSubmitter.firstName,
                                lastName: this.RegistrationService.newSubmitter.lastName,
                                institution: this.RegistrationService.newSubmitter.institution
                            },
                            headers: {
                                'Authorization': 'Bearer ' + response.data.token
                            }
                        }).then(
                            (response) => {
                                this.state = 'success';
                            },
                            (response) => {
                                this.errors.push('An unknown error has occurred: ' + JSON.stringify(response));
                            }
                        );
                    },
                    (response) => {
                        this.errors.push('An unknown error has occurred: ' + JSON.stringify(response));
                    }
                );
            },
            (response) => {
                this.errors.push('An unknown error has occurred: ' + JSON.stringify(response));
            }
        );
    };

    /**
     * Close dialog and open login modal
     */
    logIn() {
        this.modalInstance.dismiss({$value: 'cancel'});
        this.$rootScope.$broadcast('auth:login');
    };

}

export let RegistrationModalComponent = {
    selector: "registrationModal",
    templateUrl: '../../views/authentication/registrationModal.html',
    bindings: {
        modalInstance: '<',
        resolve: '<',
        close: '&',
        dismiss: '&',
        onUpdate: '&'
    },
    resolve: {
        newSubmitter: function(){
            return this.newSubmitter;
        }
    },
    controller: RegistrationModalController,
}

angular.module('moaClientApp')
    .component(RegistrationModalComponent.selector, RegistrationModalComponent);
