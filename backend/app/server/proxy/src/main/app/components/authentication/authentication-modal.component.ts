import * as angular from 'angular';

class AuthenticationModalController{
    private static $inject = ['$scope',  '$timeout', 'AuthenticationService'];
    private $scope;
    private $timeout;
    private modalInstance;
    private AuthenticationService;
    private errors;
    private state;
    private credentials;

    constructor($scope, $timeout, AuthenticationService) {
        this.$scope = $scope;
        this.$timeout = $timeout;
        this.AuthenticationService = AuthenticationService;
    }

    $onInit() {
        this.errors = [];
        this.state = 'login';
        this.credentials = {
            email: '',
            password: ''
        };

        this.$scope.$on('auth:login-success', (event, data, status, headers, config) => {
            this.state = 'success';
            this.$timeout(() =>{
                this.modalInstance.close();
            }, 1000);
        });

        this.$scope.$on('auth:login-error', (event, data, status, headers, config) => {
            this.$scope.state = 'login';

            if (data.status == '401') {
                this.errors.push('Invalid email or password');
            } else {
                this.errors.push('Unable to reach MoNA server');
            }
        });
    }


    submitLogin() {
        this.errors = [];

        if (this.credentials.email === '') {
            this.errors.push('Please enter your email address');
        }

        if (this.credentials.password === '') {
            this.errors.push('Please enter your password');
        }

        if (this.errors.length === 0) {
            this.state = 'logging in';
            this.AuthenticationService.login(this.credentials.email, this.credentials.password);
        }
    };

    cancelDialog() {
        this.modalInstance.dismiss('cancel');
    };



}

let AuthenticationModalComponent = {
    selector: "authenticationModal",
    templateUrl: "../../views/authentication/authenticationModal.html",
    bindings: {
        modalInstance: '<',
        resolve: '<',
        close: '&'
    },
    controller: AuthenticationModalController
}

angular.module('moaClientApp')
    .component(AuthenticationModalComponent.selector, AuthenticationModalComponent)
