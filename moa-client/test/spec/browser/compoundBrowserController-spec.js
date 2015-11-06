'use strict';

describe('Controller: Compound Browser Controller', function() {
  beforeEach(module('moaClientApp'), function($httpProvider) {
    $httpProvider.interceptors.push('moaClientApp');
  });

  var scope,cBrowserController,location,httpBackend;

  beforeEach(inject(function($injector,$controller,$rootScope){
    scope = $rootScope.$new();
    httpBackend = $injector.get('$httpBackend');
    location = $injector.get('$location');
    cBrowserController = $controller('CompoundBrowserController', {
      $scope: scope
    });
  }));

  afterEach(function() {
    httpBackend.verifyNoOutstandingExpectation();
    httpBackend.verifyNoOutstandingRequest();
  });

  it('show the currently selected spectra based on inchikey', function() {
    httpBackend.expectGET('http://cream.fiehnlab.ucdavis.edu:8080/rest/meta/data/?max=100').respond(200);
    httpBackend.expectGET('views/spectra/browse/spectra.html').respond(200);
    scope.viewSpectra('testkey');
    httpBackend.flush();
    expect(location.path()).toBe('/spectra/browse');
  });

  it('can load more compounds into view', function() {
    httpBackend.expectGET('http://cream.fiehnlab.ucdavis.edu:8080/rest/compounds/?max=20&offset=0').respond(200);
    httpBackend.expectGET('views/main.html').respond(200);
    scope.loadMoreCompounds();
    httpBackend.flush();
  });
});
