'use strict';

describe('Controller: Spectra Similarity Query Controller', function() {
  beforeEach(module('moaClientApp'), function($httpProvider) {
    $httpProvider.interceptors.push('moaClientApp');
  });

  var scope,specSimQueController,httpBackend,REST_SERVER,uploadService,location;

  beforeEach(function() {
    angular.mock.inject(function($injector,$controller,$rootScope,_REST_BACKEND_SERVER_,_UploadLibraryService_) {
      scope = $rootScope.$new();
      httpBackend = $injector.get('$httpBackend');
      REST_SERVER = _REST_BACKEND_SERVER_;
      uploadService = _UploadLibraryService_;
      location = $injector.get('$location');
      specSimQueController = $controller('SpectraSimilarityQueryController', {
        $scope: scope,
        UploadLibraryService: uploadService
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

  it('sorts ion tables with ion column', function() {
    httpBackend.expectGET('views/main.html').respond(200);
    spyOn(scope,'sortIonTable').and.callThrough();
    scope.sortIonTable('ion');
    expect(scope.sortIonTable).toHaveBeenCalledWith('ion');
    httpBackend.flush();
  });

  it('sorts ion tables with intensity column', function() {
    httpBackend.expectGET('views/main.html').respond(200);
    spyOn(scope,'sortIonTable').and.callThrough();
    scope.sortIonTable('intensity');
    expect(scope.sortIonTable).toHaveBeenCalledWith('intensity');
    httpBackend.flush();
  });

  it('sorts ion tables with annotation column', function() {
    httpBackend.expectGET('views/main.html').respond(200);
    spyOn(scope,'sortIonTable').and.callThrough();
    scope.sortIonTable('annotation');
    expect(scope.sortIonTable).toHaveBeenCalledWith('annotation');
    httpBackend.flush();
  });

  //TODO: find solution to test files
  it('parses spectra', function() {
    httpBackend.expectGET('views/main.html').respond(200,data);
    scope.parseFiles(['file1']);
    scope.$digest();
    httpBackend.flush();
  });

  it('utilizes a splash based search', function() {
    httpBackend.expectGET('views/main.html').respond(200);
    scope.parseSplash('testSplash');
    expect(scope.splash).toBe('testSplash');
    expect(scope.queryState).toBe(3);
    httpBackend.flush();
  });

  it('parses a pasted spectrum and returns spectrum Ions', function() {
    httpBackend.expectGET('views/main.html').respond(200);
    scope.parsePastedSpectrum('1:1 2:20 3:10');
    expect(scope.spectrumIons.length).toBe(3);
    httpBackend.flush();
  });

  it('handles a splash based query with exact type', function() {
    httpBackend.expectGET('views/spectra/browse/spectra.html').respond(200);
    scope.queryOptions.queryType = 'exact';
    scope.queryState = 3;
    scope.query();
    expect(location.path()).toBe('/spectra/browse/');
    httpBackend.flush();
  });

  it('handles a splash based query with histogram type', function() {
    httpBackend.expectGET('views/spectra/browse/spectra.html').respond(200);
    scope.queryOptions.queryType = 'histogram';
    scope.queryState = 3;
    scope.splash = 'ABVEC-2314';
    scope.query();
    expect(location.path()).toBe('/spectra/browse/');
    httpBackend.flush();
  });

  it('handles spectra similar based queries', function() {
    httpBackend.expectGET('views/spectra/browse/spectra.html').respond(200);
    scope.queryOptions.queryType = 'similar';
    scope.spectrumIons = [{selected: 'test'}];
    scope.query();
    httpBackend.flush();
    expect(location.path()).toBe('/spectra/browse');
  });

  it('handles exact spectra based queries', function() {
    httpBackend.expectPOST('http://cream.fiehnlab.ucdavis.edu:9292/splash.fiehnlab.ucdavis.edu/splash/it')
      .respond(200,data);
    httpBackend.expectGET('views/main.html').respond(200);
    httpBackend.expectGET('views/spectra/browse/spectra.html').respond(200);
    scope.queryOptions.queryType = 'exact';
    scope.query();
    httpBackend.flush();
    expect(location.path()).toBe('/spectra/browse');
  });
});
