'use strict';

describe('Controller: Authentication Controller', function() {
    beforeEach(module('moaClientApp'));

    var scope, authController, uibModal, rootScope, authService;

    beforeEach(inject(function($controller, $rootScope, $injector, _AuthenticationService_) {
        scope = $rootScope.$new();
        rootScope = $injector.get('$rootScope');
        uibModal = $injector.get('$uibModal');
        authService = _AuthenticationService_;
        authController = $controller('AuthenticationController', {
            $scope: scope
        });

    }));

    var loggedInUser = {
        firstName: 'test', access_token: 123,
        submitter: 'test@fiehnlab.com', roles: [{authority: 'USER'}]
    };

    it('opens a authentication dialogue for user to log in', function() {
        var open = {
            templateUrl: '/views/authentication/authenticationModal.html',
            controller: 'AuthenticationModalController',
            size: 'sm',
            backdrop: 'true'
        };
        spyOn(uibModal, 'open').and.returnValue(open);
        authController.handleLogin();
        expect(uibModal.open).toHaveBeenCalledWith(open);
    });

    it('logs out a user that is currently login', function() {
        spyOn(authService, 'logout');
        rootScope.currentUser = loggedInUser;
        authController.handleLogin();
        expect(authService.logout).toHaveBeenCalled();
    });

    it('opens a registration uibModal when a user is not logged in', function() {
        spyOn(uibModal, 'open');
        authController.handleRegistration();
        var open = {
            templateUrl: '/views/authentication/registrationModal.html',
            controller: 'RegistrationModalController',
            size: 'md',
            backdrop: 'static'
        };
        expect(uibModal.open).toHaveBeenCalledWith(open);
    });

    it('returns true for a user that has admin rights', function() {
        rootScope.currentUser = {
            name: 'test',
            access_token: 123,
            submitter: 'test@fiehnlab.com',
            roles: [{authority: 'ROLE_ADMIN'}]
        };
        var isAdmin = authController.isAdmin();
        expect(isAdmin).toBe(true);
    });

    it('returns false for a user that does not have admin rights', function() {
        rootScope.currentUser = loggedInUser;
        var isAdmin = authController.isAdmin();
        expect(isAdmin).toBe(false);
    });


    it('removes the welcome message on logout', function() {
        scope.$broadcast('auth:logout');
        expect(authController.welcomeMessage).toBe('Login/Register');
    });

    it('Listens for external calls to bring up the authentication uibModal', function() {
        spyOn(authController, 'openAuthenticationDialog');
        scope.$broadcast('auth:login');
        expect(authController.openAuthenticationDialog).toHaveBeenCalled();
    });
});

