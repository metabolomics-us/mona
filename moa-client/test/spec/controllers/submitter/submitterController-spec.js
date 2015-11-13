'use strict';

describe('Controllers: Submitter Controller', function() {
  beforeEach(module('moaClientApp'), function($httpProvider) {
    $httpProvider.interceptors.push('moaClientApp');
  });

  var scope,subController,httpBackend,REST_SERVER,modalInstance;

  beforeEach(function() {
    angular.mock.inject(function($injector,$controller,$rootScope,_REST_BACKEND_SERVER_) {
      scope = $rootScope.$new();
      httpBackend = $injector.get('$httpBackend');
      REST_SERVER = _REST_BACKEND_SERVER_;
      modalInstance = {
        result: {
          then: jasmine.createSpy('modalInstance.result.then')
        }
      };
      subController = $controller('SubmitterController', {
        $scope: scope,
        $modalInstance: modalInstance
      });
    });
  });

  afterEach(function() {
    httpBackend.verifyNoOutstandingExpectation();
    httpBackend.verifyNoOutstandingRequest();
  });

  it('can list all submitters in the system', function() {
    httpBackend.expectGET(REST_SERVER + '/rest/submitters').respond(200,[{id: 10},{id: 12}]);
    httpBackend.expectGET('views/main.html').respond(200);
    scope.listSubmitter;
    httpBackend.flush();
  });

  it('can remove submitters from the system', function() {
    httpBackend.expectGET(REST_SERVER + '/rest/submitters').respond(200);
    httpBackend.expectDELETE(REST_SERVER + '/rest/submitters/2').respond(200);
    httpBackend.expectGET('views/main.html').respond(200);
    scope.submitters = [{id:1},{id:2}];
    scope.remove(1);
    //scope.submitters.splice(1,1);
    console.log(scope.submitters);
    httpBackend.flush();
  });

  it('displays dialog to create a new submitter', function() {
    httpBackend.expectGET(REST_SERVER + '/rest/submitters').respond(200,[{id: 10},{id: 12}]);
    httpBackend.expectGET('/views/submitters/dialog/createDialog.html').respond(200);
    httpBackend.expectGET('views/main.html').respond(200);
    httpBackend.expectGET('template/modal/backdrop.html').respond(200,'<div><b>Hello</b> World!</div>');
    httpBackend.expectGET('template/modal/window.html').respond(200,'<div><b>Hello</b> World!</div>');
    scope.displayCreateDialog();
    expect(modalInstance).toBeDefined;
    httpBackend.flush();
  });



});
