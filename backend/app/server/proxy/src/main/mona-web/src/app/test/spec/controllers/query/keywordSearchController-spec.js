'use strict';

describe('Controller: Keyword Search Controller', function () {
    beforeEach(module('moaClientApp'));

    var scope, ctrl;

    beforeEach(function () {
        angular.mock.inject(function ($injector, $controller, $rootScope) {
            scope = $rootScope.$new();
            ctrl = $controller('KeywordSearchController', {
                $scope: scope
            });
        });
    });

    it('can be initialized', function () {
        expect(ctrl).toBeDefined();
    });

    it('can submit a query', function () {
        spyOn(scope, 'submitQuery').and.callThrough();
        scope.submitQuery();
        expect(scope.submitQuery).toHaveBeenCalled();
    });
});
