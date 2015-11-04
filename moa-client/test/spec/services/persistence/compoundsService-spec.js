'use strict';

describe('Compounds Service', function() {
  beforeEach(module('moaClientApp'),function($httpProvider) {
    $httpProvider.interceptors.push('moaClientApp');
  });

  var _Compounds_,service;

  beforeEach(function() {
    angular.mock.inject(function($injector){
      _Compounds_ = $injector.get('Compound');
      service = new _Compounds_();
    });
  });

  it('returns a $resource object of type Compounds', function() {
    expect(service instanceof _Compounds_);
  })
});
