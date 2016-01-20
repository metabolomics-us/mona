'use strict';

describe('Controller: Compound Browser Controller', function() {
    beforeEach(module('moaClientApp'));

    var scope, cBrowserController, location;

    beforeEach(inject(function($injector, $controller, $rootScope) {
        scope = $rootScope.$new();
        location = $injector.get('$location');
        cBrowserController = $controller('CompoundBrowserController', {
            $scope: scope
        });
    }));

    it('shows the currently selected spectra based on inchi key', function() {
        scope.viewSpectra('1234');
        expect(location.path()).toBe('/spectra/browse/');
    });

    it('can load more compounds', function() {
        spyOn(scope, 'loadMoreCompounds').and.callThrough();
        scope.loadMoreCompounds();
        expect(scope.loadMoreCompounds).toHaveBeenCalled();
    });
});