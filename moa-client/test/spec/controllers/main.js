//TODO: find out if this file is used, then remove if no longer needed.

'use strict';

describe('Controller: MainCtrl', function () {

  // load the controller's module
  beforeEach(module('moaClientApp'));

  var MainCtrl,
    scope;

  // Initialize the controller and a mock scope
  beforeEach(inject(function ($controller, $rootScope) {
    scope = $rootScope.$new();
    MainCtrl = $controller('MainCtrl', {
      $scope: scope
    });
  }));

  it('should attach a list of awesomeThings to the scope', function () {
    expect(scope.awesomeThings.length).toBe(3);
  });
});
