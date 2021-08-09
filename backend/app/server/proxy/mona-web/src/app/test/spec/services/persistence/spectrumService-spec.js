'use strict';

describe('Spectrum Services', function() {
  beforeEach(module('moaClientApp'), function($httpProvider) {
    $httpProvider.interceptors.push('moaClientApp');
  });

  var _Spectrum_,service;

  beforeEach(function() {
    angular.mock.inject(function($injector) {
      _Spectrum_ = $injector.get('Spectrum');
      service = new _Spectrum_();
    });
  });

  it('returns a $resource object of type Spectrum', function() {
    expect(service instanceof _Spectrum_);
  });
});
