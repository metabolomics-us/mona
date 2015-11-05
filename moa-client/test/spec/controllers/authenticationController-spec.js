'use strict';

describe('Controller: Authentication Controller', function() {
  beforeEach(module('moaClientApp'));

  var scope,createController,modal,rootScope,authService;

  beforeEach(inject(function($controller,$rootScope,$injector,_AuthenticationService_) {
    scope = $rootScope.$new();
    rootScope = $injector.get('$rootScope');
    modal = $injector.get('$modal');
    authService = _AuthenticationService_;
    createController = function() {
      return $controller('AuthenticationController', {
        $scope : scope
      });
    };

  }));

  it('opens a authentication dialogue for user to log in', function() {
    spyOn(modal, 'open');
    var controller = createController();
    controller.handleLogin();
    var open = {
      templateUrl: '/views/authentication/authenticationModal.html',
      controller: moaControllers.AuthenticationModalController,
      size: 'sm',
      backdrop: 'true'
    };
    expect(modal.open).toHaveBeenCalledWith(open);
  });

  it('logs out a user that is currently login', function() {
    spyOn(authService, 'logout');
    var controller = createController();
    rootScope.currentUser = {name: 'test', access_token: 123, submitter: 'test@fiehnlab.com' };
    controller.handleLogin();
    expect(authService.logout).toHaveBeenCalled();
  });

  it('opens a registration modal when a user is not logged in', function() {
    spyOn(modal, 'open');
    var controller = createController();
    controller.handleRegistration();
    var open = {
      templateUrl: '/views/authentication/registrationModal.html',
      controller: moaControllers.RegistrationModalController,
      size: 'md',
      backdrop: 'static'
    };
    expect(modal.open).toHaveBeenCalledWith(open);
  });

  it('returns true for a user that has admin rights', function() {
    var controller = createController();
    rootScope.currentUser = {
      name: 'test',
      access_token: 123,
      submitter: 'test@fiehnlab.com',
      roles: [{authority: 'ROLE_ADMIN'}]
    };
    var isAdmin = controller.isAdmin();
    expect(isAdmin).toBe(true);
  });

  it('returns false for a user that does not have admin rights', function() {
    var controller = createController();
    var isAdmin = controller.isAdmin();
    expect(isAdmin).toBe(false);
  });
  //TODO: ensure scope.$on methods are being called

  it('creates a welcome message on login', function() {
    var controller = createController();
    rootScope.currentUser = {firstName: 'testUser', access_token: 123, submitter: 'test@fiehnlab.com' };
    scope.$broadcast('auth:login-success');
    scope.$digest();
    expect(controller.welcomeMessage).toBe('Welcome, ' + rootScope.currentUser.firstName +'!');
  });

  it('removes the welcome message on logout', function() {
    var controller = createController();
    scope.$broadcast('auth:logout');
    expect(controller.welcomeMessage).toBe('Login/Register');
  });

  it('Listens for external calls to bring up the authentication modal', function() {
    var controller = createController();
    spyOn(controller, 'openAuthenticationDialog');
    scope.$broadcast('auth:login');
    expect(controller.openAuthenticationDialog).toHaveBeenCalled();
  });

});

describe('Controller: Authentication Modal Controller', function() {
  beforeEach(module('moaClientApp'));

  var scope,rootScope,modalController,modalInstance,authService,timeout;

  beforeEach(inject(function($controller,$rootScope,$injector,_AuthenticationService_) {
    scope = $rootScope.$new();
    rootScope = $injector.get('$rootScope');
    // WIP timeout = jasmine.createSpy().andCallFake(function() {return 'test '});
    authService = _AuthenticationService_;
    modalInstance = {                    // Create a mock object using spies
      close: jasmine.createSpy('modalInstance.close'),
      dismiss: jasmine.createSpy('modalInstance.dismiss'),
      result: {
        then: jasmine.createSpy('modalInstance.result.then')
      }
    };
    modalController = $controller('AuthenticationModalController', {
        $scope : scope,
        $modalInstance: modalInstance,
        $timeout: timeout
      });

  }));

  it('instantiate the controller properly', function() {
    expect(modalController).not.toBeUndefined();
  });

  it('can cancel a dialog', function() {
    scope.cancelDialog();
    expect(modalInstance.dismiss).toHaveBeenCalledWith('cancel');
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

  it('is in a success state and closes the modalInstance on success-login', function() {
    scope.$broadcast('auth:login-success');
    expect(scope.state).toBe('success');
    //expect(modalInstance.close).toHaveBeenCalled();
  });

});

