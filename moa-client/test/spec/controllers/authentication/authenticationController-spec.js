'use strict';

describe('Controller: Authentication Controller', function() {
  beforeEach(module('moaClientApp'), function($httpProvider){
    $httpProvider.interceptors.push('moaClientApp');
  });

  var scope,createController,uibModal,rootScope,authService,httpBackEnd;

  beforeEach(inject(function($controller,$rootScope,$injector,_AuthenticationService_) {
    scope = $rootScope.$new();
    rootScope = $injector.get('$rootScope');
    httpBackEnd = $injector.get('$httpBackend');
    uibModal = $injector.get('$uibModal');
    authService = _AuthenticationService_;
    createController = function() {
      return $controller('AuthenticationController', {
        $scope : scope
      });
    };
  }));

  it('opens a authentication dialogue for user to log in', function() {
    spyOn(uibModal, 'open');
    var controller = createController();
    controller.handleLogin();
    var open = {
      templateUrl: '/views/authentication/authenticationModal.html',
      controller: moaControllers.AuthenticationModalController,
      size: 'sm',
      backdrop: 'true'
    };
    expect(uibModal.open).toHaveBeenCalledWith(open);
  });

  it('logs out a user that is currently login', function() {
    spyOn(authService, 'logout');
    var controller = createController();
    rootScope.currentUser = {name: 'test', access_token: 123, submitter: 'test@fiehnlab.com' };
    controller.handleLogin();
    expect(authService.logout).toHaveBeenCalled();
  });

  it('opens a registration uibModal when a user is not logged in', function() {
    spyOn(uibModal, 'open');
    var controller = createController();
    controller.handleRegistration();
    var open = {
      templateUrl: '/views/authentication/registrationModal.html',
      controller: moaControllers.RegistrationModalController,
      size: 'md',
      backdrop: 'static'
    };
    expect(uibModal.open).toHaveBeenCalledWith(open);
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

  it('creates a welcome message on login', function() {
    httpBackEnd.expectPOST('http://cream.fiehnlab.ucdavis.edu:8080/rest/login/validate').respond(200,{firstName: 'testUser', access_token: 123, submitter: 'test@fiehnlab.com' });
    httpBackEnd.expectGET('views/main.html').respond(200);
    var controller = createController();
    httpBackEnd.flush();
    expect(controller.welcomeMessage).toBe('Welcome, ' + rootScope.currentUser.firstName +'!');
  });

  it('removes the welcome message on logout', function() {
    var controller = createController();
    scope.$broadcast('auth:logout');
    expect(controller.welcomeMessage).toBe('Login/Register');
  });

  it('Listens for external calls to bring up the authentication uibModal', function() {
    var controller = createController();
    spyOn(controller, 'openAuthenticationDialog');
    scope.$broadcast('auth:login');
    expect(controller.openAuthenticationDialog).toHaveBeenCalled();
  });
});

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


describe('Controller: Registration Modal Controller', function() {
  beforeEach(module('moaClientApp'), function($httpProvider){
    $httpProvider.interceptors.push('moaClientApp');
  });
  var scope,rootScope,modalController,uibModalInstance,submitter,httpBackend,REST_SERVER;

  beforeEach(function() {
    angular.mock.inject(function($injector,$controller,$rootScope,_Submitter_,_REST_BACKEND_SERVER_) {
      scope = $rootScope.$new();
      submitter = _Submitter_;
      rootScope = $injector.get('$rootScope');
      httpBackend = $injector.get('$httpBackend');
      REST_SERVER = _REST_BACKEND_SERVER_;
      uibModalInstance = {
        dismiss: jasmine.createSpy('uibModalInstance.dismiss')
      };
      modalController = $controller('RegistrationModalController', {
        $scope: scope,
        $uibModalInstance: uibModalInstance,
        Submitter: submitter
      });
    });
  });

  afterEach(function() {
    httpBackend.verifyNoOutstandingExpectation();
    httpBackend.verifyNoOutstandingRequest();
  });

  it('can cancel a dialog', function() {
    httpBackend.expectGET('views/main.html').respond(200);
    scope.cancelDialog();
    expect(uibModalInstance.dismiss).toHaveBeenCalledWith('cancel');
    httpBackend.flush();
  });

  it('register a user that submits all the correct information and is in a success state', function() {
    httpBackend.expectPOST(REST_SERVER + '/rest/submitters',
      {"firstName":"test","lastName":"user",
        "institution":"UC Davis","emailAddress":"testuser@fiehnlab.com","password":"super"})
      .respond(200);
    httpBackend.expectGET('views/main.html').respond(200);
    scope.newSubmitter.firstName = 'test';
    scope.newSubmitter.lastName = 'user';
    scope.newSubmitter.institution = 'UC Davis';
    scope.newSubmitter.emailAddress = 'testuser@fiehnlab.com';
    scope.newSubmitter.password = 'super';
    scope.submitRegistration();
    httpBackend.flush();
    expect(scope.state).toBe('success');
  });

  it('returns an error message when registration data is not submitted correctly', function() {
    var errorData = {status: 422,
      errors: [{message: 'no first name', field: 'First Name'}, {message: 'no last name', field: 'Last Name'}]
    };

    httpBackend.expectPOST(REST_SERVER + '/rest/submitters', {"institution":"UC Davis",
      "emailAddress":"testuser@fiehnlab.com",
      "password":"super"}).respond(422,errorData);
    httpBackend.expectGET('views/main.html').respond(200);
    scope.newSubmitter.institution = 'UC Davis';
    scope.newSubmitter.emailAddress = 'testuser@fiehnlab.com';
    scope.newSubmitter.password = 'super';
    scope.submitRegistration();
    httpBackend.flush();
    expect(scope.errors).toEqual(['Error in First Name: no first name', 'Error in Last Name: no last name']);
  });

  it('returns an error if a user registers with an existing email address', function() {
    var duplicateData = {status: 422,
      errors:[{message: 'must be unique', field: 'Email'}]
    };

    httpBackend.expectPOST(REST_SERVER + '/rest/submitters', {emailAddress: 'testuser@fiehnlab.com'})
      .respond(422,duplicateData);
    httpBackend.expectGET('views/main.html').respond(200);
    scope.newSubmitter.emailAddress = 'testuser@fiehnlab.com';
    scope.submitRegistration();
    httpBackend.flush();
    expect(scope.errors).toEqual(['Error in Email: already exists!']);
  });

  it('returns all other error with log of the data submitted', function() {
    var unknownError = {status: 400,
      errors: [{message: '', field: ''}]
    };

    httpBackend.expectPOST(REST_SERVER + '/rest/submitters',
      {"firstName":"test","lastName":"user",
        "institution":"UC Davis","emailAddress":"testuser@fiehnlab.com","password":"super"})
      .respond(400, unknownError);
    httpBackend.expectGET('views/main.html').respond(200);
    scope.newSubmitter.firstName = 'test';
    scope.newSubmitter.lastName = 'user';
    scope.newSubmitter.institution = 'UC Davis';
    scope.newSubmitter.emailAddress = 'testuser@fiehnlab.com';
    scope.newSubmitter.password = 'super';
    scope.submitRegistration();
    httpBackend.flush();
    expect(scope.errors).toEqual(['An unknown error has occurred: ' +
    '{"data":{"status":400,"errors":[{"message":"","field":""}]},' +
    '"status":400,"config":{"method":"POST","transformRequest":[null],' +
    '"transformResponse":[null],"data":{"firstName":"test","lastName":"user",' +
    '"institution":"UC Davis","emailAddress":"testuser@fiehnlab.com","password":"super"},' +
    '"url":"http://cream.fiehnlab.ucdavis.edu:8080/rest/submitters",' +
    '"headers":{"Accept":"application/json, text/plain, */*","Content-Type":"application/json;charset=utf-8"}},' +
    '"statusText":""}']);
  });

  it('close dialog and open login uibModal', function() {
    httpBackend.expectGET('views/main.html').respond(200);
    scope.logIn();
    expect(uibModalInstance.dismiss).toHaveBeenCalledWith('cancel');
    httpBackend.flush();
  });
});
