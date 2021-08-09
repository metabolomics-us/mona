'use strict';

describe('Statistics Services', function() {
  beforeEach(module('moaClientApp'), function($httpProvider) {
    $httpProvider.interceptors.push('moaClientApp');
  });

  var _StatisticsService_,service;

  beforeEach(function() {
    angular.mock.inject(function($injector) {
      _StatisticsService_ = $injector.get('StatisticsService');
      service = new _StatisticsService_();
    });
  });

  it('returns a $resource object of type StatisticsService', function() {
    expect(service instanceof _StatisticsService_);
  });
});
