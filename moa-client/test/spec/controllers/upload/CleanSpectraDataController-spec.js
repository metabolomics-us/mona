'use strict';

describe('Controller: Clean Spectra Data -', function() {
    beforeEach(module('moaClientApp'));

    var scope, cSpecCtrl;

    beforeEach(inject(function($controller, $rootScope, $injector) {
        scope = $rootScope.$new();
        cSpecCtrl = $controller('CleanSpectraDataController', {
            $scope: scope
        });
    }));

    // create mocks before each tests
    beforeEach(function() {
        scope.spectra = [{
            names: ['isTest'],
            name: 'testSpectra',
            metadata: 'test',
            tags: [{text: 'tag1'}, {text: 'tag2'}, {text: 'tag3'}],
            meta: [{name: 'ABI Chem', value: {eq: 'AC1Q2J5R'}}],
            inchiKey: '12098',
            inchi: '123456',
            ions: [
                {Ion: '53.42', intensity: '1.0872'},
                {Ion: '42.35', intensity: '1.8724'},
                {Ion: '98.32', intensity: '1.5442'}
            ]
        }, {
            names: ['isTest'],
            name: 'testSpectra2',
            metadata: 'test2',
            meta: [{name: 'BH Chem', value: {eq: 'AC234d'}}],
            inchiKey: '130923',
            inchi: '89769',
            ions: [
                {Ion: '23.73', intensity: '0.3423'},
                {Ion: '43.22', intensity: '18.1972'},
                {Ion: '12.33', intensity: '16.2132'}
            ]
        }];
    });

    it('gets the previous Spectrum', function() {
        spyOn(scope, 'previousSpectrum').and.callThrough();
        scope.previousSpectrum();
        expect(scope.previousSpectrum).toHaveBeenCalled();
    });

    it('gets the next spectrum', function() {
        spyOn(scope, 'nextSpectrum').and.callThrough();
        scope.nextSpectrum();
        expect(scope.nextSpectrum).toHaveBeenCalled();
    });

    it('removes current Spectrum', function() {
        scope.removeCurrentSpectrum();
        expect(scope.spectra.length).toBe(1);
    });

    it('resets file when there is no spectra', function() {
        scope.removeCurrentSpectrum();
        scope.removeCurrentSpectrum();
        expect(scope.spectra).toEqual([]);
    });

    it('sorts ion column of Ion Table', function() {
        scope.sortIonTable('ion');
        expect(scope.ionTableSort).toBe('+ion');
    });

    it('sorts intensity column of Ion Table', function() {
        scope.sortIonTable('intensity');
        expect(scope.ionTableSort).toBe('-intensity');
    });

    it('sorts annotation column of Ion Table', function() {
        scope.sortIonTable('annotation');
        expect(scope.ionTableSort).toBe('-annotation');
    });

    it('trim ions when nIons to be trimmed is defined', function() {
        scope.ionCuts.nIons = 1;
        scope.performIonCuts(1);
        expect(scope.spectra[1].ions[0].selected).toBe(false);
    });

    it('trim ions in mass spectrum with intensity limit', function() {
        scope.ionCuts.nIons = 1;
        scope.ionCuts.absAbundance = 2;
        scope.performIonCuts(0);
        expect(scope.spectra[0].ions[0].selected).toBe(false);
    });

    it('cuts all Ions from Spectra', function() {
        spyOn(scope, 'performAllIonCuts').and.callThrough();
        scope.performAllIonCuts();
        expect(scope.performAllIonCuts).toHaveBeenCalled();
    });

    it('resets Ion Cuts', function() {
        spyOn(scope, 'resetIonCuts').and.callThrough();
        scope.currentSpectrum = scope.spectra[0];
        scope.resetIonCuts();
        expect(scope.resetIonCuts).toHaveBeenCalled();
    });

    it('add a new name to the list', function() {
        scope.currentSpectrum = scope.spectra[0];
        scope.addName();
        expect(scope.currentSpectrum.names.length).toBe(2);
    });

    it('remove metadata field', function() {
        scope.removeMetadataField(0);
        expect(scope.spectra[0].meta).toEqual([]);
    });

    it('applies metadata from current spectrum to all spectrum', function() {
        scope.currentSpectrum = scope.spectra[0];
        scope.applyMetadataToAll(0);
        expect(scope.spectra[1].meta.length).toBe(2);
        expect(scope.spectra[1].meta[1]).toEqual({name: 'ABI Chem', value: {eq: 'AC1Q2J5R'}});
    });

    it('applies tags from current spectrum to all spectrum', function() {
        scope.currentSpectrum = scope.spectra[0];
        scope.applyTagsToAll();
        expect(scope.spectra[1].tags.length).toBe(3);
    });

    it('parses files', function() {
        spyOn(scope, 'parseFiles').and.callThrough();
        scope.parseFiles(['file1', 'file2']);
        expect(scope.parseFiles).toHaveBeenCalled();
    });

    it('parses MOL file input', function() {
        spyOn(scope, 'parseMolFile').and.callThrough();
        scope.parseMolFile('MolFile');
        expect(scope.parseMolFile).toHaveBeenCalled();
    });

    it('convert Mol to InChi', function() {
        spyOn(scope, 'convertMolToInChI').and.callThrough();
        scope.currentSpectrum = scope.spectra[0];
        scope.currentSpectrum.molFile = 'testMolFile';
        scope.convertMolToInChI();
        expect(scope.convertMolToInChI).toHaveBeenCalled();
    });

    it('waits for Login', function() {
        spyOn(scope, 'waitForLogin').and.callThrough();
        scope.waitForLogin();
        expect(scope.waitForLogin).toHaveBeenCalled();
    });

    it('returns an error when an uploaded file is invalid', function() {
        scope.uploadFile();
        expect(scope.error).toBe('There are some errors in the data you have provided.  The');
    });

    it('adds spectrum on broadcast', function() {
        
       scope.$broadcast('AddSpectrum');
    });

});