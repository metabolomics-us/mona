'use strict';

describe('Controller: View Spectrum Controller', function() {
  beforeEach(module('moaClientApp'), function($httpProvider) {
    $httpProvider.interceptors.push('moaClientApp');
  });

  var scope,viewSpecController,httpBackend,delayed,loadScope;

  beforeEach(function() {
    angular.mock.inject(function($injector,$rootScope,$controller) {
      scope = $rootScope.$new();
      httpBackend = $injector.get('$httpBackend');
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
