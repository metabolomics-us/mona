'use strict';

describe('Controller: View Spectrum Controller', function() {
    beforeEach(module('moaClientApp'));

    var scope, viewSpecController, route, spectrum, location;

    // mock delay Spectrum data for tests
    var delayedSpectrum = {
        metaData: [
            {
                name: 'test1',
                value: '1234567890'
            },
            {
                name: 'mass',
                value: '123.45678987'
            },
            {
                name: 'test category',
                computed: 'test computed',
                category: 'annotation',
                value: '53.0385'
            }
        ],
        biologicalCompound: {
            metaData: [{
                name: 'TEST-BIO-COMPOUND',
                value: '54'
            }]
        },
        chemicalCompound: {
            metaData: [{
                name: 'TEST-CHEM-COMPOUND',
                value: '12345'
            }]
        },
        spectrum: "53.0385:0.708585 54.0335:0.665758 55.0173:1.086237 59.0491:0.545065 60.0554:0.825384 65.0382:7.915126 66.0421:0.525599 " +
        "67.0413:0.599572 68.049:18.189605 69.0331:1.031731 72.0439:1.203037 78.0338:1.911622 79.0173:2.40218 80.0498:1.537863 92.0496:19.026669 " +
        "93.0575:20.611252 94.0648:2.347674 96.0448:1.38213 99.0562:15.939264 100.0585:0.856531 107.0613:1.27701 108.046:30.885731" +
        " 109.0488:2.943352 110.0605:8.421258 111.0648:1.082344 119.0609:0.747518" +
        " 120.0562:3.710337 131.0597:0.883784 132.0682:0.992797 133.0628:0.790345" +
        " 146.071:6.684836 147.0791:19.260269 148.0864:5.283239 156.0119:100 157.0146:9.087016 158.0078:4.960093 160.0873:20.868211 " +
        "161.0015:2.051781 161.0911:2.5073 172.087:0.552852 173.0594:0.747518 " +
        "174.0211:1.12517 176.0288:1.34709 188.0822:13.60327 189.0855:1.26533 190.0981:3.340471 " +
        "194.0379:3.145805 254.0603:25.999611 255.0624:2.57738 256.0566:0.985011"
    };

    beforeEach(inject(function($injector, $rootScope, $controller, _Spectrum_) {
        scope = $rootScope.$new();
        route = $injector.get('$route');
        location = $injector.get('$location');
        spectrum = _Spectrum_;
        viewSpecController = $controller('ViewSpectrumController', {
            $scope: scope,
            delayedSpectrum: delayedSpectrum
        });
    }));

    it('obtains mass spectrum from cache', function() {
        expect(scope.spectrum).toBeDefined();
    });

    it('sorts ion columns', function() {
        scope.sortIonTable('ion');
        expect(scope.ionTableSort).toBe('+ion');
    });

    it('sorts intensity columns', function() {
        scope.sortIonTable('intensity');
        expect(scope.ionTableSort).toBe('-intensity');
    });

    it('sorts annotation columns', function() {
        scope.sortIonTable('annotation');
        expect(scope.ionTableSort).toBe('-annotation');
    });

    it('loads similar Spectras', function() {
        spyOn(scope, 'loadSimilarSpectra').and.callThrough();
        scope.loadSimilarSpectra();
        expect(scope.loadSimilarSpectra).toHaveBeenCalled();
    });

    it('displays the spectrum for the given index', function() {
        scope.viewSpectrum(10);
        expect(location.path()).toBe('/spectra/display/10');
    });

});


