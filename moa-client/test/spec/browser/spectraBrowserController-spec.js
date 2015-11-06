'use strict';

describe('Controller: Spectra Browser Controller', function() {
  beforeEach(module('moaClientApp'), function($httpProvider) {
    $httpProvider.interceptors.push('moaClientApp');
  });

  var scope,specBrowserController,rootScope,httpBackend,modal,REST_SERVER,modalInstance,location;

  beforeEach(function() {
    angular.mock.inject(function($injector,$controller,$rootScope,_REST_BACKEND_SERVER_) {
      scope = $rootScope.$new();
      rootScope = $injector.get('$rootScope');
      httpBackend = $injector.get('$httpBackend');
      modal = $injector.get('$modal');
      location = $injector.get('$location');
      REST_SERVER = _REST_BACKEND_SERVER_;

      modalInstance = {
        result: {
          then: jasmine.createSpy('modalInstance.result.then')
        }
      };

      specBrowserController = $controller('SpectraBrowserController', {
        $scope: scope,
        $modalInstance: modalInstance
      });
    });
  });

  var spectra = function() {
    return {name: 'testSpectra',
      metadata: 'test',
      meta: [{name: 'ABI Chem',value: {eq: 'AC1Q2J5R'}}],
      inchiKey: '12098',
      inchi: '123456'
    }
  };

  afterEach(function() {
    httpBackend.verifyNoOutstandingExpectation();
    httpBackend.verifyNoOutstandingRequest();
  });

  it('resets the current query', function() {
    httpBackend.expectPOST(REST_SERVER + '/rest/spectra/searchCount').respond(200);
    httpBackend.expectPOST(REST_SERVER + '/rest/spectra/search?max=7&offset=0').respond(200);
    httpBackend.expectPOST(REST_SERVER + '/rest/spectra/searchCount').respond(200);
    httpBackend.expectGET('views/main.html').respond(200);
    spyOn(scope,'resetQuery').and.callThrough();
    scope.resetQuery();
    expect(scope.resetQuery).toHaveBeenCalled();
    httpBackend.flush();
  });

  it('fires an event to show the current query', function() {
    httpBackend.expectPOST(REST_SERVER + '/rest/spectra/searchCount').respond(200);
    httpBackend.expectPOST(REST_SERVER + '/rest/spectra/search?max=7&offset=0').respond(200);
    httpBackend.expectGET('views/main.html').respond(200);
    spyOn(rootScope,'$broadcast').and.callThrough();
    scope.displayQuery();
    expect(rootScope.$broadcast).toHaveBeenCalledWith('spectra:query:show');
    httpBackend.flush();
  });

  it('opens our modal dialog to query spectra', function() {
    httpBackend.expectPOST(REST_SERVER + '/rest/spectra/searchCount').respond(200,{data: 'test'});
    httpBackend.expectPOST(REST_SERVER + '/rest/spectra/search?max=7&offset=0').respond(200, ['test','test2']);
    httpBackend.expectGET('/views/spectra/query/query.html').respond(200);
    httpBackend.expectGET('views/main.html').respond(200);
    httpBackend.expectGET('template/modal/backdrop.html').respond(200,'<div><b>Hello</b> World!</div>');
    httpBackend.expectGET('template/modal/window.html').respond(200, '<div><b>Hello</b> World!</div>');

    scope.querySpectraDialog();
    httpBackend.flush();
  });

  it('can sets the location to view a spectrum', function() {
    httpBackend.expectPOST(REST_SERVER + '/rest/spectra/searchCount').respond(200,spectra());
    httpBackend.expectPOST(REST_SERVER + '/rest/spectra/search?max=7&offset=0').respond(200,[{name: 'test'}]);
    httpBackend.expectGET('views/main.html').respond(200);
    scope.spectra = [{
      biologicalCompound: {metaData:[{name: 'test'},
        {name: 'total exact mass', value: 10}]}
    }];
    scope.spectra.push.apply = function(data){return data};
    var res = scope.addAccurateMass(scope.spectra);
    httpBackend.flush();
    expect(res[0].accurateMass).toBe('10.000');
  });

  it('scrolls to start location when view content is loaded', function() {
    httpBackend.expectPOST(REST_SERVER + '/rest/spectra/searchCount').respond(200);
    httpBackend.expectPOST(REST_SERVER + '/rest/spectra/search?max=7&offset=0').respond(200);
    httpBackend.expectGET('views/main.html').respond(200);
    spyOn(scope, '$broadcast');
    scope.$broadcast('$viewContentLoaded');
    httpBackend.flush();
    expect(scope.$broadcast).toHaveBeenCalledWith('$viewContentLoaded');
  });
});
