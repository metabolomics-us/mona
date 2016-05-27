/**
 * displays our spectra
 * @param $scope
 * @param $uibModalInstance
 * @param spectrum
 * @param massSpec
 * @constructor
 */
(function() {
    'use strict';
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
            isBiologicalCompoundOpen: CookieService.getBooleanValue("DisplaySpectraisBiologicalCompoundOpen", true),
            isChemicalCompoundOpen: CookieService.getBooleanValue("DisplaySpectraisChemicalCompoundOpen", false),
            isDerivatizedCompoundOpen: CookieService.getBooleanValue("DisplaySpectraisDerivatizedCompoundOpen", false),
            isSpectraOpen: CookieService.getBooleanValue("DisplaySpectraisSpectraOpen", true),
            isIonTableOpen: CookieService.getBooleanValue("DisplaySpectraisIonTableOpen", false),
            isSimilarSpectraOpen: false,
            compound: []
        };

        /**
         * watch the accordion status and updates related cookies
         */
        $scope.$watch("accordionStatus", function(newVal) {
            angular.forEach($scope.accordionStatus, function(value, key) {
                CookieService.update("DisplaySpectra" + key, value);
            });
        }, true);


        $scope.$watch("accordionStatus.compound")

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
        $scope.similarityResult = {};
        $scope.similarSpectra = [];

        $scope.loadSimilarSpectra = function() {
            if (!$scope.loadingSimilarSpectra)
                return;


            Spectrum.searchSimilarSpectra(
                {spectra: $scope.spectrum.id, minSimilarity: 500, maxHits: 5},
                function(data) {
                    $scope.similarityResult = data;

                    if (data.result.length === 0) {
                        $scope.loadingSimilarSpectra = false;
                    }

                    for (var i = 0, l = data.result.length; i < l; i++) {
                        Spectrum.get({id: data.result[i].id}, function(s) {
                            for (var j = 0, k = $scope.similarityResult.result.length; j < k; j++) {
                                if ($scope.similarityResult.result[j].id === s.id) {
                                    s.similarity = $scope.similarityResult.result[j].similarity;
                                    break;
                                }
                            }

                            $scope.similarSpectra.push(s);
                            $scope.loadingSimilarSpectra = false;
                        });
                    }
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
            for (var i = 0, l = delayedSpectrum.metaData.length; i < l; i++) {
                var curMeta = delayedSpectrum.metaData[i];

                var name = curMeta.name.toLowerCase();

                if (name.indexOf('mass') > -1 || name.indexOf('m/z') > -1) {
                    curMeta.value = truncateMass(curMeta.value);
                } else if (name.indexOf('retention') > -1) {
                    curMeta.value = truncateRetentionTime(curMeta.value);
                }
            }

            // truncate compounds
            for(var i = 0, l = delayedSpectrum.compound.length; i < l; i++) {
                var compoundMeta = delayedSpectrum.compound[i].metaData;
                    for (var j = 0, m = compoundMeta.length; j < m; j++) {
                        var metadata = compoundMeta[j];
                        var name = metadata.name.toLowerCase();

                        if (name.indexOf('mass') > -1 || name.indexOf('m/z') > -1) {
                            metadata.value = truncateMass(metadata.value);
                        }
                }
            }


            // Create mass spectrum table
            

            // Regular expression to extract ions
            var ionRegex = /([0-9]*\.?[0-9]+)+:([0-9]*\.?[0-9]+)/g;

            // Assemble our annotation matrix
            var meta = [];

            for (var i = 0, l = delayedSpectrum.metaData.length; i < l; i++) {
                if (delayedSpectrum.metaData[i].category === 'annotation') {
                    meta.push(delayedSpectrum.metaData[i]);
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
