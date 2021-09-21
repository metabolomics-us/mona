'use strict';

describe('Controller: Spectra Browser Controller', function() {
    beforeEach(module('moaClientApp'));

    var scope, specBrowserController, rootScope, uibModalInstance, location,
      timeout;

    beforeEach(inject(function($injector, $controller, $rootScope) {
        scope = $rootScope.$new();
        rootScope = $injector.get('$rootScope');
        location = $injector.get('$location');
        timeout = $injector.get('$timeout');

        uibModalInstance = {
            result: {
                then: jasmine.createSpy('uibModalInstance.result.then')
            }
        };

        specBrowserController = $controller('SpectraBrowserController', {
            $scope: scope,
            $uibModalInstance: uibModalInstance
        });
    }));

    var spectra = [{
        name: 'testSpectra',
        metadata: 'test',
        meta: [{name: 'ABI Chem', value: {eq: 'AC1Q2J5R'}}],
        inchiKey: '12098',
        inchi: '123456'
    }];


    it('resets the current query', function() {
        spyOn(scope, 'resetQuery').and.callThrough();
        scope.resetQuery();
        expect(scope.resetQuery).toHaveBeenCalled();
    });

    it('broadcast the current query', function() {
        spyOn(rootScope, '$broadcast');
        scope.displayQuery();
        expect(rootScope.$broadcast).toHaveBeenCalledWith('spectra:query:show');
    });

    it('opens modal dialog to query spectra', function() {
        spyOn(scope, 'querySpectraDialog').and.callThrough();
        scope.querySpectraDialog();
        expect(scope.querySpectraDialog).toHaveBeenCalled();
    });

    it('displays the spectrum for the given index', function() {
        scope.viewSpectrum(1212, 1);
        expect(location.path()).toBe('/spectra/display/1212');
    });

    it('get natural mass as accurate mass of spectrum', function() {
        spyOn(scope, 'addAccurateMass').and.callThrough();
        scope.addAccurateMass(spectra);
        expect(scope.addAccurateMass).toHaveBeenCalledWith(spectra);
    });

    it('parses float when biological compound meta data is "total exact mass"', function() {
        spectra.push({
            name: 'testSpectra2',
            biologicalCompound: {
                metaData: [{
                    name: 'total exact mass',
                    value: '3'
                }]
            }
        });

        var result = scope.addAccurateMass(spectra);
        expect(result[1].accurateMass).toBe('3.000');
    });

    it('calls $timeout on $viewContentLoaded broadcast', function() {
        spyOn(scope, '$broadcast');
        scope.$broadcast('$viewContentLoaded');
        expect(scope.$broadcast).toHaveBeenCalledWith('$viewContentLoaded');
    });

});
