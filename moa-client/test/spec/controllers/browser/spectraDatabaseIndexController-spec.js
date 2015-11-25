'use strict';

describe('Controller: Spectra Database Index Controller', function() {
  beforeEach(module('moaClientApp'), function($httpProvider) {
    $httpProvider.interceptors.push('moaClientApp');
  });

  var scope,specDataIndexController,location,httpBackend,REST_SERVER,filter;

  beforeEach(function() {
    angular.mock.inject(function($injector,$rootScope,$controller,_REST_BACKEND_SERVER_) {
      scope = $rootScope.$new();
      location = $injector.get('$location');
      httpBackend = $injector.get('$httpBackend');
      filter = $injector.get('$filter');
      REST_SERVER = _REST_BACKEND_SERVER_;
      specDataIndexController = $controller('SpectraDatabaseIndexController', {
        $scope: scope
      });
    });
  });

  var data = [{
    name: 'testSpectra',
    metadata: 'test',
    meta: [{name: 'ABI Chem', value: {eq: 'AC1Q2J5R'}}],
    inchiKey: '12098',
    inchi: '123456'
  }];

  afterEach(function() {
    httpBackend.verifyNoOutstandingExpectation();
    httpBackend.verifyNoOutstandingRequest();
  });

  it('submits a query from clicked metadata link', function() {
    httpBackend.expectGET(REST_SERVER + '/rest/statistics/meta/spectra/count/112').respond(200,data);
    httpBackend.expectGET(REST_SERVER + '/rest/statistics/meta/spectra/count/107').respond(200,data);
    httpBackend.expectGET(REST_SERVER + '/rest/statistics/meta/spectra/count/676').respond(200,data);
    httpBackend.expectGET(REST_SERVER + '/rest/statistics/meta/spectra/count/117').respond(200,data);
    httpBackend.expectGET(REST_SERVER + '/rest/statistics/meta/spectra/count/2079592').respond(200,data);
    //httpBackend.expectGET(REST_SERVER + '/rest/statistics/meta/spectra/count/148').respond(200,data);
    //httpBackend.expectGET(REST_SERVER + '/rest/statistics/meta/spectra/count/138').respond(200,data);
    //httpBackend.expectGET(REST_SERVER + '/rest/statistics/meta/spectra/count/233').respond(200,data);
    //httpBackend.expectGET(REST_SERVER + '/rest/statistics/meta/spectra/count/770').respond(200,data);
    //httpBackend.expectGET(REST_SERVER + '/rest/statistics/meta/spectra/count/8792').respond(200,data);
    httpBackend.expectGET(REST_SERVER + '/rest/statistics/countAll/').respond(200,data);
    httpBackend.expectGET(REST_SERVER + '/rest/meta/data/?max=100').respond(200,data);
    httpBackend.expectGET('views/spectra/browse/spectra.html').respond(200,data);
    scope.submitQuery('test','10');
    httpBackend.flush();
    expect(location.path()).toBe('/spectra/browse');
  });

  it('can title case the first character of any word in a string', function() {
    httpBackend.expectGET(REST_SERVER + '/rest/statistics/meta/spectra/count/112').respond(200,data);
    httpBackend.expectGET(REST_SERVER + '/rest/statistics/meta/spectra/count/107').respond(200,data);
    httpBackend.expectGET(REST_SERVER + '/rest/statistics/meta/spectra/count/676').respond(200,data);
    httpBackend.expectGET(REST_SERVER + '/rest/statistics/meta/spectra/count/117').respond(200,data);
    httpBackend.expectGET(REST_SERVER + '/rest/statistics/meta/spectra/count/2079592').respond(200,data);
    //httpBackend.expectGET(REST_SERVER + '/rest/statistics/meta/spectra/count/148').respond(200,data);
    //httpBackend.expectGET(REST_SERVER + '/rest/statistics/meta/spectra/count/138').respond(200,data);
    //httpBackend.expectGET(REST_SERVER + '/rest/statistics/meta/spectra/count/233').respond(200,data);
    //httpBackend.expectGET(REST_SERVER + '/rest/statistics/meta/spectra/count/770').respond(200,data);
    //httpBackend.expectGET(REST_SERVER + '/rest/statistics/meta/spectra/count/8792').respond(200,data);
    httpBackend.expectGET(REST_SERVER + '/rest/statistics/countAll/').respond(200,data);
    httpBackend.expectGET('views/main.html').respond(200);

    var res = filter('titlecase')('test test test');
    expect(res).toBe('Test Test Test');
    httpBackend.flush();
  });
});
