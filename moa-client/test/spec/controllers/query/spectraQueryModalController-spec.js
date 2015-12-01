describe('Controller: Spectra Query Modal Controller', function() {
  beforeEach(module('moaClientApp'));

  var scope,querySpecModalController,uibModalInstance,filter;

  beforeEach(function() {
    angular.mock.inject(function($injector,$controller,$rootScope) {
      scope = $rootScope.$new();
      filter = $injector.get('$filter');
      uibModalInstance = {
        close: jasmine.createSpy('uibModalInstance.close'),
        dismiss: jasmine.createSpy('uibModalInstance.dismiss')
      };

      querySpecModalController = $controller('QuerySpectrumModalController', {
        $scope: scope,
        $uibModalInstance: uibModalInstance
      });
    });
  });

  it('can cancel the dialog', function() {
    scope.cancelDialog();
    expect(uibModalInstance.dismiss).toHaveBeenCalledWith('cancel');
  });

  it('closes the dialog and builds the query', function() {
    scope.submitQuery();
    expect(uibModalInstance.close).toHaveBeenCalled();
  });

  it('can filter unique fields', function() {
    expect(filter('unique')(['test','23434','TEST'],10)).toEqual(['test']);
  })
});
