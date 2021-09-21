'use strict';

describe('Controllers" Statistics Controller', function() {
  beforeEach(module('moaClientApp'));

  var scope,statController,stats;

  beforeEach(function() {
    angular.mock.inject(function($controller,$rootScope) {
      scope = $rootScope.$new();
      stats = 'test';
      statController = $controller('StatisticsController', {
        $scope: scope,
        statistics: stats
      });
    });
  });

  it('can be instantiated', function() {
    expect(statController).toBeDefined();
  })

});
