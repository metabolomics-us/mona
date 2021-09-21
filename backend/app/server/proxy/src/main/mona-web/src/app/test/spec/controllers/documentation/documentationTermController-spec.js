describe('Controller: Documentation Term Controller', function() {
  beforeEach(module('moaClientApp'));

  var scope,docController;

  beforeEach(function() {
    angular.mock.inject(function($controller,$rootScope) {
      scope = $rootScope.$new();
      docController = $controller('DocumentationTermController', {
        $scope: scope
      });
    });
  });

  it('sets the scope with 17 terms', function() {
    expect(scope.terms.length).toBe(17);
  });
});
