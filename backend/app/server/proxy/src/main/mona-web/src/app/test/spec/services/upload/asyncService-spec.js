'use strict';

describe('service: Async Service', function() {
  beforeEach(module('moaClientApp'));

  var ASyncService,ApplicationError,log,q,interval;

  beforeEach(function() {
    angular.mock.inject(function($injector,_ApplicationError_,_AsyncService_) {
      ApplicationError = _ApplicationError_;
      ASyncService = _AsyncService_;
      log = $injector.get('$log');
      q = $injector.get('$q');
      interval = $injector.get('$interval');
    });
  });

  var data = {"name": "ABI Chem",
    "value": {
      "eq": "AC1Q2J5R"
    }};

  var f = function(data) {return data};

  it('adds a function and object to pool', function() {
    ASyncService.addToPool(f,data);
    expect(ASyncService.hasPooledTasks()).toEqual(true);
  });

  it('resets the pool', function () {
    ASyncService.resetPool();
    expect(ASyncService.hasPooledTasks()).toEqual(false);
  });
});
