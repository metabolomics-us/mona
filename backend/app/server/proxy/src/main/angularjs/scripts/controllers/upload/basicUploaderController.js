/**
 * Created by sajjan on 7/13/16.
 */

(function () {
    'use strict';
    BasicUploaderController.$inject = ['$scope', '$rootScope', '$window', '$location', 'UploadLibraryService', 'gwCtsService', 'TaggingService', '$q', '$filter', 'AsyncService', '$log', 'REST_BACKEND_SERVER', '$http'];
    angular.module('moaClientApp')
        .controller('BasicUploaderController', BasicUploaderController);

    /* @ngInject */
    function BasicUploaderController($scope, $rootScope, $window, $location, UploadLibraryService, gwCtsService,
                                        TaggingService, $q, $filter, AsyncService, $log, REST_BACKEND_SERVER, $http) {

        $scope.currentSpectrum = null;
        $scope.page = 0;
        $scope.fileHasMultipleSpectra = false;
        $scope.showIonTable = true;


        /**
         * Sort order for the ion table - default m/z ascending
         */
        $scope.ionTableSort = 'ion';
        $scope.ionTableSortReverse = false;

        $scope.sortIonTable = function (column) {
            if (column === 'ion') {
                $scope.ionTableSortReverse = ($scope.ionTableSort === '+ion') ? !$scope.ionTableSortReverse : false;
                $scope.ionTableSort = '+ion';
            }
            else if (column === 'intensity') {
                $scope.ionTableSortReverse = ($scope.ionTableSort === '-intensity') ? !$scope.ionTableSortReverse : false;
                $scope.ionTableSort = '-intensity';
            }
            else if (column === 'relativeIntensity') {
                $scope.ionTableSortReverse = ($scope.ionTableSort === '-relativeIntensity') ? !$scope.ionTableSortReverse : false;
                $scope.ionTableSort = '-relativeIntensity';
            }
            else if (column === 'annotation') {
                $scope.ionTableSortReverse = ($scope.ionTableSort === '-annotation') ? !$scope.ionTableSortReverse : false;
                $scope.ionTableSort = '-annotation';
            }
        };


        /**
         * Handle switching pages
         */
        $scope.previousPage = function () {
            $scope.page--;
        };

        $scope.nextPage = function () {
            $scope.page++;
        };

        $scope.restart = function () {
            $scope.currentSpectrum = null;
            $scope.page = 0;
            $scope.fileHasMultipleSpectra = false;

            // Scroll to top of the page
            $window.scrollTo(0, 0);
        };



        /**
         * Parse spectra
         * @param files
         */
        $scope.parseFiles = function (files) {
            $scope.page = 1;

            UploadLibraryService.loadSpectraFile(files[0],
                function (data, origin) {
                    $log.info("Loading file "+ files[0].name +"...");

                    UploadLibraryService.processData(data, function (spectrum) {
                        if (!$scope.currentSpectrum) {
                            // Create list of ions
                            $log.info("Parsing ions...");

                            spectrum.basePeak = 0;
                            spectrum.basePeakIntensity = 0;

                            spectrum.ions = spectrum.spectrum.split(' ').map(function (x) {
                                x = x.split(':');
                                var annotation = '';

                                for (var i = 0; i < spectrum.meta.length; i++) {
                                    if (spectrum.meta[i].category === 'annotation' && spectrum.meta[i].value === x[0]) {
                                        annotation = spectrum.meta[i].name;
                                    }
                                }

                                var mz = parseFloat(x[0]);
                                var intensity = parseFloat(x[1]);

                                if (intensity > spectrum.basePeakIntensity) {
                                    spectrum.basePeak = mz;
                                    spectrum.basePeakIntensity = intensity;
                                }

                                return {
                                    ion: mz,
                                    intensity: intensity,
                                    annotation: annotation,
                                    selected: true
                                }
                            });

                            for (var i = 0; i < spectrum.ions.length; i++) {
                                spectrum.ions[i].relativeIntensity = 100 * spectrum.ions[i].intensity / spectrum.basePeakIntensity;
                            }

                            // Get structure from InChIKey if no InChI is provided
                            if (angular.isDefined(spectrum.inchiKey) && angular.isUndefined(spectrum.inchi)) {
                                $log.info("Retrieving MOL data...");

                                gwCtsService.convertInchiKeyToMol(spectrum.inchiKey, function (molecule) {
                                    if (molecule !== null) {
                                        spectrum.molFile = molecule;
                                    }
                                });
                            }

                            // Remove annotations and origin from metadata
                            spectrum.hiddenMetadata = spectrum.meta.filter(function (metadata) {
                                return metadata.name === 'origin' || (angular.isDefined(metadata.category) && metadata.category === 'annotation');
                            });

                            spectrum.meta = spectrum.meta.filter(function (metadata) {
                                return metadata.name !== 'origin' && (angular.isUndefined(metadata.category) || metadata.category !== 'annotation');
                            });

                            // Add an empty metadata field if none exist
                            if (spectrum.meta.length === 0) {
                                spectrum.meta.push({name: '', value: ''});
                            }

                            $log.info("Loaded spectrum from file "+ files[0].name);

                            $scope.$apply(function() {
                                $scope.currentSpectrum = spectrum;
                                $scope.page = 5;
                                $scope.showIonTable = $scope.currentSpectrum.ions.length < 500;
                            });
                        } else {
                            $log.info("Skipping additional spectrum in file "+ files[0].name);
                            $scope.fileHasMultipleSpectra = true;
                        }
                    }, origin);
                },
                function (progress) {
                    if (progress == 100) {
                        $scope.$apply(function() {
                            $scope.page = 2;
                        });
                    }
                }
            );
        };


        /**
         * Upload current data
         */
        var validateSpectrum = function (spectrum) {
            spectrum.errors = [];

            var ionCount = 0;

            for (var j = 0; j < spectrum.ions.length; j++) {
                if (spectrum.ions[j].selected) {
                    ionCount++;
                }
            }

            if (ionCount === 0) {
                spectrum.errors.push('This spectrum has no selected ions!  It cannot be uploaded.');
            }

            if ((angular.isUndefined(spectrum.inchi) || spectrum.inchi === '') &&
                (angular.isUndefined(spectrum.molFile) || spectrum.molFile === '') &&
                (angular.isUndefined(spectrum.smiles) || spectrum.smiles === '')) {
                spectrum.errors.push('This spectrum requires a structure in order to upload. Please provide a MOL file or InChI code!');
            }

            if (spectrum.errors.length > 0) {
                $scope.error = 'There are some errors in the data you have provided.';
                $window.scrollTo(0, 0);

                return false;
            } else {
                return true;
            }
        };


        $scope.uploadFile = function () {
            if (validateSpectra()) {
                // Reset the spectrum count if necessary
                if (!UploadLibraryService.isUploading()) {
                    UploadLibraryService.completedSpectraCount = 0;
                    UploadLibraryService.failedSpectraCount = 0;
                    UploadLibraryService.uploadedSpectraCount = 0;
                    UploadLibraryService.uploadStartTime = new Date().getTime();
                }

                // Re-add origin and annotations to metadata:
                for (var i = 0; i < $scope.spectra.length; i++) {
                    $scope.spectra[i].meta.push.apply($scope.spectra[i].meta, $scope.spectra[i].hiddenMetadata);
                }

                UploadLibraryService.uploadSpectra($scope.spectra, function (spectrum) {
                    $log.info(spectrum);
                    var req = {
                        method: 'POST',
                        url: REST_BACKEND_SERVER + '/rest/spectra',
                        headers: {
                            'Content-Type': 'application/json',
                            'Authorization': 'bearer ' + spectrum.submitter.access_token
                        },
                        data: JSON.stringify(spectrum)
                    };

                    $http(req).then(function (data) {
                            $log.info('Spectra successfully Upload!');
                            $log.info('Reference ID: ' + data.data.id);
                            $log.info(data);
                        },
                        function (err) {
                            $log.info(err);
                        });

                    //spectrum.$batchSave(spectrum.submitter.access_token);
                }, $scope.spectrum);

                $location.path('/upload/status');
            }
        };


        /**
         * provides us with an overview of all our tags
         * @param query
         * @returns {*}
         * Performs initialization and acquisition of data used by the wizard
         */
        $scope.loadTags = function (query) {
            var deferred = $q.defer();

            // First filters by the query and then removes any tags already selected
            deferred.resolve($filter('filter')($scope.tags, query));

            return deferred.promise;
        };

        /**
         * Performs initialization and acquisition of data used by the wizard
         */
        (function () {
            // Get tags
            TaggingService.query(
                function (data) {
                    $scope.tags = data;
                },
                function (error) {
                    $log.error('failed: ' + error);
                }
            );
        })();
     }
})();