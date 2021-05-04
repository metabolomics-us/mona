/**
 * displays our spectra
 * @param $scope
 * @param spectrum
 * @param massSpec
 * @constructor
 */
import * as angular from 'angular';

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
                $scope.accordionStatus.isCompoundOpen.push(i === 0);
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
                    $scope.similarSpectra = data.filter(function(x) { return x.id !== $scope.spectrum.id; });
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


            // truncate metadata
            if (angular.isDefined(delayedSpectrum.metaData)) {
                for (var i = 0; i < delayedSpectrum.metaData.length; i++) {
                    var curMeta = delayedSpectrum.metaData[i];

                    var name = curMeta.name.toLowerCase();

                    if (name.indexOf('mass accuracy') > -1) {
                        curMeta.value = truncateDecimal(curMeta.value, 1);
                    } else if (name.indexOf('mass') > -1 || name.indexOf('m/z') > -1) {
                        curMeta.value = truncateMass(curMeta.value);
                    } else if (name.indexOf('retention') > -1) {
                        curMeta.value = truncateDecimal(curMeta.value, 1);
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


            // Parse spectrum string to generate ion list
            var match;

            while ((match = ionRegex.exec(delayedSpectrum.spectrum)) !== null) {
                // Find annotation
                var annotation = '';
                var computed = false;

                if (angular.isDefined(delayedSpectrum.annotations)) {
                    for (var i = 0; i < delayedSpectrum.annotations.length; i++) {
                        if (delayedSpectrum.annotations[i].value === parseFloat(match[1])) {
                            annotation = delayedSpectrum.annotations[i].name;
                            computed = delayedSpectrum.annotations[i].computed;
                        }
                    }
                }

                // Truncate decimal values of m/z
                match[1] = truncateMass(match[1]);

                // Store ion
                var intensity = parseFloat(match[2]);

                if (intensity > 0) {
                    $scope.massSpec.push({
                        ion: parseFloat(match[1]),
                        intensity: intensity,
                        annotation: annotation,
                        computed: computed
                    });
                }
            }
        })();
    }
