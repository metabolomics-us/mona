'use strict';

describe('service: Authentication Service', function() {
  beforeEach(module('moaClientApp'), function($httpProvider) {
    $httpProvider.interceptors.push('moaClientApp');
    $httpProvider.defaults.headers.push('moaClientApp');
  });

  var AuthenticationService,rootScope,httpBackEnd,http,REST_SERVER,cookieService,q;

  beforeEach(function() {
    angular.mock.inject(function($injector,_AuthenticationService_,_REST_BACKEND_SERVER_,_CookieService_){
      AuthenticationService = _AuthenticationService_;
      httpBackEnd = $injector.get('$httpBackend');
      rootScope = $injector.get('$rootScope');
      http = $injector.get('$http');
      q = $injector.get('$q');
      cookieService = _CookieService_;
      REST_SERVER = _REST_BACKEND_SERVER_;
    });
  });


  afterEach(function() {
    httpBackEnd.verifyNoOutstandingExpectation();
    httpBackEnd.verifyNoOutstandingRequest();
  });

  // expect load main view and flush after each test
  var flush = function() {
    httpBackEnd.expectGET('views/main.html').respond(200);
    httpBackEnd.flush();
  };

  // mock $http response
  var respond = { data: {
                    access_token: 12345
                    },
                  status:200
                };

  // mock User Login
  var mockUserLogin = function() {
      rootScope.currentUser = respond;
      rootScope.currentUser.access_token = respond.data.access_token;
      http.defaults.headers.common['X-Auth-Token'] = respond.data.access_token;
      cookieService.update('AuthorizationToken', respond.data.access_token);
  };

  it('handles successful Log-in', function() {
    httpBackEnd.expectPOST(REST_SERVER + '/rest/login', {email: 'test@test.com', password: '12345'})
      .respond(200);
    AuthenticationService.login('test@test.com','12345');
    flush();
  });

  it('handles unsuccessful Log-in', function() {
    httpBackEnd.expectPOST(REST_SERVER + '/rest/login', {email: 'test@test.com', password: '12345'})
      .respond(401);
    AuthenticationService.login('test@test.com','12345');
    flush();
  });

  it('validates a user that is not logged in', function() {
    AuthenticationService.validate();
    flush();
    expect(AuthenticationService.loggingIn).toBe(false);
  });

  it('validates a user cookie', function() {
    httpBackEnd.expectPOST(REST_SERVER + '/rest/login/validate', {})
      .respond(200);
    mockUserLogin();
    AuthenticationService.validate();
    flush();
    expect(rootScope.currentUser.isNotNull);
  });

  it('handles 401 error when attempting to validate a user cookie', function() {
    httpBackEnd.expectPOST(REST_SERVER + '/rest/login/validate', {})
      .respond(401);
    mockUserLogin();
    AuthenticationService.validate();
    flush();
    expect(rootScope.currentUser).isNull;
  });

  it('redirects a User that is not logged in and tries to logout', function() {
    AuthenticationService.logout();
    flush();
    expect(rootScope.currentUser).toBe(null);
  });

  it('logs out a user that is logged in', function() {
    httpBackEnd.expectPOST(REST_SERVER + '/rest/logout', {})
      .respond(200);
    mockUserLogin();
    AuthenticationService.logout();
    flush();
    expect(rootScope.currentUser).toBe(null);
  });

  it('handles 401 errors when a currentUser tires to logout', function() {
    httpBackEnd.expectPOST(REST_SERVER + '/rest/logout', {})
      .respond(400);
    mockUserLogin();
    AuthenticationService.logout();
    flush();
    expect(rootScope.currentUser).toBe(null);
  });

  it('returns the current user', function() {
    var currentUser;
    mockUserLogin();
    currentUser = AuthenticationService.getCurrentUser();
    flush();
    expect(currentUser.isNotNull);
  });

  it('returns null when currentUser is not defined', function() {
    var currentUser;
    currentUser = AuthenticationService.getCurrentUser();
    flush();
    expect(currentUser).toBe(null);
  });

});
