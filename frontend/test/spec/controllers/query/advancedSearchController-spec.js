'use strict';

describe('Controller: Advanced Search Controller', function () {
    beforeEach(module('moaClientApp'));

    var scope, ctrl;

    beforeEach(function () {
        angular.mock.inject(function ($injector, $controller, $rootScope) {
            scope = $rootScope.$new();
            ctrl = $controller('AdvancedSearchController', {
                $scope: scope
            });
        });
    });

    it('can be initialized', function () {
        expect(ctrl).toBeDefined();
    });

    it('can submit a query', function () {
        spyOn(scope, 'submitAdvQuery').and.callThrough();
        scope.submitAdvQuery();
        expect(scope.submitAdvQuery).toHaveBeenCalled();
    });
});
