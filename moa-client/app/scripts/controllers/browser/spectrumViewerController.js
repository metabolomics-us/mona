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
      .controller('ViewSpectrumController', viewSpectrumController);

    /* @ngInject */
    function viewSpectrumController($scope, $location, $log, CookieService, Spectrum, delayedSpectrum) {

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
            isSimilarSpectraOpen: false
        };

        /**
         * watch the accordion status and updates related cookies
         */
        $scope.$watch("accordionStatus", function(newVal) {
            angular.forEach($scope.accordionStatus, function(value, key) {
                CookieService.update("DisplaySpectra" + key, value);
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
                var regex = new RegExp("\\s*(\\d+\\.\\d{" + length + "})\\d*\\s*");
                var m = s.match(regex);

                return (m !== null) ? s.replace(m[0].trim(), m[1]) : s;
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


            //
            // Truncate metadata mass values
            //

            for (var i = 0; i < delayedSpectrum.metaData.length; i++) {
                var name = delayedSpectrum.metaData[i].name.toLowerCase();

                if (name.indexOf('mass') > -1 || name.indexOf('m/z') > -1) {
                    delayedSpectrum.metaData[i].value = truncateMass(delayedSpectrum.metaData[i].value);
                } else if (name.indexOf('retention') > -1) {
                    delayedSpectrum.metaData[i].value = truncateRetentionTime(delayedSpectrum.metaData[i].value);
                }
            }

            for (var i = 0; i < delayedSpectrum.biologicalCompound.metaData.length; i++) {
                var name = delayedSpectrum.biologicalCompound.metaData[i].name.toLowerCase();

                if (name.indexOf('mass') > -1 || name.indexOf('m/z') > -1) {
                    delayedSpectrum.biologicalCompound.metaData[i].value = truncateMass(delayedSpectrum.biologicalCompound.metaData[i].value);
                }
            }

            for (var i = 0; i < delayedSpectrum.chemicalCompound.metaData.length; i++) {
                var name = delayedSpectrum.chemicalCompound.metaData[i].name.toLowerCase();

                if (name.indexOf('mass') > -1 || name.indexOf('m/z') > -1) {
                    delayedSpectrum.chemicalCompound.metaData[i].value = truncateMass(delayedSpectrum.chemicalCompound.metaData[i].value);
                }
            }


            // Temporary Origin String
            var tags = [];
            for (var i = 0; i < delayedSpectrum.tags.length; i++) {
                tags.push(delayedSpectrum.tags[i].text);
            }

            if (tags.indexOf('massbank') > -1) {
                var accession = null;
                var authors = null;

                for (var i = 0; i < delayedSpectrum.metaData.length; i++) {
                    if (delayedSpectrum.metaData[i].name == 'accession')
                        accession = delayedSpectrum.metaData[i].value;
                    if (delayedSpectrum.metaData[i].name == 'authors')
                        authors = delayedSpectrum.metaData[i].value;
                }

                $scope.origin = 'Originally submitted to the MassBank Spectral Database as <a href="http://www.massbank.jp/jsp/Dispatcher.jsp?type=disp&id='+ accession +'&site=23">'+ accession +'</a>';
                $scope.authors = 'Authors: '+ authors;
            } else if(tags.indexOf('gnps')) {
                var accession = null;
                var authors = [];

                for (var i = 0; i < delayedSpectrum.metaData.length; i++) {
                    if (delayedSpectrum.metaData[i].name == 'spectrumid')
                        accession = delayedSpectrum.metaData[i].value;
                    if (delayedSpectrum.metaData[i].name == 'authors')
                        authors.push(delayedSpectrum.metaData[i].value);
                }

                $scope.origin = 'Originally submitted to the GNPS Library as <a href="http://gnps.ucsd.edu/ProteoSAFe/gnpslibraryspectrum.jsp?SpectrumID='+ accession +'">'+ accession +'</a>';
                $scope.authors = 'Authors: '+ authors.join(', ');
            } else {
                $scope.origin = "Originally submitted to the MoNA Spectral Library";
            }


            //
            // Create mass spectrum table
            //

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
