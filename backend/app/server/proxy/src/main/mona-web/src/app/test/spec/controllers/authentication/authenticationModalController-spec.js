'use strict';

describe('Controller: Authentication Modal Controller', function() {
    beforeEach(module('moaClientApp'));

    var scope,rootScope,modalController,uibModalInstance,authService,timeout;

    beforeEach(inject(function($controller,$rootScope,$injector,_AuthenticationService_,$timeout) {
        scope = $rootScope.$new();
        timeout = $timeout;
        rootScope = $injector.get('$rootScope');
        authService = _AuthenticationService_;
        uibModalInstance = {                    // Create a mock object using spies
            close: jasmine.createSpy('uibModalInstance.close'),
            dismiss: jasmine.createSpy('uibModalInstance.dismiss'),
            result: {
                then: jasmine.createSpy('uibModalInstance.result.then')
            }
        };
        modalController = $controller('AuthenticationModalController', {
            $scope : scope,
            $uibModalInstance: uibModalInstance,
            $timeout: timeout
        });

    }));

    it('instantiate the controller properly', function() {
        expect(modalController).not.toBeUndefined();
    });

    it('can cancel a dialog', function() {
        scope.cancelDialog();
        expect(uibModalInstance.dismiss).toHaveBeenCalledWith('cancel');
    });

    it('pushes error when a user attempt to login without an email address or password', function() {
        scope.submitLogin();
        expect(scope.errors[0]).toBe('Please enter your email address');
        expect(scope.errors[1]).toBe('Please enter your password');
    });

    it('submits email and password credentials to authentication service', function() {
        scope.credentials.email ='testuser@fiehnlab.com';
        scope.credentials.password = 'super';
        spyOn(authService, 'login');
        scope.submitLogin();
        expect(authService.login).toHaveBeenCalledWith('testuser@fiehnlab.com','super');
    });

    it('is in a success state on success-login', function() {
        scope.$broadcast('auth:login-success');
        expect(scope.state).toBe('success');
    });

    it('returns an error for invalid email or password', function() {
        var data = {status: 401};
        scope.$broadcast('auth:login-error',data);
        expect(scope.errors[0]).toBe('Invalid email or password');
    });

    it('returns an error if MoNA servers cant be reached during login', function() {
        var data = {status: 500};
        scope.$broadcast('auth:login-error', data);
        expect(scope.errors[0]).toBe('Unable to reach MoNA server');
    });
});