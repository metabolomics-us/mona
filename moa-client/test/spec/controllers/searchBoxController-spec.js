'use strict';

describe('Controller: Search Box Controller', function() {
  beforeEach(module('moaClientApp'), function($httpProvider){
    $httpProvider.interceptors.push('moaClientApp');
  });

  var scope,modal,searchBoxController,modalInstance,location,route,httpBackend;

  beforeEach(function() {
    angular.mock.inject(function($injector,$controller,$rootScope) {
      scope = $rootScope.$new();
      modal = $injector.get('$modal');
      route = $injector.get('$route');
      location = $injector.get('$location');
      httpBackend = $injector.get('$httpBackend');
      modalInstance = {                    // Create a mock object using spies
        close: jasmine.createSpy('modalInstance.close'),
        dismiss: jasmine.createSpy('modalInstance.dismiss'),
        result: {
          then: jasmine.createSpy('modalInstance.result.then')
        }
      };
      
      searchBoxController = $controller('SearchBoxController', {
        $scope: scope,
        $modal: modal,
        $modalInstance: modalInstance,
        $location: location
      });
    });
  });

  afterEach(function() {
    httpBackend.verifyNoOutstandingExpectation();
    httpBackend.verifyNoOutstandingRequest();
  });

  it('can handle empty query errors', function() {
    httpBackend.expectGET('views/main.html').respond(200);
    var res = scope.performSimpleQuery();
    expect(res).toBe(undefined);
    httpBackend.flush();
  });

  it('can handle empty string query errors', function() {
    httpBackend.expectGET('views/main.html').respond(200);
    var res = scope.performSimpleQuery('');
    expect(res).toBe(undefined);
    httpBackend.flush();
  });

  it('handles InChikey queries, and update view', function() {
    httpBackend.expectGET('http://cream.fiehnlab.ucdavis.edu:8080/rest/meta/data/?max=100').respond(200);
    httpBackend.expectGET('views/spectra/browse/spectra.html').respond(200);
    scope.performSimpleQuery('JLKIGFTWXXRPMT-UHFFFAOYSA-N');
    expect(location.path()).toBe('/spectra/browse');
    httpBackend.flush();
  });

  it('handles MoNA ID queries, and update view', function() {
    httpBackend.expectGET('http://cream.fiehnlab.ucdavis.edu:8080/rest/spectra/729?max=7').respond(200);
    httpBackend.expectGET('views/spectra/display/viewSpectrum.html').respond(200);
    scope.performSimpleQuery('729');
    expect(location.path()).toBe('/spectra/display/729');
    httpBackend.flush();
  });

  it('handles a MoNA hash query, and update view', function() {
    httpBackend.expectGET('views/spectra/browse/spectra.html').respond(200);
    scope.performSimpleQuery('mona-1344');
    expect(location.path()).toBe('/spectra/browse');
    httpBackend.flush();
  });

  it('handle a name query, and update view', function() {
    httpBackend.expectGET('http://cream.fiehnlab.ucdavis.edu:8080/rest/meta/data/?max=100').respond(200);
    httpBackend.expectGET('views/spectra/browse/spectra.html').respond(200);
    scope.performSimpleQuery('test');
    expect(location.path()).toBe('/spectra/browse');
    httpBackend.flush();
  });

  it('keeps the current view if the updated view is current', function() {
    httpBackend.expectGET('http://cream.fiehnlab.ucdavis.edu:8080/rest/meta/data/?max=100').respond(200);
    httpBackend.expectGET('views/spectra/browse/spectra.html').respond(200);
    spyOn(route, 'reload');
    location.path('/spectra/browse');
    scope.performSimpleQuery('JLKIGFTWXXRPMT-UHFFFAOYSA-N');
    expect(route.reload).toHaveBeenCalled();
    httpBackend.flush();
  });

  it('opens modal dialog to query spectra against the system', function() {
    httpBackend.expectGET('/views/spectra/query/query.html').respond(200);
    httpBackend.expectGET('views/main.html').respond(200);
    httpBackend.expectGET('template/modal/backdrop.html').respond(200,'<div><b>Hello</b> World!</div>');
    httpBackend.expectGET('template/modal/window.html').respond(200, '<div><b>Hello</b> World!</div>');
    scope.querySpectraDialog();
    httpBackend.flush();
  });
});
