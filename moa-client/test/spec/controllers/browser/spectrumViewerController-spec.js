'use strict';

describe('Controller: View Spectrum Controller', function() {
  beforeEach(module('moaClientApp'), function($httpProvider) {
    $httpProvider.interceptors.push('moaClientApp');
  });

  var scope,viewSpecController,httpBackend,route;

  var data = [{
    name: 'testSpectra',
    metadata: 'test',
    meta: [{name: 'ABI Chem', value: {eq: 'AC1Q2J5R'}}],
    inchiKey: '12098',
    inchi: '123456'
  }];

  beforeEach(function() {
    angular.mock.inject(function($injector,$rootScope,$controller) {
      scope = $rootScope.$new();
      httpBackend = $injector.get('$httpBackend');
      route = $injector.get('$route');
      viewSpecController = $controller('ViewSpectrumController', {
        $scope: scope,
        delayedSpectrum: self.loadSpectrum
      });
    });
  });
/*
  afterEach(function() {
    httpBackend.verifyNoOutstandingExpectation();
    httpBackend.verifyNoOutstandingRequest();
  });


  it('sort order for the ion table', function() {
    //viewSpecController.delayedSpectrum();
  });
*/
});
