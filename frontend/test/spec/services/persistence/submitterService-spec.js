'use strict';

describe('Submitter Service', function() {
  beforeEach(module('moaClientApp'), function($httpProvider) {
    $httpProvider.interceptors.push('moaClientApp');
  });

  var _Submitter_,service;

  beforeEach(function() {
    angular.mock.inject(function($injector) {
      _Submitter_ = $injector.get('Submitter');
      service = new _Submitter_();
    });
  });

  it('returns a $resource object of type Submitter', function() {
    expect(service instanceof _Submitter_);
  });

});
