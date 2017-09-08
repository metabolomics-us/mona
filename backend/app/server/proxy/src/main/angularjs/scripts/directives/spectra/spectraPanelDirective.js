/**
 * Created by wohlgemuth on 10/16/14.
 */
(function() {
    'use strict';

    displaySpectraPanelController.$inject = ['$scope', 'SpectrumCache'];
    angular.module('moaClientApp')
        .directive('displaySpectraPanel', displaySpectraPanel);

    function displaySpectraPanel() {
        return {
            require: "ngModel",
            restrict: "A",
            templateUrl: '/views/spectra/display/panel.html',
            replace: true,
            scope: {
                spectrum: '=spectrum'
            },
            controller: displaySpectraPanelController
        };
    }

    /* @ngInject */
    function displaySpectraPanelController($scope, SpectrumCache) {

        // Top 10 important metadata fields
        var IMPORTANT_METADATA = [
            'ms level', 'precursor type', 'precursor m/z', 'instrument', 'instrument type',
            'ionization', 'ionization mode', 'collision energy', 'retention time', 'retention index'
        ];

        var truncateDecimal = function(s, length) {
            return (typeof(s) === 'number') ?  s.toFixed(length) :  s;
        };

        /**
         * displays the spectrum for the given index
         */
        $scope.viewSpectrum = function() {
            SpectrumCache.setSpectrum($scope.spectrum);

            return '/spectra/display/' + $scope.spectrum.id;
        };

        (function() {
            var importantMetadata = [];
            var secondaryMetadata = [];

            angular.forEach($scope.spectrum.metaData, function(metaData, index) {
                metaData.value = truncateDecimal(metaData.value, 4);

                if (IMPORTANT_METADATA.indexOf(metaData.name.toLowerCase()) > -1) {
                    importantMetadata.push(metaData);
                } else {
                    secondaryMetadata.push(metaData);
                }
            });

            importantMetadata = importantMetadata.sort(function(a, b) {
                return IMPORTANT_METADATA.indexOf(b.name.toLowerCase()) < IMPORTANT_METADATA.indexOf(a.name.toLowerCase());
            });

            console.log(importantMetadata)

            $scope.spectrum.metaData = importantMetadata.concat(secondaryMetadata).slice(0, 10);
        })();
    }
})();

