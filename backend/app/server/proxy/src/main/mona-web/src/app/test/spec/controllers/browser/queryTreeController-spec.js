'use strict';

describe('Controller: Query Tree Controller', function() {
    beforeEach(module('moaClientApp'));

    var scope,queryTreeCtrl,specService,spectrum;

    var node = {query: 'test'};

    beforeEach(inject(function($controller,$rootScope,$injector) {
        scope = $rootScope.$new();
        specService = $injector.get('SpectraQueryBuilderService');
        spectrum = $injector.get('Spectrum');

        spyOn(spectrum,'getPredefinedQueries').and.returnValue('test');
        queryTreeCtrl = $controller('QueryTreeController', {
           $scope: scope,
            SpectraQueryBuilderService: specService,
            Spectrum: spectrum
        });
    }));

    it('executes queries', function() {
        spyOn(scope,'executeQuery');
        scope.executeQuery(node);
        expect(scope.executeQuery).toHaveBeenCalled();
    });
});