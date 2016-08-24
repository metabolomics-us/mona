/**
 * Created by sajjan on 7/13/16.
 */

(function () {
    'use strict';
    BasicUploaderController.$inject = ['$scope', '$rootScope', '$window', '$location', 'UploadLibraryService', 'gwCtsService', 'gwChemifyService', 'TaggingService', '$q', '$filter', 'AsyncService', '$log', 'REST_BACKEND_SERVER', '$http'];
    angular.module('moaClientApp')
        .controller('BasicUploaderController', BasicUploaderController);

    /* @ngInject */
    function BasicUploaderController($scope, $rootScope, $window, $location, UploadLibraryService, gwCtsService, gwChemifyService,
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



        $scope.parsePastedSpectrum = function(pastedSpectrum) {
            $log.debug("Parsing "+ pastedSpectrum);

            $scope.spectrumIons = pastedSpectrum.split(' ').map(function(x) {
                x = x.split(':');
                var annotation = '';

                return {
                    ion: parseFloat(x[0]),
                    intensity: parseFloat(x[1]),
                    ionStr: x[0],
                    intensityStr: x[1],
                    annotation: annotation,
                    selected: true
                }
            });

            $scope.showIonTable = ($scope.spectrumIons.length < 500);
            $scope.queryState = 2;
            $scope.spectraCount = 1;
            $scope.page = 2;

            $scope.currentSpectrum = spectrum;
            $scope.showIonTable = $scope.currentSpectrum.ions.length < 500;
        };



        function namesToInChIKey(names, callback) {
            if (names.length == 0) {
                return null;
            } else {
                gwChemifyService.nameToInChIKey(names[0], function(molecule) {
                    console.log(molecule)
                    if (molecule !== null) {
                        callback(molecule);
                    } else {
                        namesToInChIKey(names.slice(1));
                    }
                }, function(error) {
                    namesToInChIKey(names.slice(1));
                });
            }
        }

        $scope.retrieveCompoundData = function() {
            $log.info("Retrieving MOL data...");

            if ($scope.currentSpectrum.inchiKey) {
                gwCtsService.convertInchiKeyToMol($scope.currentSpectrum.inchiKey, function (molecule) {
                    if (molecule !== null) {
                        $scope.currentSpectrum.molFile = molecule;
                    } else if ($scope.currentSpectrum.inchi) {
                        gwCtsService.convertInChICodeToMol($scope.currentSpectrum.inchi, function (molecule) {
                            if (molecule !== null) {
                                $scope.currentSpectrum.molFile = molecule;
                            }
                        });
                    }
                });
            }

            else if ($scope.currentSpectrum.inchi) {
                gwCtsService.convertInChICodeToMol($scope.currentSpectrum.inchi, function (molecule) {
                    if (molecule !== null) {
                        $scope.currentSpectrum.molFile = molecule;
                    }
                });
            }

            else {
                namesToInChIKey($scope.currentSpectrum.names, function(inchiKey) {
                    if (inchiKey !== null) {
                        console.log('Found InChIKey: '+ inchiKey)
                        $scope.currentSpectrum.inchiKey = inchiKey;

                        gwCtsService.convertInchiKeyToMol(inchiKey, function (molecule) {
                            if (molecule !== null) {
                                $scope.currentSpectrum.molFile = molecule;
                            }
                        });
                    }
                });
            }
        };


        $scope.convertMolToInChI = function () {
            if (angular.isDefined($scope.currentSpectrum.molFile) && $scope.currentSpectrum.molFile !== '') {
                gwCtsService.convertToInchiKey($scope.currentSpectrum.molFile, function (result) {
                    console.log(result);
                    $scope.currentSpectrum.inchiKey = result.inchikey;
                });
            }
        };

        $scope.addName = function () {
            if ($scope.currentSpectrum.names[$scope.currentSpectrum.names.length - 1] !== '') {
                $scope.currentSpectrum.names.push('');
            }
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
                                $scope.page = 2;
                                $scope.showIonTable = $scope.currentSpectrum.ions.length < 500;

                                if (!$scope.currentSpectrum.molFile) {
                                    $scope.retrieveCompoundData();
                                }
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