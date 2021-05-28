/**
 * Created by wohlgemuth on 7/11/14.
 */

// TODO: waiting for implementation of return user data for admin from authentication Service
import * as angular from 'angular';

class AuthenticationController {
    private static $inject = ['$scope','$rootScope', '$uibModal', 'AuthenticationService'];
    private $scope;
    private $rootScope;
    private $uibModal;
    private AuthenticationService;
    private ADMIN_ROLE_NAME;
    private currentUser;
    private welcomeMessage;

    constructor($scope, $rootScope, $uibModal, AuthenticationService) {
        this.$scope =$scope;
        this.$rootScope = $rootScope;
        this.$uibModal = $uibModal;
        this.AuthenticationService = AuthenticationService;
    }

    $onInit = () => {
        this.currentUser = null;
        this.ADMIN_ROLE_NAME = 'ROLE_ADMIN';
        this.welcomeMessage = 'Login/Register';

        /**
         * Create a welcome message on login
         */
        this.$scope.$on('auth:login-success', (event, data, status, headers, config) => {

            this.AuthenticationService.getCurrentUser().then((data) => {
                console.log(JSON.stringify(data));
                this.welcomeMessage = 'Welcome, ' + data.firstName + '!';
            });
        });

        /**
         * Remove the welcome message on logout
         */
        this.$scope.$on('auth:logout', (event, data, status, headers, config) => {
            this.welcomeMessage = 'Login/Register';
        });

        /**
         * Listen for external calls to bring up the authentication modal
         */
        this.$scope.$on('auth:login', (event) => {
            if (!this.isLoggedIn()) {
                this.AuthenticationService.openAuthenticationDialog();
            }
        });

        this.AuthenticationService.validate();
    }

    isLoggedIn() {
        return this.AuthenticationService.isLoggedIn();
    }

    isAdmin() {
        if (this.AuthenticationService.isLoggedIn() && angular.isDefined(this.$rootScope.currentUser.roles)) {
            for (let i = 0; i < this.$rootScope.currentUser.roles.length; i++) {
                if (this.$rootScope.currentUser.roles[i].authority === this.ADMIN_ROLE_NAME)
                    return true;
            }
        }

        return false;
    };

    /**
     * Handle login
     */

    handleLogin() {
        this.AuthenticationService.handleLogin();
    }

   /* handleLogin() {
        if (this.isLoggedIn()) {
            this.AuthenticationService.logout();
        } else {
            this.openAuthenticationDialog();
        }
    };*/

    /**
     * Opens the authentication modal dialog
     */
    /*openAuthenticationDialog() {
        this.$uibModal.open({
            component: 'authenticationModal',
            size: 'sm',
            backdrop: 'true'
        });
    }; */

    /**
     * Opens the registration modal dialog
     */
    handleRegistration() {
        if (!this.isLoggedIn()) {
            this.$uibModal.open({
                component: 'registrationModal',
                size: 'md',
                backdrop: 'static'
            });
        }
    };
}

let AuthenticationComponent = {
    selector: "authentication",
    templateUrl: "../../views/navbar/loginDropdown.html",
    bindings: {
        modalInstance: '<',
        resolve: '<',
        close: '&'
    },
    controller: AuthenticationController
}

angular.module('moaClientApp')
    .component(AuthenticationComponent.selector, AuthenticationComponent)


