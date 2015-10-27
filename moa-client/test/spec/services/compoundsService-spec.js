'use strict';

describe('Compounds Service test', function() {
  beforeEach(module('moaClientApp'),function($httpProvider) {
    $httpProvider.interceptors.push('moaClientApp');
  });

  var $resource,httpBackEnd,requestHandle;

  var queryResult = [];
  beforeEach(function() {
    angular.mock.inject(function($injector){
      httpBackEnd = $injector.get('$httpBackend');
      $resource = $injector.get('Compound');

    })
  });

  //console.log(mockFactory.get({Id:62}));

  it('has a custom update method', function() {
    expect($resource.update());
  });

  it('handles GET request with @id param', function() {
    expect($resource.get({id:65}));
  });

  it('handles GET request with @offset param', function(){
    expect($resource.get({offset:1}));
  });
  
});
