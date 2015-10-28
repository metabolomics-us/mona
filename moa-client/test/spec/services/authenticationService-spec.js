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

  it('Calls rest end point for login', function() {
    httpBackEnd.expectPOST(REST_SERVER + '/rest/login', {email: 'test@test.com', password: '12345'})
      .respond(200,http.response);

    httpBackEnd.expectGET('views/main.html').respond(200);

    AuthenticationService.login('test@test.com','12345', function(data,status,headers,config) {
    });

    httpBackEnd.flush();
  });

})
/*
 Since $httpBackend doesn't work with returned promises, one way you can do this is to get your data synchronously. $http doesn't have a synchronous option out of the box, so you would have to make the call to your file without it, like so:

 $httpBackend.whenPOST('/phones').respond(function(method, url, data) {
 var request = new XMLHttpRequest();

 request.open('GET', '/responses/phones.js', false);
 request.send(null);

 return [request.status, request.response, {}];
 });

 */
