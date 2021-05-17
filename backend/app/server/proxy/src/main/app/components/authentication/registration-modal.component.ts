import * as angular from 'angular';

class RegistrationModalController{
    private static $inject = ['$scope', '$rootScope', '$uibModalInstance', '$http', 'AuthenticationService', 'REST_BACKEND_SERVER'];
    private $scope;
    private $rootScope;
    private $uibModalInstance;
    private $http;
    private AuthenticationService;
    private REST_BACKEND_SERVER;
    private errors;
    private state;
    private newSubmitter;

    constructor($scope, $rootScope, $uibModalInstance, $http, AuthenticationService, REST_BACKEND_SERVER){
        this.$scope = $scope;
        this.$rootScope = $rootScope;
        this.$uibModalInstance = $uibModalInstance;
        this.$http = $http;
        this.AuthenticationService = AuthenticationService;
        this.REST_BACKEND_SERVER = REST_BACKEND_SERVER;
    }

    $onInit = () => {
        this.errors = [];
        this.state = 'register';
        this.newSubmitter = {};
    }

    cancelDialog = () => {
        this.$uibModalInstance.dismiss('cancel');
    };

    /**
     * closes the dialog and finishes and builds the query
     */
    submitRegistration = () => {
        this.errors = [];

        // var submitter = new Submitter();
        // submitter.firstName = $scope.newSubmitter.firstName;
        // submitter.lastName = $scope.newSubmitter.lastName;
        // submitter.institution = $scope.newSubmitter.institution;
        // submitter.emailAddress = $scope.newSubmitter.emailAddress;
        // submitter.password = $scope.newSubmitter.password;

        this.state = 'registering';


        this.$http({
            method: 'POST',
            url: this.REST_BACKEND_SERVER + '/rest/users',
            data: {
                username: this.newSubmitter.emailAddress,
                password: this.newSubmitter.password
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
                        username: this.newSubmitter.emailAddress,
                        password: this.newSubmitter.password
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
                                emailAddress: this.newSubmitter.emailAddress,
                                firstName: this.newSubmitter.firstName,
                                lastName: this.newSubmitter.lastName,
                                institution: this.newSubmitter.institution
                            },
                            headers: {
                                'Content-Type': 'application/json',
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


        /*
        Submitter.save(submitter,
            function() {
                $scope.state = 'success';
            },
            function(data) {
                $scope.state = 'register';

                if (data.status === 422) {
                    for (var i = 0; i < data.data.errors.length; i++) {
                        var message = 'Error in ' + data.data.errors[i].field + ': ';

                        if (data.data.errors[i].message.indexOf('must be unique') > -1) {
                            $scope.errors.push(message + 'already exists!');
                        } else {
                            $scope.errors.push(message + data.data.errors[i].message);
                        }
                    }
                } else {
                    $scope.errors.push('An unknown error has occurred: ' + JSON.stringify(data));
                }
            }
        );
        */
    };

    /**
     * Close dialog and open login modal
     */
    logIn = () => {
        this.$uibModalInstance.dismiss('cancel');
        this.$rootScope.$broadcast('auth:login');
    };

}

let RegistrationModalComponent = {
    selector: "registrationModal",
    bindings: {},
    controller: RegistrationModalController
}

angular.module('moaClientApp')
    .component(RegistrationModalComponent.selector, RegistrationModalComponent);
