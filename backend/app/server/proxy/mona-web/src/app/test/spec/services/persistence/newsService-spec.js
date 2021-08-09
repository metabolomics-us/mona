'use strict';

describe('News Services', function() {
  beforeEach(module('moaClientApp'), function($httpProvider) {
    $httpProvider.interceptors.push('moaClientApp');
  });

  var _News_,service;

  beforeEach(function() {
    angular.mock.inject(function($injector) {
      _News_ = $injector.get('News');
      service = new _News_();
    });
  });

  it('returns a $resource object of type News', function() {
    expect(service instanceof _News_).toEqual(true);
  });

});
