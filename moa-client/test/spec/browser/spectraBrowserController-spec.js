'use strict';

describe('Controller: Spectra Browser Controller', function() {
  beforeEach(module('moaClientApp'), function($httpProvider) {
    $httpProvider.intereceptors.push('moaClientApp');
  });

  var scope,specBrowserController,rootScope,httpBackend,modal;

  beforeEach(function() {
    angular.mock.inject(function($injector,$controller,$rootScope) {
      scope = $rootScope.$new();
      rootScope = $injector.get('$rootScope');
      httpBackend = $injector.get('$httpBackend');
      modal = $injector.get('$modal');
      specBrowserController = $controller('SpectraBrowserController', {
        $scope: scope
      });
    });
  });

  afterEach(function() {
    httpBackend.verifyNoOutstandingExpectation();
    httpBackend.verifyNoOutstandingRequest();
  });

  it('resets the current query', function() {
    httpBackend.expectPOST('http://cream.fiehnlab.ucdavis.edu:8080/rest/spectra/searchCount').respond(200);
    httpBackend.expectPOST('http://cream.fiehnlab.ucdavis.edu:8080/rest/spectra/search?max=7&offset=0').respond(200);
    httpBackend.expectPOST('http://cream.fiehnlab.ucdavis.edu:8080/rest/spectra/searchCount').respond(200);
    httpBackend.expectGET('views/main.html').respond(200);
    spyOn(scope,'resetQuery').and.callThrough();
    scope.resetQuery();
    expect(scope.resetQuery).toHaveBeenCalled();
    httpBackend.flush();
  });

  it('fires an event to show the current query', function() {
    httpBackend.expectPOST('http://cream.fiehnlab.ucdavis.edu:8080/rest/spectra/searchCount').respond(200);
    httpBackend.expectPOST('http://cream.fiehnlab.ucdavis.edu:8080/rest/spectra/search?max=7&offset=0').respond(200);
    httpBackend.expectGET('views/main.html').respond(200);
    spyOn(rootScope,'$broadcast').and.callThrough();
    scope.displayQuery();
    expect(rootScope.$broadcast).toHaveBeenCalledWith('spectra:query:show');
    httpBackend.flush();
  });

  it('opens our modal dialog to query spectra', function() {
    httpBackend.expectPOST('POST http://cream.fiehnlab.ucdavis.edu:8080/rest/spectra/searchCount').respond(200,{data: 'test'});
    spyOn(modal, 'open');
    var open = {
      templateUrl: '/views/spectra/query/query.html',
      controller: moaControllers.QuerySpectrumModalController,
      size: 'lg',
      backdrop: 'true'
    };

    scope.querySpectraDialog();
    expect(modal.open).toHaveBeenCalledWith(open);
    httpBackend.flush();
  });

});
