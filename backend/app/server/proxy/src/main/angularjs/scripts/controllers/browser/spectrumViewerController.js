/**
 * displays our spectra
 * @param $scope
 * @param spectrum
 * @param massSpec
 * @constructor
 */
(function() {
    'use strict';
    ViewSpectrumController.$inject = ['$scope', '$location', '$log', 'CookieService', 'Spectrum', 'delayedSpectrum'];
    angular.module('moaClientApp')
      .controller('ViewSpectrumController', ViewSpectrumController);


    /* @ngInject */
    function ViewSpectrumController($scope, $location, $log, CookieService, Spectrum, delayedSpectrum) {

        /**
         * Mass spectrum obtained from cache if it exists, otherwise from REST api
         */
        $scope.spectrum = delayedSpectrum;

        /**
         * quality score of our spectrum
         * @type {number}
         */
        $scope.score = 0;

        $scope.massSpec = [];


        /**
         * status of our accordion
         * @type {{isBiologicalCompoundOpen: boolean, isChemicalCompoundOpen: boolean, isDerivatizedCompoundOpen: boolean}}
         */
        $scope.accordionStatus = {
            isSpectraOpen: true,
            isIonTableOpen: false,
            isSimilarSpectraOpen: false,
            isCompoundOpen: []
        };

        if (angular.isDefined($scope.spectrum.compound)) {
            for (var i = 0; i < $scope.spectrum.compound.length; i++) {
                var name = 'DisplayCompound' + i;
                $scope.accordionStatus.isCompoundOpen.push(CookieService.getBooleanValue(name, false));
            }
        }

        /**
         * watch the accordion status and updates related cookies
         */
        $scope.$watch("accordionStatus", function(newVal) {
            angular.forEach($scope.accordionStatus, function(value, key) {

                if(key === 'isCompoundOpen') {
                    for (var i = 0; i < $scope.spectrum.compound.length; i++) {
                        CookieService.update('DisplayCompound' + i, value[i]);
                    }
                }
                else {
                    CookieService.update("DisplaySpectra" + key, value);
                }
            });
        }, true);


        /**
         * Sort order for the ion table - default m/z ascending
         */
        $scope.ionTableSort = 'ion';
        $scope.ionTableSortReverse = false;

        $scope.sortIonTable = function(column) {
            if (column === 'ion') {
                $scope.ionTableSortReverse = ($scope.ionTableSort === '+ion') ? !$scope.ionTableSortReverse : false;
                $scope.ionTableSort = '+ion';
            }
            else if (column === 'intensity') {
                $scope.ionTableSortReverse = ($scope.ionTableSort === '-intensity') ? !$scope.ionTableSortReverse : false;
                $scope.ionTableSort = '-intensity';
            }
            else if (column === 'annotation') {
                $scope.ionTableSortReverse = ($scope.ionTableSort === '-annotation') ? !$scope.ionTableSortReverse : false;
                $scope.ionTableSort = '-annotation';
            }
        };


        /**
         * Loading of similar spectra
         */
        $scope.loadingSimilarSpectra = true;
        $scope.similarSpectra = [];

        $scope.loadSimilarSpectra = function() {
            if (!$scope.loadingSimilarSpectra)
                return;

            Spectrum.searchSimilarSpectra(
                {spectrum: $scope.spectrum.spectrum, minSimilarity: 0.5},
                function(data) {
                    $scope.similarSpectra = data;
                    $scope.loadingSimilarSpectra = false;
                }, function(data) {
                    $scope.loadingSimilarSpectra = false;
                }
            );
        };

        /**
         * @Deprecated
         * displays the spectrum for the given index
         * @param id
         * @param index
         */
        $scope.viewSpectrum = function(id) {
            $location.path('/spectra/display/' + id);
        };


        /**
         * Perform all initial data formatting and processing
         */
        (function() {
            // Regular expression for truncating accurate masses
            var massRegex = /^\s*(\d+\.\d{4})\d*\s*$/;

            /**
             * Decimal truncation routines
             */
            var truncateDecimal = function(s, length) {
                return (typeof(s) === 'number') ? s.toFixed(length) : s;
            };

            /**
             * Truncate the
             */
            var truncateMass = function(mass) {
                return truncateDecimal(mass, 4);
            };

            var truncateRetentionTime = function(mass) {
                return truncateDecimal(mass, 1);
            };


            // truncate metadata
            if (angular.isDefined(delayedSpectrum.metaData)) {
                for (var i = 0; i < delayedSpectrum.metaData.length; i++) {
                    var curMeta = delayedSpectrum.metaData[i];

                    var name = curMeta.name.toLowerCase();

                    if (name.indexOf('mass') > -1 || name.indexOf('m/z') > -1) {
                        curMeta.value = truncateMass(curMeta.value);
                    } else if (name.indexOf('retention') > -1) {
                        curMeta.value = truncateRetentionTime(curMeta.value);
                    }
                }
            }

            // truncate compounds
            if (angular.isDefined(delayedSpectrum.compound)) {
                for (var i = 0; i < delayedSpectrum.compound.length; i++) {
                    var compoundMeta = delayedSpectrum.compound[i].metaData;
                    for (var j = 0, m = compoundMeta.length; j < m; j++) {
                        var metadata = compoundMeta[j];
                        var name = metadata.name.toLowerCase();

                        if (name.indexOf('mass') > -1 || name.indexOf('m/z') > -1) {
                            metadata.value = truncateMass(metadata.value);
                        }
                    }
                }
            }


            // Regular expression to extract ions
            var ionRegex = /([0-9]*\.?[0-9]+)+:([0-9]*\.?[0-9]+)/g;

            // Assemble our annotation matrix
            var meta = [];

            if (angular.isDefined(delayedSpectrum.metaData)) {
                for (var i = 0, l = delayedSpectrum.metaData.length; i < l; i++) {
                    if (delayedSpectrum.metaData[i].category === 'annotation') {
                        meta.push(delayedSpectrum.metaData[i]);
                    }
                }
            }

            // Parse spectrum string to generate ion list
            var match;

            while ((match = ionRegex.exec(delayedSpectrum.spectrum)) !== null) {
                // Find annotation
                var annotation = '';
                var computed = false;

                for (var i = 0; i < meta.length; i++) {
                    if (meta[i].value === match[1]) {
                        annotation = meta[i].name;
                        computed = meta[i].computed;
                    }
                }

                // Truncate decimal values of m/z
                match[1] = truncateMass(match[1]);

                // Store ion
                $scope.massSpec.push({
                    ion: parseFloat(match[1]),
                    intensity: parseFloat(match[2]),
                    annotation: annotation,
                    computed: computed
                });
            }
        })();
    }
})();
