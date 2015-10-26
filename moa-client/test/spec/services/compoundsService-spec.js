'use strict';

describe('Compounds Service test', function() {
  beforeEach(module('moaClientApp'),function($httpProvider) {
    $httpProvider.interceptors.push('moaClientApp');
  });

  var mockFactory,httpBackEnd,requestHandle;

  var queryResult = [];
  beforeEach(function() {
    angular.mock.inject(function($injector){
      httpBackEnd = $injector.get('$httpBackend');
      mockFactory = $injector.get('Compound');

    })
  });

  console.log(httpBackEnd);

  it('calls the REST API with params', function() {
    var id = 62;
    var max = 20;
    httpBackEnd.expectGET('');
  });


});
