describe('Controller: Spectra Query Modal Controller', function() {
  beforeEach(module('moaClientApp'));

  var scope,querySpecModalController,modalInstance,filter;

  beforeEach(function() {
    angular.mock.inject(function($injector,$controller,$rootScope) {
      scope = $rootScope.$new();
      filter = $injector.get('$filter');
      modalInstance = {
        close: jasmine.createSpy('modalInstance.close'),
        dismiss: jasmine.createSpy('modalInstance.dismiss')
      };

      querySpecModalController = $controller('QuerySpectrumModalController', {
        $scope: scope,
        $modalInstance: modalInstance
      });
    });
  });

  it('can cancel the dialog', function() {
    scope.cancelDialog();
    expect(modalInstance.dismiss).toHaveBeenCalledWith('cancel');
  });

  it('closes the dialog and builds the query', function() {
    scope.submitQuery();
    expect(modalInstance.close).toHaveBeenCalled();
  });

  it('can filter unique fields', function() {
    expect(filter('unique')(['test','23434','TEST'],10)).toEqual(['test']);
  })
});
